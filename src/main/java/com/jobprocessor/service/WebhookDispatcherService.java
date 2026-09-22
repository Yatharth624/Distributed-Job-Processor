package com.jobprocessor.service;

import com.jobprocessor.domain.JobEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookDispatcherService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookDispatcherService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    public void dispatchWebhookNotification(JobEntity job) {
        if (job.getWebhookUrl() == null || job.getWebhookUrl().trim().isEmpty()) {
            return;
        }

        String webhookUrl = job.getWebhookUrl();
        logger.info("Dispatching Webhook Notification for Job {} to {}", job.getId(), webhookUrl);

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("jobId", job.getId());
            payload.put("name", job.getName());
            payload.put("jobType", job.getJobType());
            payload.put("status", job.getStatus().name());
            payload.put("result", job.getResult());
            payload.put("errorMessage", job.getErrorMessage());
            payload.put("executionTimeMs", job.getExecutionTimeMs());
            payload.put("completedAt", job.getCompletedAt() != null ? job.getCompletedAt().toString() : null);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Event-Type", "JOB_STATUS_UPDATE");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(webhookUrl, request, String.class);
            logger.info("Webhook successfully delivered for Job {} to {}", job.getId(), webhookUrl);

        } catch (Exception e) {
            logger.warn("Failed to deliver Webhook for Job {} to {}: {}", job.getId(), webhookUrl, e.getMessage());
        }
    }
}
