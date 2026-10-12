package com.zerotohero.book;

import com.zerotohero.validation.Isbn;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Module 07: a request DTO. Validation rules live on the input, not on the entity. */
public record BookRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Isbn String isbn,
        @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal price,
        @PositiveOrZero int stock,
        @NotNull Long authorId) {
}
