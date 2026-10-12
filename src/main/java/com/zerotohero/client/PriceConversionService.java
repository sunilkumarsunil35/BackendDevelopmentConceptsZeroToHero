package com.zerotohero.client;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;

/**
 * Module 17: protect a call to a remote service. Retry handles brief glitches; the circuit
 * breaker stops calling a service that keeps failing; the fallback serves the last good rate.
 * Settings come from resilience4j.* in application.yml.
 */
@Service
public class PriceConversionService {

    private final ExchangeRateClient client;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final Map<String, BigDecimal> lastKnown = new ConcurrentHashMap<>();

    public PriceConversionService(ExchangeRateClient client, CircuitBreakerRegistry circuitBreakers,
                                  RetryRegistry retries) {
        this.client = client;
        this.circuitBreaker = circuitBreakers.circuitBreaker("exchangeRates");
        this.retry = retries.retry("exchangeRates");
    }

    public record Conversion(BigDecimal amount, String currency, boolean stale) {
    }

    public Conversion convert(BigDecimal amount, String from, String to) {
        String pair = from + "->" + to;
        // Retry wraps the raw call; the circuit breaker wraps the retries.
        Supplier<BigDecimal> call = Retry.decorateSupplier(retry, () -> client.rate(from, to));
        Supplier<BigDecimal> guarded = CircuitBreaker.decorateSupplier(circuitBreaker, call);
        try {
            BigDecimal rate = guarded.get();
            lastKnown.put(pair, rate);
            return new Conversion(amount.multiply(rate).setScale(2, RoundingMode.HALF_UP), to, false);
        } catch (RuntimeException e) {
            BigDecimal cached = lastKnown.get(pair);
            if (cached == null) throw new IllegalStateException("Exchange rates are unavailable right now", e);
            return new Conversion(amount.multiply(cached).setScale(2, RoundingMode.HALF_UP), to, true);
        }
    }

    public CircuitBreaker.State circuitState() {
        return circuitBreaker.getState();
    }
}
