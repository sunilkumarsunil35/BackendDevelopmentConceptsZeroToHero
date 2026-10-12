package com.zerotohero.book;

import com.zerotohero.aop.LogExecutionTime;
import com.zerotohero.error.ConflictException;
import com.zerotohero.error.NotFoundException;
import com.zerotohero.web.PageResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Modules 09, 10, 14 and 16: business logic for the catalog. Controllers stay thin and call this.
 * The class is read-only by default; methods that write override it.
 */
@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository books;
    private final AuthorRepository authors;

    public BookService(BookRepository books, AuthorRepository authors) {
        this.books = books;
        this.authors = authors;
    }

    @LogExecutionTime
    public PageResponse<BookResponse> search(String title, Pageable pageable) {
        var page = (title == null || title.isBlank())
                ? books.findAllBy(pageable)
                : books.findByTitleContainingIgnoreCase(title.trim(), pageable);
        return PageResponse.of(page, BookResponse::from);
    }

    /** Module 14: the first call hits the database; later calls for the same id come from the cache. */
    @Cacheable(cacheNames = "books", key = "#id")
    public BookResponse findById(Long id) {
        return BookResponse.from(load(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        String isbn = normalize(request.isbn());
        if (books.existsByIsbn(isbn)) {
            throw new ConflictException("A book with ISBN " + isbn + " already exists");
        }
        Book book = new Book(request.title().trim(), isbn, request.price(), request.stock(), author(request.authorId()));
        return BookResponse.from(books.save(book));
    }

    /** Module 14: refresh the cached copy with the new value. */
    @Transactional
    @CachePut(cacheNames = "books", key = "#id")
    public BookResponse update(Long id, BookRequest request) {
        Book book = load(id);
        String isbn = normalize(request.isbn());
        books.findByIsbn(isbn).filter(other -> !other.getId().equals(id)).ifPresent(other -> {
            throw new ConflictException("A book with ISBN " + isbn + " already exists");
        });
        book.update(request.title().trim(), isbn, request.price(), request.stock(), author(request.authorId()));
        // No save() call needed: the entity is managed, so Hibernate flushes the change at commit
        // (dirty checking). We flush here so the new @Version is in the response.
        books.flush();
        return BookResponse.from(book);
    }

    @Transactional
    @CacheEvict(cacheNames = "books", key = "#id")
    public void delete(Long id) {
        books.delete(load(id));
    }

    private Book load(Long id) {
        return books.findById(id).orElseThrow(() -> new NotFoundException("Book", id));
    }

    private Author author(Long id) {
        return authors.findById(id).orElseThrow(() -> new NotFoundException("Author", id));
    }

    static String normalize(String isbn) {
        return isbn.replace("-", "").replace(" ", "");
    }
}
