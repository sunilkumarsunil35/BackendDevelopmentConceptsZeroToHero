package com.zerotohero.client;

import com.zerotohero.book.BookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Module 17: GET /api/books/1/price?currency=INR converts the USD price using a remote API. */
@RestController
@Tag(name = "Books")
public class PriceController {

    private final BookService books;
    private final PriceConversionService conversions;

    public PriceController(BookService books, PriceConversionService conversions) {
        this.books = books;
        this.conversions = conversions;
    }

    @GetMapping("/api/books/{id}/price")
    public PriceConversionService.Conversion price(@PathVariable Long id, @RequestParam String currency) {
        return conversions.convert(books.findById(id).price(), "USD", currency.toUpperCase());
    }
}
