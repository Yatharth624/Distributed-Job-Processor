package com.jobprocessor.service;

import com.jobprocessor.domain.JobEntity;
import com.jobprocessor.domain.JobStatus;
import com.jobprocessor.dto.JobResponse;
import com.jobprocessor.dto.SubmitJobRequest;
import com.jobprocessor.dto.SystemMetricsResponse;
import com.jobprocessor.repository.JobRepository;
import com.jobprocessor.worker.WorkerPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class JobService {

    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PriorityQueueService priorityQueueService;

    @Autowired
    private DistributedLockService lockService;

    @Autowired
    private WorkerPoolManager workerPoolManager;

    @Transactional
    public JobResponse submitJob(SubmitJobRequest request) {
        String jobId = UUID.randomUUID().toString();

        JobEntity job = new JobEntity(
                jobId,
                request.getName() != null ? request.getName() : "Job-" + jobId.substring(0, 8),
                request.getJobType() != null ? request.getJobType() : "CALCULATION",
                request.getPayload(),
                request.getPriority(),
                request.getMaxRetries()
        );
        job.setWebhookUrl(request.getWebhookUrl());
        job.setStatus(JobStatus.QUEUED);

        JobEntity saved = jobRepository.save(job);
        logger.info("Created job {} with status QUEUED and priority {}", jobId, request.getPriority());

        // Enqueue to Priority Queue
        priorityQueueService.enqueue(saved.getId(), saved.getPriority());

        return JobResponse.fromEntity(saved);
    }

    public JobResponse getJob(String jobId) {
        JobEntity entity = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        return JobResponse.fromEntity(entity);
    }

    public List<JobResponse> listJobs(JobStatus statusFilter) {
        List<JobEntity> jobs;
        if (statusFilter != null) {
            jobs = jobRepository.findByStatus(statusFilter);
        } else {
            jobs = jobRepository.findAll();
        }

        // Sort by createdAt descending
        jobs.sort(Comparator.comparing(JobEntity::getCreatedAt).reversed());

        return jobs.stream().map(JobResponse::fromEntity).toList();
    }

    @Transactional
    public JobResponse cancelJob(String jobId) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.FAILED) {
            throw new IllegalStateException("Cannot cancel a job that is already " + job.getStatus());
        }

        job.setStatus(JobStatus.CANCELLED);
        JobEntity updated = jobRepository.save(job);

        // Remove from priority queue & lock if present
        priorityQueueService.removeJob(jobId);
        lockService.releaseLock(jobId, workerPoolManager.getWorkerNodeId());

        logger.info("Job {} was CANCELLED successfully", jobId);
        return JobResponse.fromEntity(updated);
    }

    public SystemMetricsResponse getSystemMetrics() {
        Map<String, Long> statusCounts = new HashMap<>();
        for (JobStatus status : JobStatus.values()) {
            statusCounts.put(status.name(), jobRepository.countByStatus(status));
        }

        return new SystemMetricsResponse(
                workerPoolManager.getWorkerNodeId(),
                workerPoolManager.getActiveWorkerCount(),
                workerPoolManager.getPoolSize(),
                priorityQueueService.getQueueSize(),
                statusCounts
        );
    }
}
