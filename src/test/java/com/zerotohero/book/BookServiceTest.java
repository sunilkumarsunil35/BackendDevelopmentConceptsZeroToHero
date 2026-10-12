package com.zerotohero.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zerotohero.error.ConflictException;
import com.zerotohero.error.NotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Module 12: a fast unit test. Repositories are Mockito mocks; no Spring, no database. */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    BookRepository books;

    @Mock
    AuthorRepository authors;

    @InjectMocks
    BookService service;

    private final BookRequest request = new BookRequest(" Clean Architecture ", "978-0-13-449416-6",
            new BigDecimal("39.99"), 5, 2L);

    @Test
    void createsABookWithANormalizedIsbn() {
        Author author = new Author("Robert C. Martin");
        when(books.existsByIsbn("9780134494166")).thenReturn(false);
        when(authors.findById(2L)).thenReturn(Optional.of(author));
        when(books.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BookResponse created = service.create(request);

        assertThat(created.title()).isEqualTo("Clean Architecture");
        assertThat(created.isbn()).isEqualTo("9780134494166");
        assertThat(created.authorName()).isEqualTo("Robert C. Martin");
    }

    @Test
    void rejectsADuplicateIsbn() {
        when(books.existsByIsbn("9780134494166")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ConflictException.class);
        verify(books, never()).save(any());
    }

    @Test
    void unknownAuthorIsNotFound() {
        when(authors.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Author 2 was not found");
    }

    @Test
    void stockCannotGoNegative() {
        Book book = new Book("Release It!", "9781680502398", BigDecimal.TEN, 1, new Author("Michael T. Nygard"));
        book.removeStock(1);
        assertThat(book.getStock()).isZero();
        assertThatThrownBy(() -> book.removeStock(1)).isInstanceOf(InsufficientStockException.class);
    }
}
