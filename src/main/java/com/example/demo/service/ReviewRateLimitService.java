package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Rate limiting service for review operations
 * Prevents spam and abuse of review system
 */
@Service
@Slf4j
public class ReviewRateLimitService {

    // Store last review time for each user
    private final ConcurrentMap<String, LocalDateTime> lastReviewTime = new ConcurrentHashMap<>();
    
    // Minimum time between reviews (in minutes)
    private static final int MIN_REVIEW_INTERVAL_MINUTES = 1;
    
    // Maximum reviews per user per day
    private static final int MAX_REVIEWS_PER_DAY = 10;
    
    // Store daily review counts
    private final ConcurrentMap<String, DailyReviewCount> dailyReviewCounts = new ConcurrentHashMap<>();

    /**
     * Check if user can create a review now
     */
    public boolean canCreateReview(String userEmail) {
        return canCreateReviewByTime(userEmail) && canCreateReviewByDailyLimit(userEmail);
    }

    /**
     * Record that user has created a review
     */
    public void recordReviewCreation(String userEmail) {
        lastReviewTime.put(userEmail, LocalDateTime.now());
        
        // Update daily count
        String today = LocalDateTime.now().toLocalDate().toString();
        String key = userEmail + ":" + today;
        
        dailyReviewCounts.compute(key, (k, count) -> {
            if (count == null || !count.date.equals(today)) {
                return new DailyReviewCount(today, 1);
            } else {
                return new DailyReviewCount(today, count.count + 1);
            }
        });
        
        log.info("Recorded review creation for user: {}", userEmail);
    }

    /**
     * Check time-based rate limit
     */
    private boolean canCreateReviewByTime(String userEmail) {
        LocalDateTime lastTime = lastReviewTime.get(userEmail);
        if (lastTime == null) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastReview = java.time.Duration.between(lastTime, now).toMinutes();
        
        boolean canCreate = minutesSinceLastReview >= MIN_REVIEW_INTERVAL_MINUTES;
        
        if (!canCreate) {
            log.warn("Rate limit exceeded for user {}: {} minutes since last review", 
                    userEmail, minutesSinceLastReview);
        }
        
        return canCreate;
    }

    /**
     * Check daily limit
     */
    private boolean canCreateReviewByDailyLimit(String userEmail) {
        String today = LocalDateTime.now().toLocalDate().toString();
        String key = userEmail + ":" + today;
        
        DailyReviewCount count = dailyReviewCounts.get(key);
        if (count == null || !count.date.equals(today)) {
            return true;
        }
        
        boolean canCreate = count.count < MAX_REVIEWS_PER_DAY;
        
        if (!canCreate) {
            log.warn("Daily limit exceeded for user {}: {} reviews today", 
                    userEmail, count.count);
        }
        
        return canCreate;
    }

    /**
     * Get remaining time until next review allowed (in minutes)
     */
    public long getRemainingTimeMinutes(String userEmail) {
        LocalDateTime lastTime = lastReviewTime.get(userEmail);
        if (lastTime == null) {
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutesSinceLastReview = java.time.Duration.between(lastTime, now).toMinutes();
        
        return Math.max(0, MIN_REVIEW_INTERVAL_MINUTES - minutesSinceLastReview);
    }

    /**
     * Get remaining daily review count
     */
    public int getRemainingDailyReviews(String userEmail) {
        String today = LocalDateTime.now().toLocalDate().toString();
        String key = userEmail + ":" + today;
        
        DailyReviewCount count = dailyReviewCounts.get(key);
        if (count == null || !count.date.equals(today)) {
            return MAX_REVIEWS_PER_DAY;
        }
        
        return Math.max(0, MAX_REVIEWS_PER_DAY - count.count);
    }

    /**
     * Clean up old entries (should be called periodically)
     */
    public void cleanup() {
        String today = LocalDateTime.now().toLocalDate().toString();
        
        // Remove old daily counts (keep only today and yesterday)
        dailyReviewCounts.entrySet().removeIf(entry -> {
            String date = entry.getValue().date;
            return !date.equals(today) && 
                   !date.equals(LocalDateTime.now().minusDays(1).toLocalDate().toString());
        });
        
        // Remove old last review times (older than 24 hours)
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        lastReviewTime.entrySet().removeIf(entry -> entry.getValue().isBefore(yesterday));
        
        log.debug("Cleaned up rate limit data");
    }

    /**
     * Inner class to track daily review counts
     */
    private static class DailyReviewCount {
        final String date;
        final int count;
        
        DailyReviewCount(String date, int count) {
            this.date = date;
            this.count = count;
        }
    }
}
