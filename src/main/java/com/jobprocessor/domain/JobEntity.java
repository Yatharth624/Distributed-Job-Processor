package com.jobprocessor.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "jobs", indexes = {
    @Index(name = "idx_job_status", columnList = "status"),
    @Index(name = "idx_job_priority", columnList = "priority")
})
public class JobEntity {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String jobType;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(length = 500)
    private String webhookUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobPriority priority = JobPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status = JobStatus.PENDING;

    private int maxRetries = 3;

    private int retryCount = 0;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private String workerId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime lastHeartbeat;

    private Long executionTimeMs;

    public JobEntity() {
    }

    public JobEntity(String id, String name, String jobType, String payload, JobPriority priority, int maxRetries) {
        this.id = id;
        this.name = name;
        this.jobType = jobType;
        this.payload = payload;
        this.priority = priority != null ? priority : JobPriority.MEDIUM;
        this.maxRetries = maxRetries;
        this.status = JobStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }

    public JobPriority getPriority() { return priority; }
    public void setPriority(JobPriority priority) { this.priority = priority; }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }

    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}
