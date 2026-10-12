package com.zerotohero.book;

/** Module 10: an unchecked exception, so {@code @Transactional} rolls back by default. */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(Long bookId, String title, int available, int requested) {
        super("Only " + available + " copies of '" + title + "' (id " + bookId + ") left, " + requested + " requested");
    }
}
