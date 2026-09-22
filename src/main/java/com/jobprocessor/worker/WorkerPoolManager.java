package com.jobprocessor.worker;

import com.jobprocessor.domain.JobEntity;
import com.jobprocessor.domain.JobStatus;
import com.jobprocessor.repository.JobRepository;
import com.jobprocessor.service.DistributedLockService;
import com.jobprocessor.service.JobExecutionService;
import com.jobprocessor.service.PriorityQueueService;
import com.jobprocessor.service.WebhookDispatcherService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WorkerPoolManager {

    private static final Logger logger = LoggerFactory.getLogger(WorkerPoolManager.class);

    private final String workerNodeId = "worker-node-" + UUID.randomUUID().toString().substring(0, 8);

    @Value("${job.worker.pool-size:4}")
    private int poolSize;

    @Autowired
    private PriorityQueueService priorityQueueService;

    @Autowired
    private DistributedLockService lockService;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobExecutionService executionService;

    @Autowired
    private WebhookDispatcherService webhookDispatcherService;

    private ExecutorService workerExecutor;
    private ScheduledExecutorService pollingExecutor;
    private final AtomicInteger activeWorkerCount = new AtomicInteger(0);
    private volatile boolean running = true;

    @PostConstruct
    public void startWorkerPool() {
        logger.info("Initializing Worker Pool Node: {} with {} worker threads", workerNodeId, poolSize);

        workerExecutor = Executors.newFixedThreadPool(poolSize, new ThreadFactory() {
            private final AtomicInteger threadId = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "job-worker-thread-" + threadId.getAndIncrement());
            }
        });

        pollingExecutor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "job-poller-thread"));
        pollingExecutor.scheduleWithFixedDelay(this::pollAndDispatch, 500, 200, TimeUnit.MILLISECONDS);
    }

    private void pollAndDispatch() {
        if (!running) return;

        try {
            // Only poll if we have available capacity in thread pool
            if (activeWorkerCount.get() >= poolSize) {
                return;
            }

            String jobId = priorityQueueService.popHighestPriorityJob();
            if (jobId == null) {
                return;
            }

            // Lock & process job asynchronously
            workerExecutor.submit(() -> processJob(jobId));

        } catch (Exception e) {
            logger.error("Error in worker polling loop", e);
        }
    }

    private void processJob(String jobId) {
        activeWorkerCount.incrementAndGet();
        try {
            boolean acquired = lockService.tryAcquireLock(jobId, workerNodeId, 30);
            if (!acquired) {
                logger.debug("Job {} is already locked by another worker. Skipping.", jobId);
                return;
            }

            JobEntity job = jobRepository.findById(jobId).orElse(null);
            if (job == null || job.getStatus() == JobStatus.CANCELLED || job.getStatus() == JobStatus.COMPLETED) {
                lockService.releaseLock(jobId, workerNodeId);
                return;
            }

            // Update DB status to RUNNING
            job.setStatus(JobStatus.RUNNING);
            job.setWorkerId(workerNodeId);
            job.setStartedAt(LocalDateTime.now());
            job.setLastHeartbeat(LocalDateTime.now());
            jobRepository.save(job);

            logger.info("Worker {} started processing job {} (Type: {}, Priority: {})",
                    workerNodeId, jobId, job.getJobType(), job.getPriority());

            long startTime = System.currentTimeMillis();
            try {
                // Execute computational workload
                String result = executionService.executeJob(job.getJobType(), job.getPayload(), job.getRetryCount());

                long elapsed = System.currentTimeMillis() - startTime;
                job.setStatus(JobStatus.COMPLETED);
                job.setResult(result);
                job.setExecutionTimeMs(elapsed);
                job.setCompletedAt(LocalDateTime.now());
                JobEntity saved = jobRepository.save(job);

                logger.info("Job {} COMPLETED in {} ms", jobId, elapsed);

                // Dispatch webhook if configured
                webhookDispatcherService.dispatchWebhookNotification(saved);

            } catch (Exception ex) {
                long elapsed = System.currentTimeMillis() - startTime;
                handleJobFailure(job, ex, elapsed);
            }

        } catch (Exception e) {
            logger.error("Unexpected error executing job {}", jobId, e);
        } finally {
            lockService.releaseLock(jobId, workerNodeId);
            activeWorkerCount.decrementAndGet();
        }
    }

    private void handleJobFailure(JobEntity job, Exception ex, long elapsed) {
        int currentRetries = job.getRetryCount() + 1;
        job.setRetryCount(currentRetries);
        job.setExecutionTimeMs(elapsed);
        job.setErrorMessage(ex.getMessage());

        if (currentRetries <= job.getMaxRetries()) {
            job.setStatus(JobStatus.RETRYING);
            jobRepository.save(job);

            long backoffMs = (long) Math.pow(2, currentRetries) * 1000L;
            logger.warn("Job {} failed attempt {}/{}. Re-enqueueing after backoff {} ms. Error: {}",
                    job.getId(), currentRetries, job.getMaxRetries(), backoffMs, ex.getMessage());

            // Schedule re-enqueue after exponential backoff
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                job.setStatus(JobStatus.QUEUED);
                jobRepository.save(job);
                priorityQueueService.enqueue(job.getId(), job.getPriority());
            }, backoffMs, TimeUnit.MILLISECONDS);

        } else {
            job.setStatus(JobStatus.FAILED);
            job.setCompletedAt(LocalDateTime.now());
            JobEntity saved = jobRepository.save(job);
            logger.error("Job {} FAILED permanently after {} retries. Error: {}",
                    job.getId(), job.getMaxRetries(), ex.getMessage());

            // Dispatch webhook for permanent failure
            webhookDispatcherService.dispatchWebhookNotification(saved);
        }
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        if (pollingExecutor != null) pollingExecutor.shutdownNow();
        if (workerExecutor != null) workerExecutor.shutdown();
    }

    public int getActiveWorkerCount() {
        return activeWorkerCount.get();
    }

    public int getPoolSize() {
        return poolSize;
    }

    public String getWorkerNodeId() {
        return workerNodeId;
    }
}
