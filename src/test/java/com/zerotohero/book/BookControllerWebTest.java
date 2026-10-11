package com.zerotohero.book;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zerotohero.error.NotFoundException;
import com.zerotohero.security.SecurityConfig;
import com.zerotohero.web.PageResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Modules 06, 07, 12, 13: the HTTP contract of /api/books, with the service mocked out. */
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerWebTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    BookService service;

    private static final BookResponse EFFECTIVE_JAVA = new BookResponse(1L, "Effective Java", "9780134685991",
            new BigDecimal("45.99"), 12, 1L, "Joshua Bloch", 0);

    private static final String VALID_BODY = """
            {"title":"Effective Java","isbn":"9780134685991","price":45.99,"stock":12,"authorId":1}
            """;

    @Test
    void listIsPublicAndPaged() throws Exception {
        when(service.search(eq("java"), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(EFFECTIVE_JAVA), 0, 20, 1, 1));
        mvc.perform(get("/api/books").param("title", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Effective Java"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void missingBookIs404ProblemDetail() throws Exception {
        when(service.findById(99L)).thenThrow(new NotFoundException("Book", 99L));
        mvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.title").value("Not found"))
                .andExpect(jsonPath("$.detail").value("Book 99 was not found"));
    }

    @Test
    void creatingRequiresAuthentication() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "USER")
    void creatingRequiresAdmin() throws Exception {
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReturns201WithLocation() throws Exception {
        when(service.create(any())).thenReturn(EFFECTIVE_JAVA);
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/books/1"))
                .andExpect(jsonPath("$.authorName").value("Joshua Bloch"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidBodyListsEveryBadField() throws Exception {
        String body = """
                {"title":"","isbn":"9780134685990","price":-1,"stock":-5}
                """;
        mvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.title").exists())
                .andExpect(jsonPath("$.errors.isbn").value("must be a valid ISBN-13"))
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.stock").exists())
                .andExpect(jsonPath("$.errors.authorId").exists());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/books/1")).andExpect(status().isNoContent());
        verify(service).delete(1L);
    }
}
