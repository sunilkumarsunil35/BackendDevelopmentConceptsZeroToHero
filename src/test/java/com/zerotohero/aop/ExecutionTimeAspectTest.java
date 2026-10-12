package com.zerotohero.aop;

import static org.assertj.core.api.Assertions.assertThat;

import com.zerotohero.book.BookService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

/** Module 16: the aspect runs around the annotated method and records a timer. */
@SpringBootTest
@ActiveProfiles("test")
class ExecutionTimeAspectTest {

    @Autowired
    BookService service;

    @Autowired
    MeterRegistry registry;

    @Test
    void theServiceIsAProxyAndCallsAreTimed() {
        assertThat(AopUtils.isAopProxy(service)).as("Spring wrapped the bean in a proxy").isTrue();

        service.search("java", PageRequest.of(0, 5));
        service.search(null, PageRequest.of(0, 5));

        Timer timer = registry.find("bookstore.method.time").tag("method", "BookService.search(..)")
                .tag("outcome", "success").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isGreaterThanOrEqualTo(2);
    }
}
