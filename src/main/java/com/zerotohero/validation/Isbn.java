package com.zerotohero.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Module 07: a custom constraint. Use it like any built-in one: {@code @Isbn String isbn}. */
@Documented
@Constraint(validatedBy = IsbnValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Isbn {
    String message() default "must be a valid ISBN-13";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
