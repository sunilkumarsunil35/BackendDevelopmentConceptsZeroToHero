package com.zerotohero.book;

import java.math.BigDecimal;

/** Module 06: a response DTO. Never return entities from controllers. */
public record BookResponse(Long id, String title, String isbn, BigDecimal price, int stock,
                           Long authorId, String authorName, long version) {

    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getIsbn(), book.getPrice(), book.getStock(),
                book.getAuthor().getId(), book.getAuthor().getName(), book.getVersion());
    }
}
