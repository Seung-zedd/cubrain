package com.cubrain.springboot_starter_auth.domain.job;

import com.cubrain.springboot_starter_auth.domain.card.FlashcardResponseDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.COMPLETED;
import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.FAILED;
import static com.cubrain.springboot_starter_auth.domain.job.JobStatus.PROCESSING;

@Component
public class JobManager {

    private final Map<String, JobStatus> jobStatuses = new ConcurrentHashMap<>();
    private final Map<String, List<FlashcardResponseDto>> jobResults = new ConcurrentHashMap<>();
    private final Map<String, Integer> jobProgress = new ConcurrentHashMap<>(); // 0 to 100

    public String createJob() {
        String jobId = UUID.randomUUID().toString();
        jobStatuses.put(jobId, PROCESSING);
        jobProgress.put(jobId, 0);
        return jobId;
    }

    public void updateProgress(String jobId, int progress) {
        jobProgress.put(jobId, progress);
    }

    public void completeJob(String jobId, List<FlashcardResponseDto> results) {
        jobResults.put(jobId, results);
        jobStatuses.put(jobId, COMPLETED);
        jobProgress.put(jobId, 100);
    }

    public void failJob(String jobId) {
        jobStatuses.put(jobId, FAILED);
    }

    public JobStatus getStatus(String jobId) {
        return jobStatuses.get(jobId);
    }

    public Integer getProgress(String jobId) {
        return jobProgress.getOrDefault(jobId, 0);
    }

    public List<FlashcardResponseDto> getResults(String jobId) {
        return jobResults.get(jobId);
    }
}
