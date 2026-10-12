package com.zerotohero.observability;

import static com.zerotohero.TestSupport.bearer;
import static com.zerotohero.TestSupport.token;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Module 18: correlation ids, health checks and metrics. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ObservabilityTest {

    @Autowired
    MockMvc mvc;

    @Test
    void everyResponseCarriesACorrelationId() throws Exception {
        mvc.perform(get("/api/info"))
                .andExpect(header().string(CorrelationIdFilter.HEADER, matchesPattern("[0-9a-f-]{36}")));
        mvc.perform(get("/api/info").header(CorrelationIdFilter.HEADER, "client-supplied-123"))
                .andExpect(header().string(CorrelationIdFilter.HEADER, "client-supplied-123"));
    }

    @Test
    void healthIncludesTheCustomCatalogCheck() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.catalog.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"));
        mvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());
    }

    @Test
    void metricsAreAdminOnly() throws Exception {
        mvc.perform(get("/actuator/metrics")).andExpect(status().isUnauthorized());
        String admin = token(mvc, "admin", "admin-pass");
        mvc.perform(get("/actuator/metrics/jvm.memory.used").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("jvm.memory.used"));
    }
}
