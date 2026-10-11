package com.zerotohero.async;

import static org.assertj.core.api.Assertions.assertThat;

import com.zerotohero.book.BookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** Module 15: test a scheduled job by calling its method directly; don't wait for the clock. */
@DataJpaTest
class LowStockReporterTest {

    @Autowired
    BookRepository books;

    @Test
    void reportsBooksBelowTheThreshold() {
        LowStockReporter reporter = new LowStockReporter(books, 3);
        assertThat(reporter.report()).containsExactly("Release It! (1 left)", "Java Concurrency in Practice (2 left)");
    }
}
