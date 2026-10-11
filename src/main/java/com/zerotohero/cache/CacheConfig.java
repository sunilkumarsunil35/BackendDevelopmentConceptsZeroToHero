package com.zerotohero.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Module 14: switch on Spring's cache abstraction. The provider (Caffeine) and its size and
 * expiry come from spring.cache.* in application.yml. Swapping to Redis is a dependency and a
 * property change; the {@code @Cacheable} code stays the same.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
