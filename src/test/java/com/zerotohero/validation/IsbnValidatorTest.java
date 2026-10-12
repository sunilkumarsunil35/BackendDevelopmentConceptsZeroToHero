package com.zerotohero.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Module 07/12: parameterized tests run one test body against many inputs. */
class IsbnValidatorTest {

    private final IsbnValidator validator = new IsbnValidator();

    @ParameterizedTest
    @ValueSource(strings = {"9780134685991", "978-0-13-468599-1", "9781449373320"})
    void acceptsValidIsbn13(String isbn) {
        assertThat(validator.isValid(isbn, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"9780134685990", "12345", "978013468599X", ""})
    void rejectsInvalidIsbn(String isbn) {
        assertThat(validator.isValid(isbn, null)).isFalse();
    }
}
