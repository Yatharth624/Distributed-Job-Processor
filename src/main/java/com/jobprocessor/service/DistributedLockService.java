package com.jobprocessor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DistributedLockService {

    private static final Logger logger = LoggerFactory.getLogger(DistributedLockService.class);
    private static final String LOCK_PREFIX = "distributed:lock:job:";

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    // Fallback local lock map: jobId -> LockMetadata
    private final ConcurrentHashMap<String, LockMetadata> localLockMap = new ConcurrentHashMap<>();

    private record LockMetadata(String workerId, Instant expiresAt) {}

    public boolean tryAcquireLock(String jobId, String workerId, long leaseTimeSeconds) {
        String lockKey = LOCK_PREFIX + jobId;

        if (redisTemplate != null) {
            try {
                Boolean acquired = redisTemplate.opsForValue()
                        .setIfAbsent(lockKey, workerId, Duration.ofSeconds(leaseTimeSeconds));
                if (Boolean.TRUE.equals(acquired)) {
                    logger.debug("Worker {} acquired Redis lock for job {}", workerId, jobId);
                    return true;
                }
                return false;
            } catch (Exception e) {
                logger.warn("Redis lock acquisition failed ({}), using local lock fallback", e.getMessage());
            }
        }

        // Local in-memory lock check
        Instant now = Instant.now();
        LockMetadata existing = localLockMap.get(jobId);
        if (existing != null && existing.expiresAt().isAfter(now)) {
            return false; // Lock is held and active
        }

        localLockMap.put(jobId, new LockMetadata(workerId, now.plusSeconds(leaseTimeSeconds)));
        logger.debug("Worker {} acquired Local lock for job {}", workerId, jobId);
        return true;
    }

    public boolean renewLock(String jobId, String workerId, long leaseTimeSeconds) {
        String lockKey = LOCK_PREFIX + jobId;

        if (redisTemplate != null) {
            try {
                String val = redisTemplate.opsForValue().get(lockKey);
                if (workerId.equals(val)) {
                    redisTemplate.expire(lockKey, Duration.ofSeconds(leaseTimeSeconds));
                    return true;
                }
                return false;
            } catch (Exception e) {
                logger.warn("Redis lock renew failed: {}", e.getMessage());
            }
        }

        LockMetadata existing = localLockMap.get(jobId);
        if (existing != null && workerId.equals(existing.workerId())) {
            localLockMap.put(jobId, new LockMetadata(workerId, Instant.now().plusSeconds(leaseTimeSeconds)));
            return true;
        }

        return false;
    }

    public void releaseLock(String jobId, String workerId) {
        String lockKey = LOCK_PREFIX + jobId;

        if (redisTemplate != null) {
            try {
                String val = redisTemplate.opsForValue().get(lockKey);
                if (workerId.equals(val)) {
                    redisTemplate.delete(lockKey);
                    logger.debug("Worker {} released Redis lock for job {}", workerId, jobId);
                }
            } catch (Exception e) {
                logger.warn("Redis lock release failed: {}", e.getMessage());
            }
        }

        LockMetadata existing = localLockMap.get(jobId);
        if (existing != null && workerId.equals(existing.workerId())) {
            localLockMap.remove(jobId);
            logger.debug("Worker {} released Local lock for job {}", workerId, jobId);
        }
    }
}
