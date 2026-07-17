package com.airbnbspa.config;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MINUTES = 15;

    private final Map<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    /**
     * Record a failed login attempt for the given username.
     */
    public void loginFailed(String username) {
        AttemptInfo info = attemptsCache.get(username);
        if (info == null) {
            info = new AttemptInfo(1, Instant.now());
            attemptsCache.put(username, info);
        } else {
            info.incrementAttempts();
            info.setLastAttempt(Instant.now());
        }
    }

    /**
     * Record a successful login attempt, clearing any previous failures.
     */
    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
    }

    /**
     * Check if the given username is currently blocked due to too many failed attempts.
     */
    public boolean isBlocked(String username) {
        AttemptInfo info = attemptsCache.get(username);
        if (info == null) {
            return false;
        }

        // If the block duration has passed, clear and allow
        if (Instant.now().minusSeconds(BLOCK_DURATION_MINUTES * 60).isAfter(info.getLastAttempt())) {
            attemptsCache.remove(username);
            return false;
        }

        return info.getAttempts() >= MAX_ATTEMPTS;
    }

    /**
     * Get the remaining block time in minutes for a blocked user.
     */
    public long getRemainingBlockTimeMinutes(String username) {
        AttemptInfo info = attemptsCache.get(username);
        if (info == null || info.getAttempts() < MAX_ATTEMPTS) {
            return 0;
        }

        long elapsed = java.time.Duration.between(info.getLastAttempt(), Instant.now()).toMinutes();
        long remaining = BLOCK_DURATION_MINUTES - elapsed;
        return Math.max(0, remaining);
    }

    private static class AttemptInfo {
        private int attempts;
        private Instant lastAttempt;

        public AttemptInfo(int attempts, Instant lastAttempt) {
            this.attempts = attempts;
            this.lastAttempt = lastAttempt;
        }

        public int getAttempts() {
            return attempts;
        }

        public void incrementAttempts() {
            this.attempts++;
        }

        public Instant getLastAttempt() {
            return lastAttempt;
        }

        public void setLastAttempt(Instant lastAttempt) {
            this.lastAttempt = lastAttempt;
        }
    }
}