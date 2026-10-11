package com.zerotohero.client;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Module 17: call another HTTP service with {@link RestClient}, Spring's modern synchronous client.
 * The builder comes from Spring Boot, so it shares Jackson settings, metrics and tracing.
 */
@Component
public class ExchangeRateClient {

    private final RestClient restClient;

    public ExchangeRateClient(RestClient.Builder builder, ExchangeRateProperties props) {
        this.restClient = builder.baseUrl(props.baseUrl()).build();
    }

    /** The shape of the remote JSON: {"base":"USD","rates":{"INR":83.1}} */
    public record RatesResponse(String base, Map<String, BigDecimal> rates) {
    }

    public BigDecimal rate(String from, String to) {
        RatesResponse response = restClient.get()
                .uri("/latest?from={from}&to={to}", from, to)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                // 4xx/5xx become RestClientResponseException by default; customise with onStatus(...)
                .body(RatesResponse.class);
        if (response == null || response.rates() == null || !response.rates().containsKey(to)) {
            throw new IllegalStateException("No rate for " + from + " -> " + to);
        }
        return response.rates().get(to);
    }
}
