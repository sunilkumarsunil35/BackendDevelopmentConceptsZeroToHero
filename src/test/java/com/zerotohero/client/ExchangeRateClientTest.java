package com.zerotohero.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpServerErrorException;

/** Module 17: test an HTTP client against a fake server; no network involved. */
@RestClientTest(ExchangeRateClient.class)
class ExchangeRateClientTest {

    // Test slices don't bind @ConfigurationProperties records found by scanning; enable this one explicitly.
    @TestConfiguration
    @EnableConfigurationProperties(ExchangeRateProperties.class)
    static class Props {
    }

    @Autowired
    ExchangeRateClient client;

    @Autowired
    MockRestServiceServer server;

    @Test
    void readsTheRateFromJson() {
        server.expect(requestTo("https://api.frankfurter.app/latest?from=USD&to=INR"))
                .andRespond(withSuccess("{\"base\":\"USD\",\"rates\":{\"INR\":83.25}}", MediaType.APPLICATION_JSON));
        assertThat(client.rate("USD", "INR")).isEqualByComparingTo("83.25");
        server.verify();
    }

    @Test
    void serverErrorsBecomeExceptions() {
        server.expect(requestTo("https://api.frankfurter.app/latest?from=USD&to=EUR")).andRespond(withServerError());
        assertThatThrownBy(() -> client.rate("USD", "EUR")).isInstanceOf(HttpServerErrorException.class);
    }
}
