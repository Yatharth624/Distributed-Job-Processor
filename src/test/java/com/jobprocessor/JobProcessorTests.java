package com.jobprocessor;

import com.jobprocessor.domain.JobPriority;
import com.jobprocessor.domain.JobStatus;
import com.jobprocessor.dto.JobResponse;
import com.jobprocessor.dto.SubmitJobRequest;
import com.jobprocessor.dto.SystemMetricsResponse;
import com.jobprocessor.security.RateLimiterService;
import com.jobprocessor.service.JobExecutionService;
import com.jobprocessor.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JobProcessorTests {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobExecutionService executionService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @Test
    void testNewEnterpriseWorkloads() throws Exception {
        String aiResult = executionService.executeJob("AI_INFERENCE", "1024 tokens", 0);
        assertTrue(aiResult.contains("Evaluated LLM Transformer inference"));

        String imgResult = executionService.executeJob("IMAGE_TRANSFORMATION", "4K_RAW", 0);
        assertTrue(imgResult.contains("Transformed & compressed image asset"));

        String pdfResult = executionService.executeJob("REPORT_GENERATION", "25", 0);
        assertTrue(pdfResult.contains("Generated PDF Financial Report"));
    }

    @Test
    void testJobSubmissionWithWebhook() throws Exception {
        SubmitJobRequest jobReq = new SubmitJobRequest(
                "Webhook Test Job",
                "AI_INFERENCE",
                "512",
                "https://httpbin.org/post",
                JobPriority.HIGH,
                3
        );

        JobResponse response = jobService.submitJob(jobReq);
        assertNotNull(response.getId());
        assertEquals("https://httpbin.org/post", response.getWebhookUrl());

        Thread.sleep(1200);

        JobResponse updated = jobService.getJob(response.getId());
        assertTrue(updated.getStatus() == JobStatus.RUNNING || updated.getStatus() == JobStatus.COMPLETED);
    }

    @Test
    void testRateLimiterService() {
        String apiKey = "test-rate-limit-key";
        for (int i = 0; i < 60; i++) {
            assertTrue(rateLimiterService.tryAcquire(apiKey), "Request " + i + " should pass rate limit");
        }
        // 61st request in same minute should fail
        assertFalse(rateLimiterService.tryAcquire(apiKey), "61st request should be rate limited");
    }

    @Test
    void testSystemMetrics() {
        SystemMetricsResponse metrics = jobService.getSystemMetrics();
        assertNotNull(metrics.getWorkerNodeId());
        assertTrue(metrics.getPoolSize() > 0);
        assertNotNull(metrics.getJobCountsByStatus());
    }
}
