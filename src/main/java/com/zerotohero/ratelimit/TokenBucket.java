package com.zerotohero.ratelimit;

import java.util.function.LongSupplier;

/**
 * Module 23: the token-bucket algorithm. The bucket holds up to {@code capacity} tokens and refills
 * at a fixed rate; each request takes one token, and a request with no token left is rejected.
 * Bursts up to the capacity are allowed; the long-run rate can't exceed the refill rate.
 */
public class TokenBucket {

    private final long capacity;
    private final double refillPerNano;
    private final LongSupplier nanoClock;
    private double tokens;
    private long lastRefill;

    public TokenBucket(long capacity, double refillPerSecond, LongSupplier nanoClock) {
        this.capacity = capacity;
        this.refillPerNano = refillPerSecond / 1_000_000_000d;
        this.nanoClock = nanoClock;
        this.tokens = capacity;
        this.lastRefill = nanoClock.getAsLong();
    }

    public synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    /** Seconds until the next token is available; used for the Retry-After header. */
    public synchronized long secondsUntilNextToken() {
        refill();
        if (tokens >= 1) return 0;
        return (long) Math.ceil((1 - tokens) / refillPerNano / 1_000_000_000d);
    }

    private void refill() {
        long now = nanoClock.getAsLong();
        tokens = Math.min(capacity, tokens + (now - lastRefill) * refillPerNano);
        lastRefill = now;
    }
}
