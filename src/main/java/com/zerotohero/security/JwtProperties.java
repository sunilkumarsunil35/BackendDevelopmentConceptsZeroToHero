package com.zerotohero.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Module 13: JWT settings. HS256 needs a secret of at least 32 bytes (256 bits). */
@Validated
@ConfigurationProperties("app.jwt")
public record JwtProperties(@NotBlank @Size(min = 32) String secret, Duration ttl, String issuer) {
}
