package com.jobprocessor.dto;

import java.util.Map;

public class SystemMetricsResponse {

    private String workerNodeId;
    private int activeWorkerCount;
    private int poolSize;
    private long queueSize;
    private Map<String, Long> jobCountsByStatus;

    public SystemMetricsResponse() {}

    public SystemMetricsResponse(String workerNodeId, int activeWorkerCount, int poolSize, long queueSize, Map<String, Long> jobCountsByStatus) {
        this.workerNodeId = workerNodeId;
        this.activeWorkerCount = activeWorkerCount;
        this.poolSize = poolSize;
        this.queueSize = queueSize;
        this.jobCountsByStatus = jobCountsByStatus;
    }

    public String getWorkerNodeId() { return workerNodeId; }
    public void setWorkerNodeId(String workerNodeId) { this.workerNodeId = workerNodeId; }

    public int getActiveWorkerCount() { return activeWorkerCount; }
    public void setActiveWorkerCount(int activeWorkerCount) { this.activeWorkerCount = activeWorkerCount; }

    public int getPoolSize() { return poolSize; }
    public void setPoolSize(int poolSize) { this.poolSize = poolSize; }

    public long getQueueSize() { return queueSize; }
    public void setQueueSize(long queueSize) { this.queueSize = queueSize; }

    public Map<String, Long> getJobCountsByStatus() { return jobCountsByStatus; }
    public void setJobCountsByStatus(Map<String, Long> jobCountsByStatus) { this.jobCountsByStatus = jobCountsByStatus; }
}
