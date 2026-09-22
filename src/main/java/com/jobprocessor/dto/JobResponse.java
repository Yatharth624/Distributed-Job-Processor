package com.jobprocessor.dto;

import com.jobprocessor.domain.JobEntity;
import com.jobprocessor.domain.JobPriority;
import com.jobprocessor.domain.JobStatus;

import java.time.LocalDateTime;

public class JobResponse {

    private String id;
    private String name;
    private String jobType;
    private String payload;
    private String webhookUrl;
    private JobPriority priority;
    private JobStatus status;
    private int maxRetries;
    private int retryCount;
    private String result;
    private String errorMessage;
    private String workerId;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long executionTimeMs;

    public JobResponse() {}

    public static JobResponse fromEntity(JobEntity entity) {
        JobResponse dto = new JobResponse();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.jobType = entity.getJobType();
        dto.payload = entity.getPayload();
        dto.webhookUrl = entity.getWebhookUrl();
        dto.priority = entity.getPriority();
        dto.status = entity.getStatus();
        dto.maxRetries = entity.getMaxRetries();
        dto.retryCount = entity.getRetryCount();
        dto.result = entity.getResult();
        dto.errorMessage = entity.getErrorMessage();
        dto.workerId = entity.getWorkerId();
        dto.createdAt = entity.getCreatedAt();
        dto.startedAt = entity.getStartedAt();
        dto.completedAt = entity.getCompletedAt();
        dto.executionTimeMs = entity.getExecutionTimeMs();
        return dto;
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

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
}
