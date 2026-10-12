package com.zerotohero.async;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Module 15: switch on {@code @Async} and {@code @Scheduled}. With spring.threads.virtual.enabled
 * Spring Boot runs async tasks on virtual threads, so we don't size a thread pool here.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
}
