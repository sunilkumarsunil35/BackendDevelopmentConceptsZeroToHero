package com.zerotohero.core;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Module 04/05: type-safe configuration. Binds {@code app.greeting.*} from application.yml,
 * environment variables or command-line flags, and fails at startup if prefix is blank.
 */
@Validated
@ConfigurationProperties("app.greeting")
public record GreetingProperties(@NotBlank @DefaultValue("Hello") String prefix,
                                 @DefaultValue("true") boolean exclaim) {
}
