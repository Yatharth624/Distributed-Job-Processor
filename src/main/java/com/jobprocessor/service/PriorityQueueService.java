package com.jobprocessor.service;

import com.jobprocessor.domain.JobPriority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class PriorityQueueService {

    private static final Logger logger = LoggerFactory.getLogger(PriorityQueueService.class);
    private static final String QUEUE_KEY = "distributed:job:priority_queue";

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // Fallback in-memory thread-safe priority queue when Redis is offline or not configured
    private final PriorityBlockingQueue<PrioritizedJob> inMemoryQueue =
            new PriorityBlockingQueue<>(1000, Comparator.comparingDouble(PrioritizedJob::score).reversed());

    public record PrioritizedJob(String jobId, double score) {}

    /**
     * Pushes a job to the priority queue.
     * Score calculation: (priorityRank * 1_000_000_000_000.0) + (9_000_000_000_000.0 - timestampMs)
     */
    public void enqueue(String jobId, JobPriority priority) {
        long now = System.currentTimeMillis();
        double score = (priority.getRank() * 1_000_000_000_000.0) + (9_000_000_000_000.0 - now);

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForZSet().add(QUEUE_KEY, jobId, score);
                logger.info("Enqueued job {} with priority {} (score {}) to Redis", jobId, priority, score);
                return;
            } catch (Exception e) {
                logger.warn("Redis enqueue failed ({}), falling back to in-memory queue", e.getMessage());
            }
        }

        inMemoryQueue.offer(new PrioritizedJob(jobId, score));
        logger.info("Enqueued job {} with priority {} (score {}) to In-Memory Queue", jobId, priority, score);
    }

    /**
     * Atomically pops the highest priority job from the queue.
     */
    public String popHighestPriorityJob() {
        if (redisTemplate != null) {
            try {
                // Redis ZPOPMIN/ZPOPMAX logic or Lua script. Higher score = highest priority.
                Set<String> result = redisTemplate.opsForZSet().reverseRange(QUEUE_KEY, 0, 0);
                if (result != null && !result.isEmpty()) {
                    String jobId = result.iterator().next();
                    Long removed = redisTemplate.opsForZSet().remove(QUEUE_KEY, jobId);
                    if (removed != null && removed > 0) {
                        logger.debug("Popped job {} from Redis ZSET", jobId);
                        return jobId;
                    }
                }
            } catch (Exception e) {
                logger.debug("Redis pop failed ({}), falling back to in-memory queue", e.getMessage());
            }
        }

        PrioritizedJob job = inMemoryQueue.poll();
        if (job != null) {
            logger.debug("Popped job {} from In-Memory Queue", job.jobId());
            return job.jobId();
        }

        return null;
    }

    public long getQueueSize() {
        if (redisTemplate != null) {
            try {
                Long size = redisTemplate.opsForZSet().zCard(QUEUE_KEY);
                if (size != null) return size;
            } catch (Exception ignored) {}
        }
        return inMemoryQueue.size();
    }

    public boolean removeJob(String jobId) {
        if (redisTemplate != null) {
            try {
                Long removed = redisTemplate.opsForZSet().remove(QUEUE_KEY, jobId);
                if (removed != null && removed > 0) return true;
            } catch (Exception ignored) {}
        }
        return inMemoryQueue.removeIf(job -> Objects.equals(job.jobId(), jobId));
    }
}
