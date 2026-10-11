# 07 · Validation and error handling

> **Phase:** Building APIs · **Week:** 4 · **Code:** [`validation/`](../src/main/java/com/zerotohero/validation), [`error/GlobalExceptionHandler.java`](../src/main/java/com/zerotohero/error/GlobalExceptionHandler.java) · **Tests:** [`BookControllerWebTest.java`](../src/test/java/com/zerotohero/book/BookControllerWebTest.java), [`IsbnValidatorTest.java`](../src/test/java/com/zerotohero/validation/IsbnValidatorTest.java)

Never trust input. Validate it at the edge, reject bad requests with a clear 400, and turn every
failure into a consistent, documented error format so clients can handle it.

## Concepts

### Bean Validation annotations
**Level:** Beginner · **Code:** [BookRequest.java](../src/main/java/com/zerotohero/book/BookRequest.java)

`@NotNull`, `@NotBlank`, `@Size`, `@Min`/`@Max`, `@Positive`, `@PositiveOrZero`, `@DecimalMin`,
`@Digits`, `@Email`, `@Pattern`, `@Past`/`@Future`. Put them on request records and add `@Valid`
to the `@RequestBody` parameter. `@NotBlank` is for strings, `@NotEmpty` for collections.

### Nested and collection validation
**Level:** Intermediate · **Code:** [OrderDtos.java](../src/main/java/com/zerotohero/order/OrderDtos.java)

Validation doesn't cascade unless you ask: `List<@Valid OrderLine> items` validates every line.
Combine with `@NotEmpty` and `@Size(max = 20)` to bound the list.

### Custom constraints
**Level:** Intermediate · **Code:** [IsbnValidator.java](../src/main/java/com/zerotohero/validation/IsbnValidator.java)

Create an annotation with `@Constraint(validatedBy = ...)` and a `ConstraintValidator` class.
`@Isbn` checks the ISBN-13 check digit. Return `true` for `null` and let `@NotBlank` handle
missing values, so each constraint does one job.

### Validating parameters and configuration
**Level:** Intermediate

Add `@Validated` to a controller or service class to validate method parameters
(`@RequestParam @Min(1) int size`); violations throw `ConstraintViolationException`. `@Validated`
on `@ConfigurationProperties` validates configuration at startup (module 04).

### Centralised handling with @RestControllerAdvice
**Level:** Beginner · **Code:** [GlobalExceptionHandler.java](../src/main/java/com/zerotohero/error/GlobalExceptionHandler.java)

One `@RestControllerAdvice` class maps exceptions to responses for every controller: not found →
404, duplicate ISBN → 409, validation → 400 with every bad field listed. Services throw meaningful
exceptions (`NotFoundException`, `ConflictException`) and never build HTTP responses themselves.

### Problem Details (RFC 9457)
**Level:** Intermediate · **Code:** [GlobalExceptionHandler.java](../src/main/java/com/zerotohero/error/GlobalExceptionHandler.java)

Spring's `ProblemDetail` produces the standard error body
`{"type","title","status","detail","instance"}` with content type `application/problem+json`, and
you can add properties such as `errors`. A standard shape means clients and gateways can handle
errors generically.

### What not to leak
**Level:** Intermediate

Never return stack traces, SQL, or internal class names to clients. Log the full exception with the
correlation id on the server and return a generic message. Keep `server.error.include-stacktrace=never`
(the default) in production.

## Practice

### Validate a date range
**Level:** Intermediate

Create a class-level constraint `@ValidDateRange` for a `ReportRequest(LocalDate from, LocalDate to)`
that fails when `from` is after `to`, and report the error on the `to` field.

### Handle malformed JSON
**Level:** Beginner

Send `{"title": ` (broken JSON) to `POST /api/books`. Add a handler for
`HttpMessageNotReadableException` that returns a ProblemDetail with title "Malformed JSON" and test it.

### Validation groups
**Level:** Advanced

Use validation groups so that `id` must be null on create but present on update, with one DTO.
Then decide whether two DTOs would be simpler, and write down why.

## Interview questions

- Where should validation happen: controller, service, entity or database? (Answer: at each layer for a different reason.)
- How do you return all validation errors at once instead of the first?
- What is RFC 9457 and why use it?
- Why should a custom validator return `true` for null?

## Further reading

- [Jakarta Bean Validation specification](https://jakarta.ee/specifications/bean-validation/)
- [Spring Framework: Error responses](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html)
- [RFC 9457: Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457)
