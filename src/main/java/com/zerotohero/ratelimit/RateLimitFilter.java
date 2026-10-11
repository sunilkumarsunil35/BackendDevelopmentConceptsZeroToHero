package com.zerotohero.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Module 23: one token bucket per client IP for /api/**. Over the limit, respond 429 Too Many
 * Requests with Retry-After. In a cluster, keep the buckets in Redis (e.g. Bucket4j) so all
 * instances share one limit.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final long capacity;
    private final double refillPerSecond;

    public RateLimitFilter(@Value("${app.rate-limit.capacity:100}") long capacity,
                           @Value("${app.rate-limit.refill-per-second:50}") double refillPerSecond) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        TokenBucket bucket = buckets.computeIfAbsent(request.getRemoteAddr(),
                ip -> new TokenBucket(capacity, refillPerSecond, System::nanoTime));
        if (bucket.tryConsume()) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(Math.max(1, bucket.secondsUntilNextToken())));
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("{\"title\":\"Too many requests\",\"status\":429}");
    }
}
