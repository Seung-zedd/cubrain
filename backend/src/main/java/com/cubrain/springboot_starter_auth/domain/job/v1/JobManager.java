package com.cubrain.springboot_starter_auth.domain.job.v1;

import com.cubrain.springboot_starter_auth.domain.job.JobStatus;

public interface JobManager {
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
}
