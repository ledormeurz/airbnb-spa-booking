package com.airbnbspa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Filtre HTTP : limite le débit sur login, register, booking et sync calendrier.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitProperties properties,
                           RateLimitService rateLimitService,
                           ObjectMapper objectMapper) {
        this.properties = properties;
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        return resolvePolicy(request) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        LimitPolicy policy = resolvePolicy(request);
        if (policy == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        String key = policy.name() + ":" + clientIp;

        boolean allowed = rateLimitService.tryAcquire(
                key,
                policy.limit().getMaxRequests(),
                policy.limit().getWindowMs()
        );

        if (!allowed) {
            writeTooManyRequests(response, policy);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private LimitPolicy resolvePolicy(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }

        String path = normalizePath(request.getRequestURI());

        if ("/api/public/login".equals(path)) {
            return new LimitPolicy("login", properties.getLogin());
        }
        if ("/api/public/register".equals(path)) {
            return new LimitPolicy("register", properties.getRegister());
        }
        if ("/api/public/booking-requests".equals(path)) {
            return new LimitPolicy("booking", properties.getBooking());
        }
        if ("/api/admin/calendar-feeds/sync-all".equals(path)
                || path.matches("/api/admin/calendar-feeds/\\d+/sync")) {
            return new LimitPolicy("sync", properties.getSync());
        }
        return null;
    }

    private String normalizePath(String uri) {
        if (uri == null || uri.isBlank()) {
            return "";
        }
        // Retire le context-path éventuel et le slash final
        String path = uri;
        int semicolon = path.indexOf(';');
        if (semicolon >= 0) {
            path = path.substring(0, semicolon);
        }
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private void writeTooManyRequests(HttpServletResponse response, LimitPolicy policy) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        long retryAfterSeconds = Math.max(1, policy.limit().getWindowMs() / 1000);
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 429);
        body.put("error", "Too Many Requests");
        body.put("message", "Trop de requêtes. Réessayez dans quelques minutes.");
        body.put("limit", policy.name());
        body.put("timestamp", LocalDateTime.now().toString());

        objectMapper.writeValue(response.getWriter(), body);
    }

    private record LimitPolicy(String name, RateLimitProperties.EndpointLimit limit) {}
}
