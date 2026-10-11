package com.zerotohero.error;

import com.zerotohero.book.InsufficientStockException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Module 07: one place that turns exceptions into consistent error responses. Every error uses
 * RFC 9457 "problem details": {@code {"type", "title", "status", "detail", ...}}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, "Not found", e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail conflict(ConflictException e) {
        return problem(HttpStatus.CONFLICT, "Conflict", e.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail insufficientStock(InsufficientStockException e) {
        return problem(HttpStatus.CONFLICT, "Insufficient stock", e.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail staleUpdate(OptimisticLockingFailureException e) {
        return problem(HttpStatus.CONFLICT, "Concurrent update",
                "Someone else changed this record. Reload it and try again.");
    }

    /** A database constraint failed, e.g. deleting a book that is part of an order. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail constraintViolated(DataIntegrityViolationException e) {
        return problem(HttpStatus.CONFLICT, "Conflict", "The change conflicts with existing data");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail badArgument(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, "Bad request", e.getMessage());
    }

    /** Bean Validation failures: list every invalid field so the client can fix them all at once. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalid(MethodArgumentNotValidException e) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation failed", "One or more fields are invalid");
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        pd.setProperty("errors", errors);
        return pd;
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        return pd;
    }
}
