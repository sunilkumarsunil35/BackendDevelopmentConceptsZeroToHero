# 06 · Building REST APIs with Spring MVC

> **Phase:** Building APIs · **Week:** 3 · **Code:** [`web/HelloController.java`](../src/main/java/com/zerotohero/web/HelloController.java), [`book/BookController.java`](../src/main/java/com/zerotohero/book/BookController.java) · **Tests:** [`HelloControllerTest.java`](../src/test/java/com/zerotohero/web/HelloControllerTest.java), [`BookControllerWebTest.java`](../src/test/java/com/zerotohero/book/BookControllerWebTest.java)

Spring MVC turns HTTP requests into method calls. A request arrives at the `DispatcherServlet`,
which finds the matching handler method, converts the inputs, calls it, and writes the return
value as JSON.

## Concepts

### @RestController and request mapping
**Level:** Beginner · **Code:** [HelloController.java](../src/main/java/com/zerotohero/web/HelloController.java)

`@RestController` = `@Controller` + `@ResponseBody` (return values are written to the body).
`@RequestMapping("/api/hello")` on the class sets a prefix; `@GetMapping`, `@PostMapping`,
`@PutMapping`, `@PatchMapping`, `@DeleteMapping` on methods pick the HTTP method and path.

### Reading input: path variables, query params, headers
**Level:** Beginner · **Code:** [HelloController.java](../src/main/java/com/zerotohero/web/HelloController.java)

`@PathVariable` for identity (`/books/{id}`), `@RequestParam` for options (`?name=Asha`, with
`defaultValue` or `required = false`), `@RequestHeader` for headers. Spring converts strings to the
declared type and returns 400 if it can't.

### Request bodies and JSON mapping
**Level:** Beginner · **Code:** [BookRequest.java](../src/main/java/com/zerotohero/book/BookRequest.java)

`@RequestBody BookRequest request` deserializes JSON into a record using Jackson. Unknown fields
are ignored by default; malformed JSON gives 400. Use records for request and response bodies.

### Responses: status codes, headers and ResponseEntity
**Level:** Beginner · **Code:** [BookController.java](../src/main/java/com/zerotohero/book/BookController.java)

Return an object for 200, use `@ResponseStatus(HttpStatus.CREATED)` for a fixed status, or return
`ResponseEntity` for full control. `BookController.create` returns 201 with a `Location` header
built by `ServletUriComponentsBuilder`; `delete` returns 204 No Content.

### DTOs: never expose entities
**Level:** Beginner · **Code:** [BookResponse.java](../src/main/java/com/zerotohero/book/BookResponse.java)

Returning JPA entities leaks your database design, can trigger lazy-loading errors and infinite JSON
recursion (Author → books → author ...), and lets clients set fields they shouldn't (mass
assignment). Map entities to response records at the boundary, as `BookResponse.from` does.

### Layered architecture: controller, service, repository
**Level:** Beginner · **Code:** [BookService.java](../src/main/java/com/zerotohero/book/BookService.java)

Controllers translate HTTP to method calls and back; services hold business rules and transactions;
repositories talk to the database. Keep controllers thin: no business logic, no repository calls.
This keeps logic testable without HTTP.

### Pagination, sorting and filtering
**Level:** Intermediate · **Code:** [BookController.java](../src/main/java/com/zerotohero/book/BookController.java)

Accept a `Pageable` parameter and Spring fills it from `?page=0&size=20&sort=price,desc`.
`@PageableDefault` sets defaults. Return a stable page shape such as
[`PageResponse`](../src/main/java/com/zerotohero/web/PageResponse.java) rather than serializing
Spring's `Page` implementation. Always cap `size` (`spring.data.web.pageable.max-page-size`).

### Content negotiation and message converters
**Level:** Intermediate

`HttpMessageConverter`s turn bodies into objects and back. The `Accept` header picks the response
format; `produces`/`consumes` on a mapping restrict it. Customise Jackson with
`spring.jackson.*` properties or a `Jackson2ObjectMapperBuilderCustomizer` bean (dates, naming,
null handling).

### Filters and interceptors
**Level:** Intermediate · **Code:** [CorrelationIdFilter.java](../src/main/java/com/zerotohero/observability/CorrelationIdFilter.java)

A servlet `Filter` sees every request before Spring MVC (security, correlation ids, rate limiting).
A `HandlerInterceptor` runs around controller methods and knows which handler was chosen. Use a
filter for cross-cutting HTTP concerns, an interceptor when you need the handler.

## Practice

### Build an authors resource
**Level:** Beginner

Add `AuthorController` with `GET /api/authors`, `GET /api/authors/{id}` (including the number of
books, using `BookRepository.countByAuthorId`) and `POST /api/authors`. Write `@WebMvcTest` tests.

### Add PATCH for price changes
**Level:** Intermediate

Add `PATCH /api/books/{id}/price` that takes `{"price": 39.99}` and changes only the price. Decide
which status to return and what happens if the book doesn't exist.

### Add filtering by price range
**Level:** Intermediate

Support `GET /api/books?minPrice=20&maxPrice=50` together with title search and paging. Add the
repository method and a test.

## Interview questions

- What does the `DispatcherServlet` do?
- Why should controllers return DTOs rather than entities?
- How does Spring know which `HttpMessageConverter` to use?
- Filter vs interceptor vs aspect: when would you use each?

## Further reading

- [Spring Framework: Spring Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Spring Boot: Servlet web applications](https://docs.spring.io/spring-boot/reference/web/servlet.html)
