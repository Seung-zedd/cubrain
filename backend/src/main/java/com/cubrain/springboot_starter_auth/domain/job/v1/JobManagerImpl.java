package com.cubrain.springboot_starter_auth.domain.job.v1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.cubrain.springboot_starter_auth.domain.job.JobStatus;
import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.COMPLETED;
import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.FAILED;
import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.PROCESSING;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class JobManagerImpl implements JobManager {

    private final Map<String, JobStatus> jobStatuses = new ConcurrentHashMap<>();
    private final Map<String, Object> jobResults = new ConcurrentHashMap<>();
    private final Map<String, Integer> jobProgress = new ConcurrentHashMap<>();
    private final Map<String, Instant> jobCompletionTimes = new ConcurrentHashMap<>();
    private final Map<String, String> jobOwners = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> jobMetadata = new ConcurrentHashMap<>();
    private final Map<String, List<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();

    private static final long JOB_RETENTION_SECONDS = 3600;

    @Override
    @Scheduled(fixedRate = 300000)
    public void cleanupOldJobs() {
        Instant now = Instant.now();
        int removedCount = 0;

        for (Map.Entry<String, Instant> entry : jobCompletionTimes.entrySet()) {
            String jobId = entry.getKey();
            Instant completionTime = entry.getValue();

            if (now.getEpochSecond() - completionTime.getEpochSecond() > JOB_RETENTION_SECONDS) {
                jobStatuses.remove(jobId);
                jobResults.remove(jobId);
                jobProgress.remove(jobId);
                jobCompletionTimes.remove(jobId);
                jobOwners.remove(jobId);
                jobMetadata.remove(jobId);
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("Cleaned up {} old jobs", removedCount);
        }
    }

    @Override
    public void addSseEmitter(String jobId, SseEmitter emitter) {
        sseEmitters.computeIfAbsent(jobId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeSseEmitter(jobId, emitter));
        emitter.onTimeout(() -> removeSseEmitter(jobId, emitter));
        emitter.onError(e -> removeSseEmitter(jobId, emitter));
    }

    @Override
    public void removeSseEmitter(String jobId, SseEmitter emitter) {
        List<SseEmitter> emitters = sseEmitters.get(jobId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                sseEmitters.remove(jobId);
            }
        }
    }

    private void broadcast(String jobId, String eventName, Object data) {
        List<SseEmitter> emitters = sseEmitters.get(jobId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.debug("[SSE] Failed to send event {} to emitter for job {}: {}", eventName, jobId, e.getMessage());
                deadEmitters.add(emitter);
            }
        }
        emitters.removeAll(deadEmitters);
    }

    @Override
    public String createJob() {
        return createJob("anonymous");
    }

    @Override
    public String createJob(String ownerId) {
        String jobId = UUID.randomUUID().toString();
        jobStatuses.put(jobId, PROCESSING);
        jobProgress.put(jobId, 0);
        jobOwners.put(jobId, ownerId);
        jobMetadata.put(jobId, new ConcurrentHashMap<>());
        return jobId;
    }

    @Override
    public boolean hasActiveJob(String ownerId) {
        if ("anonymous".equals(ownerId))
            return false; // Allow multiple anonymous jobs for now or handle differently
        return jobOwners.entrySet().stream()
                .filter(e -> e.getValue().equals(ownerId))
                .map(e -> jobStatuses.get(e.getKey()))
                .anyMatch(status -> status == PROCESSING);
    }

    @Override
    public void updateProgress(String jobId, int progress) {
        jobProgress.put(jobId, progress);
        broadcast(jobId, "PROGRESS", Map.of("progress", progress, "status", PROCESSING));
    }

    @Override
    public void completeJob(String jobId, Object result) {
        jobResults.put(jobId, result);
        jobStatuses.put(jobId, COMPLETED);
        jobProgress.put(jobId, 100);
        jobCompletionTimes.put(jobId, Instant.now());
        broadcast(jobId, "COMPLETE", Map.of("progress", 100, "status", COMPLETED, "results", result));
    }

    @Override
    public void completeJob(String jobId) {
        completeJob(jobId, "Success");
    }

    @Override
    public void failJob(String jobId) {
        jobStatuses.put(jobId, FAILED);
        jobCompletionTimes.put(jobId, Instant.now());
        broadcast(jobId, "ERROR", Map.of("status", FAILED, "message", "Job failed"));
    }

    @Override
    public JobStatus getStatus(String jobId) {
        return jobStatuses.get(jobId);
    }

    @Override
    public Integer getProgress(String jobId) {
        return jobProgress.getOrDefault(jobId, 0);
    }

    @Override
    public Object getResults(String jobId) {
        return jobResults.get(jobId);
    }

    @Override
    public void updateMetadata(String jobId, String key, Object value) {
        Map<String, Object> metadata = jobMetadata.get(jobId);
        if (metadata != null) {
            metadata.put(key, value);
        }
    }

    @Override
    public Map<String, Object> getMetadata(String jobId) {
        return jobMetadata.getOrDefault(jobId, Map.of());
    }

    @Override
    public java.util.Optional<String> findLastJobIdByOwner(String ownerId) {
        return findAllJobsByOwner(ownerId).stream().findFirst();
    }

    @Override
    public java.util.List<String> findAllJobsByOwner(String ownerId) {
        return jobOwners.entrySet().stream()
                .filter(e -> e.getValue().equals(ownerId))
                .map(Map.Entry::getKey)
                .sorted((id1, id2) -> {
                    Instant t1 = jobCompletionTimes.getOrDefault(id1, Instant.MIN);
                    Instant t2 = jobCompletionTimes.getOrDefault(id2, Instant.MIN);
                    return t2.compareTo(t1); // Descending (most recent first)
                })
                .toList();
    }

    @Override
    public void changeOwner(String jobId, String newOwnerId) {
        if (jobOwners.containsKey(jobId)) {
            jobOwners.put(jobId, newOwnerId);
        }
    }
}
