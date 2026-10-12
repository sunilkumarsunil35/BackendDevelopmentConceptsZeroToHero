package com.zerotohero.core;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Module 04: {@code @Bean} methods create objects you can't annotate yourself (like
 * {@link Clock}), and {@code @Scope} controls how many instances exist.
 */
@Configuration
public class BeanScopesConfig {

    /** Singleton (the default): one shared instance. Inject a Clock so tests can fix the time. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    /** Prototype: a new instance every time one is requested. */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public TaskCounter taskCounter() {
        return new TaskCounter();
    }

    public static class TaskCounter {
        private final AtomicInteger count = new AtomicInteger();

        public int increment() {
            return count.incrementAndGet();
        }
    }
}
