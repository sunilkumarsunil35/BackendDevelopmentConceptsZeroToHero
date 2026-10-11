package com.zerotohero;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Helpers shared by the full-application tests. */
public final class TestSupport {

    private TestSupport() {
    }

    /** Log in through the real endpoint and return the JWT. */
    public static String token(MockMvc mvc, String username, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    public static String bearer(String token) {
        return "Bearer " + token;
    }
}
