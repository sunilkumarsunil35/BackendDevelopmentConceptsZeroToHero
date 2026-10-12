package com.zerotohero.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Module 07: checks the ISBN-13 check digit. Weights alternate 1 and 3; the sum must be a multiple of 10. */
public class IsbnValidator implements ConstraintValidator<Isbn, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // let @NotBlank decide about missing values
        String digits = value.replace("-", "").replace(" ", "");
        if (!digits.matches("\\d{13}")) return false;
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += (digits.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        }
        return sum % 10 == 0;
    }
}
