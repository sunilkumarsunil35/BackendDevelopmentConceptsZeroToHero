package com.zerotohero.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zerotohero.core.GreetingService;
import com.zerotohero.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Module 06/12: @WebMvcTest starts only the web layer; the service is a Mockito mock. */
@WebMvcTest(HelloController.class)
@Import(SecurityConfig.class)
class HelloControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    GreetingService greetingService;

    @Test
    void queryParameter() throws Exception {
        when(greetingService.greet("Asha")).thenReturn("Hello, Asha!");
        mvc.perform(get("/api/hello").param("name", "Asha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Asha!"));
    }

    @Test
    void pathVariable() throws Exception {
        when(greetingService.greet("Ravi")).thenReturn("Hello, Ravi!");
        mvc.perform(get("/api/hello/Ravi")).andExpect(jsonPath("$.message").value("Hello, Ravi!"));
    }

    @Test
    void requestHeader() throws Exception {
        when(greetingService.greetInHindi("World")).thenReturn("Namaste, World!");
        mvc.perform(get("/api/hello/lang").header("Accept-Language", "hi-IN"))
                .andExpect(jsonPath("$.message").value("Namaste, World!"));
    }

    @Test
    void requestBodyAndStatus() throws Exception {
        mvc.perform(post("/api/hello/echo").contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"ping\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("ping"));
    }

    @Test
    void responseEntity() throws Exception {
        mvc.perform(get("/api/hello/teapot"))
                .andExpect(status().isIAmATeapot())
                .andExpect(header().string("X-Course", "zero-to-hero"));
    }
}
