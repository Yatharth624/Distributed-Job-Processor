package com.jobprocessor.scheduler;

import com.jobprocessor.domain.JobEntity;
import com.jobprocessor.domain.JobStatus;
import com.jobprocessor.repository.JobRepository;
import com.jobprocessor.service.PriorityQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrphanJobRecoveryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OrphanJobRecoveryScheduler.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PriorityQueueService priorityQueueService;

    /**
     * Periodic scheduled check (every 30 seconds) to recover jobs stranded in RUNNING state
     * without a fresh heartbeat (e.g., after worker node crashes or unexpected reboot).
     */
    @Scheduled(fixedDelay = 30000)
    public void recoverOrphanedJobs() {
        LocalDateTime staleThreshold = LocalDateTime.now().minusSeconds(45);
        List<JobEntity> staleJobs = jobRepository.findStaleRunningJobs(staleThreshold);

        if (!staleJobs.isEmpty()) {
            logger.warn("Found {} orphaned/stale RUNNING jobs. Triggering crash recovery...", staleJobs.size());

            for (JobEntity job : staleJobs) {
                logger.info("Recovering orphaned job {} (was assigned to worker: {}). Re-enqueueing...",
                        job.getId(), job.getWorkerId());

                job.setStatus(JobStatus.QUEUED);
                job.setWorkerId(null);
                job.setErrorMessage("Recovered from worker node crash/timeout");
                jobRepository.save(job);

                priorityQueueService.enqueue(job.getId(), job.getPriority());
            }
        }
    }
}
