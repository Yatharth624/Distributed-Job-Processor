package com.jobprocessor.dto;

import com.jobprocessor.domain.JobPriority;

public class SubmitJobRequest {

    private String name;
    private String jobType;
    private String payload;
    private String webhookUrl;
    private JobPriority priority = JobPriority.MEDIUM;
    private int maxRetries = 3;

    public SubmitJobRequest() {}

    public SubmitJobRequest(String name, String jobType, String payload, String webhookUrl, JobPriority priority, int maxRetries) {
        this.name = name;
        this.jobType = jobType;
        this.payload = payload;
        this.webhookUrl = webhookUrl;
        this.priority = priority;
        this.maxRetries = maxRetries;
    }

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

    public int getMaxRetries() { return maxRetries; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
}
