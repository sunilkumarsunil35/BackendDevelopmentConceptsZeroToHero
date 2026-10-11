package com.zerotohero.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.zerotohero.book.BookRequest;
import com.zerotohero.book.BookResponse;
import com.zerotohero.book.BookService;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

/** Module 14: watch the cache fill, refresh and empty as the service is used. */
@SpringBootTest
@ActiveProfiles("test")
class BookCachingTest {

    @Autowired
    BookService service;

    @Autowired
    CacheManager cacheManager;

    Cache cache;

    @BeforeEach
    void clear() {
        cache = cacheManager.getCache("books");
        cache.clear();
    }

    @Test
    void cacheableStoresTheResult() {
        assertThat(cache.get(6L)).isNull();
        BookResponse first = service.findById(6L);
        assertThat(cache.get(6L, BookResponse.class)).isEqualTo(first);
        assertThat(service.findById(6L)).isSameAs(cache.get(6L, BookResponse.class)); // served from the cache
    }

    @Test
    void cachePutRefreshesAndCacheEvictRemoves() {
        BookResponse original = service.findById(2L);
        BookResponse updated = service.update(2L, new BookRequest(original.title(), original.isbn(),
                new BigDecimal("33.00"), original.stock(), original.authorId()));
        assertThat(cache.get(2L, BookResponse.class)).isEqualTo(updated);
        assertThat(cache.get(2L, BookResponse.class).price()).isEqualByComparingTo("33.00");
        assertThat(updated.version()).isEqualTo(original.version() + 1);
    }
}
