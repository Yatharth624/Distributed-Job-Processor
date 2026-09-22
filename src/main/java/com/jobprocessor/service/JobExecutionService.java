package com.jobprocessor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class JobExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(JobExecutionService.class);
    private final Random random = new Random();

    public String executeJob(String jobType, String payload, int currentRetry) throws Exception {
        logger.info("Executing computational task: type={}, retryAttempt={}", jobType, currentRetry);

        switch (jobType.toUpperCase()) {
            case "CALCULATION":
                return performCalculation(payload);

            case "DATA_PROCESSING":
                return performDataProcessing(payload);

            case "SIMULATION":
                return performSimulation(payload);

            case "IMAGE_TRANSFORMATION":
                return performImageTransformation(payload);

            case "REPORT_GENERATION":
                return performReportGeneration(payload);

            case "AI_INFERENCE":
                return performAiInference(payload);

            case "FAIL_SIMULATION":
                if (currentRetry < 2) {
                    throw new RuntimeException("Simulated transient failure on attempt " + currentRetry + " (Will Retry!)");
                }
                return "Completed successfully after retry attempt " + currentRetry;

            default:
                // Default workload simulation
                Thread.sleep(1500 + random.nextInt(1000));
                return "Executed generic job type: " + jobType + " with payload length " + (payload != null ? payload.length() : 0);
        }
    }

    private String performCalculation(String payload) throws Exception {
        int n = 35; // Computational workload
        if (payload != null && payload.matches("\\d+")) {
            n = Math.min(Integer.parseInt(payload), 40);
        }

        long startTime = System.currentTimeMillis();
        long fibResult = fibonacci(n);
        long elapsed = System.currentTimeMillis() - startTime;

        return String.format("Computed Fibonacci(%d) = %d in %d ms", n, fibResult, elapsed);
    }

    private String performDataProcessing(String payload) throws Exception {
        Thread.sleep(1200);
        int recordCount = payload != null ? payload.split(",").length * 250 : 1000;
        return String.format("Processed %d data records successfully. Checksum: %x", recordCount, System.currentTimeMillis() % 0xFFFF);
    }

    private String performSimulation(String payload) throws Exception {
        int durationMs = 2000;
        Thread.sleep(durationMs);
        return String.format("Completed 100%% computation simulation. Payload: '%s'", payload != null ? payload : "none");
    }

    private String performImageTransformation(String payload) throws Exception {
        Thread.sleep(1800);
        String format = payload != null && !payload.isBlank() ? payload : "1080p_PNG";
        return String.format("Transformed & compressed image asset [%s]. Resized 1920x1080 -> 800x600 (Compression Ratio 4.2x)", format);
    }

    private String performReportGeneration(String payload) throws Exception {
        Thread.sleep(2200);
        int pages = payload != null && payload.matches("\\d+") ? Integer.parseInt(payload) : 15;
        return String.format("Generated PDF Financial Report (%d pages). Compiled data tables & generated charts in 2.2s", pages);
    }

    private String performAiInference(String payload) throws Exception {
        long startTime = System.currentTimeMillis();
        // Simulate Matrix Multiplication / Transformer Layer Tokens
        double dummyLoss = 0.0;
        for (int i = 0; i < 5_000_000; i++) {
            dummyLoss += Math.sin(i) * Math.cos(i);
        }
        long elapsed = System.currentTimeMillis() - startTime;
        return String.format("Evaluated LLM Transformer inference (%s tokens). Loss score: %.4f in %d ms",
                payload != null && !payload.isBlank() ? payload : "512", dummyLoss, elapsed);
    }

    private long fibonacci(int n) {
        if (n <= 1) return n;
        long a = 0, b = 1;
        for (int i = 2; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        return b;
    }
}
