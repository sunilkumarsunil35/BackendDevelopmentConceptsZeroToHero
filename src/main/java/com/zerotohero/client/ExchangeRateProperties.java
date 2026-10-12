package com.zerotohero.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.exchange-rates")
public record ExchangeRateProperties(String baseUrl) {
}
