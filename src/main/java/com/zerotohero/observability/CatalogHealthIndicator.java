package com.zerotohero.observability;

import com.zerotohero.book.BookRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/** Module 18: a custom health check, shown under "catalog" at /actuator/health. */
@Component("catalog")
public class CatalogHealthIndicator implements HealthIndicator {

    private final BookRepository books;

    public CatalogHealthIndicator(BookRepository books) {
        this.books = books;
    }

    @Override
    public Health health() {
        long count = books.count();
        return count > 0
                ? Health.up().withDetail("books", count).build()
                : Health.down().withDetail("reason", "catalog is empty").build();
    }
}
