package com.zerotohero.book;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Module 09: turn on auditing so {@code @CreatedDate} and {@code @LastModifiedDate} are filled in. */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
