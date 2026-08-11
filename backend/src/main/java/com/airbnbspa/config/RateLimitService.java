package com.airbnbspa.config;

import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter in-memory par clé (ex. IP + type d'endpoint).
 * Fenêtre fixe : au-delà de {@code maxRequests} dans {@code windowMs} → refus.
 */
@Service
public class RateLimitService {

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * @return true si la requête est autorisée, false si la limite est atteinte
     */
    public boolean tryAcquire(String key, int maxRequests, long windowMs) {
        if (maxRequests <= 0 || windowMs <= 0) {
            return true;
        }

        long now = System.currentTimeMillis();
        Window window = windows.computeIfAbsent(key, k -> new Window(now));

        synchronized (window) {
            if (now - window.windowStartMs >= windowMs) {
                window.windowStartMs = now;
                window.count = 0;
            }
            if (window.count >= maxRequests) {
                return false;
            }
            window.count++;
            return true;
        }
    }

    /**
     * Nettoyage opportuniste des fenêtres expirées (évite une croissance infinie en mémoire).
     */
    public void evictExpired(long maxIdleMs) {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Window>> it = windows.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Window> entry = it.next();
            Window window = entry.getValue();
            synchronized (window) {
                if (now - window.windowStartMs >= maxIdleMs) {
                    it.remove();
                }
            }
        }
    }

    public void clearAll() {
        windows.clear();
    }

    private static final class Window {
        private long windowStartMs;
        private int count;

        private Window(long windowStartMs) {
            this.windowStartMs = windowStartMs;
            this.count = 0;
        }
    }
}
