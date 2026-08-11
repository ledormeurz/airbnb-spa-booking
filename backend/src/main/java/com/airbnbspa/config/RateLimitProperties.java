package com.airbnbspa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /**
     * Active le filtre de rate limiting HTTP.
     * En local/tests on peut le désactiver via RATE_LIMIT_ENABLED=false.
     */
    private boolean enabled = true;

    private EndpointLimit login = new EndpointLimit(20, 60_000);
    private EndpointLimit register = new EndpointLimit(10, 600_000);
    private EndpointLimit booking = new EndpointLimit(15, 900_000);
    private EndpointLimit sync = new EndpointLimit(30, 600_000);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public EndpointLimit getLogin() {
        return login;
    }

    public void setLogin(EndpointLimit login) {
        this.login = login;
    }

    public EndpointLimit getRegister() {
        return register;
    }

    public void setRegister(EndpointLimit register) {
        this.register = register;
    }

    public EndpointLimit getBooking() {
        return booking;
    }

    public void setBooking(EndpointLimit booking) {
        this.booking = booking;
    }

    public EndpointLimit getSync() {
        return sync;
    }

    public void setSync(EndpointLimit sync) {
        this.sync = sync;
    }

    public static class EndpointLimit {
        /** Nombre max de requêtes dans la fenêtre. */
        private int maxRequests = 20;
        /** Durée de la fenêtre en millisecondes. */
        private long windowMs = 60_000;

        public EndpointLimit() {
        }

        public EndpointLimit(int maxRequests, long windowMs) {
            this.maxRequests = maxRequests;
            this.windowMs = windowMs;
        }

        public int getMaxRequests() {
            return maxRequests;
        }

        public void setMaxRequests(int maxRequests) {
            this.maxRequests = maxRequests;
        }

        public long getWindowMs() {
            return windowMs;
        }

        public void setWindowMs(long windowMs) {
            this.windowMs = windowMs;
        }
    }
}
