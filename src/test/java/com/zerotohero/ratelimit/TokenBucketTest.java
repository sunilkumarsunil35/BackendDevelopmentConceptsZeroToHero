package com.zerotohero.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Module 23: a fake clock makes time-based code testable. */
class TokenBucketTest {

    private final AtomicLong now = new AtomicLong();

    @Test
    void allowsABurstThenRefillsOverTime() {
        TokenBucket bucket = new TokenBucket(3, 1.0, now::get);
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();
        assertThat(bucket.secondsUntilNextToken()).isEqualTo(1);

        now.addAndGet(1_000_000_000L); // one second later: one new token
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();
    }

    @Test
    void neverHoldsMoreThanCapacity() {
        TokenBucket bucket = new TokenBucket(2, 10.0, now::get);
        now.addAndGet(60_000_000_000L);
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isTrue();
        assertThat(bucket.tryConsume()).isFalse();
    }

    @Test
    void filterReturns429WithRetryAfter() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(2, 0.5);
        int[] statuses = new int[3];
        String retryAfter = null;
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/books");
            request.setRemoteAddr("10.0.0.7");
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(request, response, new MockFilterChain());
            statuses[i] = response.getStatus();
            retryAfter = response.getHeader("Retry-After");
        }
        assertThat(statuses).containsExactly(200, 200, 429);
        assertThat(retryAfter).isEqualTo("2");
    }
}
