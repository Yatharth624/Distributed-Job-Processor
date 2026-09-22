package com.jobprocessor.domain;

public enum JobPriority {
    HIGH(300),
    MEDIUM(200),
    LOW(100);

    private final int rank;

    JobPriority(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }
}
