package com.zerotohero.book;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Modules 09, 12 and 23: @DataJpaTest starts JPA, Flyway and an in-memory database only. Each test
 * runs in a transaction that is rolled back afterwards, so tests don't affect each other.
 */
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import(JpaConfig.class)
class BookRepositoryTest {

    @Autowired
    BookRepository books;

    @Autowired
    AuthorRepository authors;

    @Autowired
    EntityManager em;

    @Autowired
    EntityManagerFactory emf;

    Statistics stats;

    @BeforeEach
    void resetStatistics() {
        stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
    }

    @Test
    void derivedQueries() {
        assertThat(books.findByIsbn("9780134685991")).get().extracting(Book::getTitle).isEqualTo("Effective Java");
        assertThat(books.existsByIsbn("0000000000000")).isFalse();
        assertThat(books.countByAuthorId(1L)).isEqualTo(1);
    }

    @Test
    void pagingAndSorting() {
        var page = books.findByTitleContainingIgnoreCase("in", PageRequest.of(0, 2, Sort.by("price").descending()));
        // "in" matches Designing, Spring in Action, Concurrency in Practice, Domain, Refactoring, Building
        assertThat(page.getTotalElements()).isEqualTo(6);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.getContent()).extracting(Book::getTitle)
                .containsExactly("Domain-Driven Design", "Spring in Action");
    }

    @Test
    void projectionsSelectOnlyWhatYouNeed() {
        List<BookSummary> cheap = books.findByPriceLessThanOrderByPriceAsc(new BigDecimal("42"));
        assertThat(cheap).extracting(BookSummary::getTitle).containsExactly("Clean Code", "Release It!");
    }

    @Test
    void customJpqlQuery() {
        assertThat(books.findLowStock(3)).extracting(Book::getTitle).containsExactly("Release It!", "Java Concurrency in Practice");
    }

    @Test
    void auditingFillsTimestampsAndVersionStartsAtZero() {
        Author author = authors.save(new Author("Kent Beck"));
        Book saved = books.saveAndFlush(new Book("TDD by Example", "9780321146533", new BigDecimal("40"), 3, author));
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getVersion()).isZero();
    }

    @Test
    void nPlusOneProblemAndTheFetchJoinFix() {
        // Naive: 1 query for books + 1 query per author touched = N+1.
        em.clear();
        books.findAll().forEach(b -> b.getAuthor().getName());
        long naive = stats.getPrepareStatementCount();

        em.clear();
        stats.clear();
        books.findAllWithAuthor().forEach(b -> b.getAuthor().getName());
        long fetchJoin = stats.getPrepareStatementCount();

        assertThat(naive).isEqualTo(11); // 1 + 10 authors
        assertThat(fetchJoin).isEqualTo(1);
    }
}
