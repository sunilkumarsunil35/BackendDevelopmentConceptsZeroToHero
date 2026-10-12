package com.zerotohero.security;

import static com.zerotohero.TestSupport.bearer;
import static com.zerotohero.TestSupport.token;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Module 13: the whole security chain, end to end, with real JWTs. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    MockMvc mvc;

    private static final String NEW_BOOK = """
            {"title":"Clean Architecture","isbn":"9780134494166","price":39.99,"stock":5,"authorId":2}
            """;

    @Test
    void wrongPasswordIs401() throws Exception {
        mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Wrong username or password"));
    }

    @Test
    void readingIsPublic() throws Exception {
        mvc.perform(get("/api/books/1")).andExpect(status().isOk());
    }

    @Test
    void ordersNeedAToken() throws Exception {
        mvc.perform(get("/api/orders")).andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", org.hamcrest.Matchers.startsWith("Bearer")));
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        String jwt = token(mvc, "reader", "reader-pass");
        String tampered = jwt.substring(0, jwt.length() - 2) + (jwt.endsWith("AA") ? "BB" : "AA");
        mvc.perform(get("/api/orders").header("Authorization", bearer(tampered))).andExpect(status().isUnauthorized());
    }

    @Test
    void readerCanOrderButNotEditTheCatalog() throws Exception {
        String jwt = token(mvc, "reader", "reader-pass");
        mvc.perform(post("/api/books").header("Authorization", bearer(jwt))
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_BOOK))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/orders").header("Authorization", bearer(jwt)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"bookId\":10,\"quantity\":1}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customer").value("reader"));
    }

    @Test
    void adminCanAddBooks() throws Exception {
        String jwt = token(mvc, "admin", "admin-pass");
        mvc.perform(post("/api/books").header("Authorization", bearer(jwt))
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_BOOK))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value("9780134494166"));
        mvc.perform(post("/api/books").header("Authorization", bearer(jwt))
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_BOOK))
                .andExpect(status().isConflict());
    }

    @Test
    void usersOnlySeeTheirOwnOrders() throws Exception {
        String reader = token(mvc, "reader", "reader-pass");
        String admin = token(mvc, "admin", "admin-pass");
        String body = mvc.perform(post("/api/orders").header("Authorization", bearer(reader))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"items\":[{\"bookId\":10,\"quantity\":1}]}"))
                .andReturn().getResponse().getContentAsString();
        Integer id = com.jayway.jsonpath.JsonPath.read(body, "$.id");
        mvc.perform(get("/api/orders/" + id).header("Authorization", bearer(reader))).andExpect(status().isOk());
        mvc.perform(get("/api/orders/" + id).header("Authorization", bearer(admin))).andExpect(status().isNotFound());
    }
}
