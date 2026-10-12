package com.zerotohero.book;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Module 09: Spring Data writes the implementation. Method names become queries; {@code @Query}
 * handles anything the naming convention can't express.
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    // Derived query: SELECT ... WHERE isbn = ?
    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    // Derived query with paging. @EntityGraph loads the author in the same SQL query (no N+1).
    @EntityGraph(attributePaths = "author")
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Book> findAllBy(Pageable pageable);

    // JPQL with a fetch join: one query for books and their authors.
    @Query("select b from Book b join fetch b.author order by b.id")
    List<Book> findAllWithAuthor();

    // JPQL with a named parameter.
    @Query("select b from Book b where b.stock < :threshold order by b.stock")
    List<Book> findLowStock(@Param("threshold") int threshold);

    // Projection: only title and price are selected.
    List<BookSummary> findByPriceLessThanOrderByPriceAsc(BigDecimal max);

    long countByAuthorId(Long authorId);
}
