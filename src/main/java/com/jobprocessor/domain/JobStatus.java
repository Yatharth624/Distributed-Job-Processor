package com.jobprocessor.domain;

public enum JobStatus {
    PENDING,
    QUEUED,
    RUNNING,
    COMPLETED,
    FAILED,
    RETRYING,
    CANCELLED
}
