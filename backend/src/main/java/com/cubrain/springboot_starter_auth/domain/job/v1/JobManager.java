package com.cubrain.springboot_starter_auth.domain.job.v1;

import com.cubrain.springboot_starter_auth.domain.job.JobStatus;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface JobManager {
    void addSseEmitter(String jobId, SseEmitter emitter);

    void removeSseEmitter(String jobId, SseEmitter emitter);

    void cleanupOldJobs();

    String createJob();

    String createJob(String ownerId);

    boolean hasActiveJob(String ownerId);

    void updateProgress(String jobId, int progress);

    void completeJob(String jobId, Object result);

    void completeJob(String jobId);

    void failJob(String jobId);

    JobStatus getStatus(String jobId);

    Integer getProgress(String jobId);

    Object getResults(String jobId);

    void updateMetadata(String jobId, String key, Object value);

    java.util.Map<String, Object> getMetadata(String jobId);

    java.util.Optional<String> findLastJobIdByOwner(String ownerId);

    java.util.List<String> findAllJobsByOwner(String ownerId);

    void changeOwner(String jobId, String newOwnerId);
}
