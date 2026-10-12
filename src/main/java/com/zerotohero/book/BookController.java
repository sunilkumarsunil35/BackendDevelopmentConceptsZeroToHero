package com.zerotohero.book;

import com.zerotohero.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Module 06: a full CRUD resource.
 * <pre>
 * GET    /api/books?title=java&page=0&size=5&sort=price,desc   list (paged, filtered, sorted)
 * GET    /api/books/{id}                                       one book
 * POST   /api/books                                            create  (201 + Location header, ADMIN)
 * PUT    /api/books/{id}                                       replace (ADMIN)
 * DELETE /api/books/{id}                                       delete  (204, ADMIN)
 * </pre>
 */
@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "The book catalog")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Search books by title, with paging and sorting")
    public PageResponse<BookResponse> list(@RequestParam(required = false) String title,
                                           @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC)
                                           Pageable pageable) {
        return service.search(title, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one book")
    public BookResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Add a book", security = @SecurityRequirement(name = "bearer"))
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        BookResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace a book", security = @SecurityRequirement(name = "bearer"))
    public BookResponse update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return service.update(id, request);
    }

    /** Module 13: method security. The URL rules already require ADMIN; this is defense in depth. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a book", security = @SecurityRequirement(name = "bearer"))
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
