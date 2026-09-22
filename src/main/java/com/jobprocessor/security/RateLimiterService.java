package com.jobprocessor.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private final ConcurrentHashMap<String, KeyBucket> buckets = new ConcurrentHashMap<>();

    private record KeyBucket(int tokens, Instant resetTime) {}

    public synchronized boolean tryAcquire(String apiKey) {
        Instant now = Instant.now();
        KeyBucket bucket = buckets.get(apiKey);

        if (bucket == null || now.isAfter(bucket.resetTime())) {
            buckets.put(apiKey, new KeyBucket(MAX_REQUESTS_PER_MINUTE - 1, now.plusSeconds(60)));
            return true;
        }

        if (bucket.tokens() > 0) {
            buckets.put(apiKey, new KeyBucket(bucket.tokens() - 1, bucket.resetTime()));
            return true;
        }

        return false;
    }
}
