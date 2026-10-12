package com.zerotohero.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.math.BigDecimal;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Module 17: retry, circuit breaker and fallback, tested without Spring or a network. */
class PriceConversionServiceTest {

    ExchangeRateClient client = mock(ExchangeRateClient.class);
    PriceConversionService service;

    @BeforeEach
    void setUp() {
        var breakers = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .slidingWindowSize(4).minimumNumberOfCalls(4).failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMinutes(1)).build());
        var retries = RetryRegistry.of(RetryConfig.custom().maxAttempts(3).waitDuration(Duration.ofMillis(1)).build());
        service = new PriceConversionService(client, breakers, retries);
    }

    @Test
    void retriesATransientFailure() {
        when(client.rate("USD", "INR"))
                .thenThrow(new IllegalStateException("blip"))
                .thenReturn(new BigDecimal("83"));
        var result = service.convert(new BigDecimal("10"), "USD", "INR");
        assertThat(result.amount()).isEqualByComparingTo("830.00");
        assertThat(result.stale()).isFalse();
        verify(client, times(2)).rate("USD", "INR");
    }

    @Test
    void fallsBackToTheLastKnownRate() {
        when(client.rate("USD", "EUR")).thenReturn(new BigDecimal("0.9")).thenThrow(new IllegalStateException("down"));
        service.convert(BigDecimal.ONE, "USD", "EUR");
        var result = service.convert(new BigDecimal("100"), "USD", "EUR");
        assertThat(result.amount()).isEqualByComparingTo("90.00");
        assertThat(result.stale()).isTrue();
    }

    @Test
    void circuitOpensAfterRepeatedFailuresAndStopsCallingTheService() {
        when(client.rate("USD", "GBP")).thenThrow(new IllegalStateException("down"));
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> service.convert(BigDecimal.ONE, "USD", "GBP"))
                    .hasMessageContaining("unavailable");
        }
        assertThat(service.circuitState()).isEqualTo(CircuitBreaker.State.OPEN);
        verify(client, times(12)).rate("USD", "GBP"); // 4 calls x 3 attempts

        assertThatThrownBy(() -> service.convert(BigDecimal.ONE, "USD", "GBP")).hasMessageContaining("unavailable");
        verify(client, times(12)).rate("USD", "GBP"); // open circuit: no new call reached the client
    }
}
