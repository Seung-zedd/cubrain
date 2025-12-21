package com.cubrain.springboot_starter_auth.domain.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.COMPLETED;
import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.FAILED;
import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.PROCESSING;

@Slf4j
@Component
public class JobManager {

    private final Map<String, JobStatus> jobStatuses = new ConcurrentHashMap<>();
    private final Map<String, Object> jobResults = new ConcurrentHashMap<>();
    private final Map<String, Integer> jobProgress = new ConcurrentHashMap<>(); // 0 to 100
    private final Map<String, Instant> jobCompletionTimes = new ConcurrentHashMap<>();

    // Cleanup completed jobs older than 1 hour
    private static final long JOB_RETENTION_SECONDS = 3600;

    @Scheduled(fixedRate = 300000) // Run every 5 minutes
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
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("Cleaned up {} old jobs", removedCount);
        }
    }

    public String createJob() {
        String jobId = UUID.randomUUID().toString();
        jobStatuses.put(jobId, PROCESSING);
        jobProgress.put(jobId, 0);
        return jobId;
    }

    public void updateProgress(String jobId, int progress) {
        jobProgress.put(jobId, progress);
    }

    public void completeJob(String jobId, Object result) {
        jobResults.put(jobId, result);
        jobStatuses.put(jobId, COMPLETED);
        jobProgress.put(jobId, 100);
        jobCompletionTimes.put(jobId, Instant.now());
    }

    public void completeJob(String jobId) {
        completeJob(jobId, "Success");
    }

    public void failJob(String jobId) {
        jobStatuses.put(jobId, FAILED);
        jobCompletionTimes.put(jobId, Instant.now());
    }

    public JobStatus getStatus(String jobId) {
        return jobStatuses.get(jobId);
    }

    public Integer getProgress(String jobId) {
        return jobProgress.getOrDefault(jobId, 0);
    }

    public Object getResults(String jobId) {
        return jobResults.get(jobId);
    }
}
