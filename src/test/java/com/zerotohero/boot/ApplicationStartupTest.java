package com.zerotohero.boot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Module 05: the whole application starts, profiles apply, and configuration is readable. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationStartupTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    MockMvc mvc;

    @Test
    void contextLoadsAndProfileSpecificBeansAreSkipped() {
        assertThat(context.getBeansOfType(StartupLogger.class)).as("@Profile(\"!test\")").isEmpty();
    }

    @Test
    void infoEndpointShowsNameAndProfile() throws Exception {
        mvc.perform(get("/api/info"))
                .andExpect(jsonPath("$.name").value("backend-zero-to-hero"))
                .andExpect(jsonPath("$.activeProfiles[0]").value("test"));
    }
}
