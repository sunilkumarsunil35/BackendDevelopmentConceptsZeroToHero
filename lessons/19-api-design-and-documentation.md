# 19 · API design and documentation

> **Phase:** Production-ready · **Week:** 9 · **Code:** [`docs/OpenApiConfig.java`](../src/main/java/com/zerotohero/docs/OpenApiConfig.java), [`order/OrderService.java`](../src/main/java/com/zerotohero/order/OrderService.java) · **Tests:** [`OpenApiTest.java`](../src/test/java/com/zerotohero/docs/OpenApiTest.java)

An API is a product for other developers. Good APIs are predictable, documented, safe to retry,
and evolve without breaking clients. Run the app and open `http://localhost:8080/swagger-ui.html`.

## Concepts

### OpenAPI and springdoc
**Level:** Beginner · **Code:** [OpenApiConfig.java](../src/main/java/com/zerotohero/docs/OpenApiConfig.java)

OpenAPI is the standard machine-readable API description. springdoc generates it from your
controllers at `/v3/api-docs` and serves Swagger UI. Enrich it with `@Tag`, `@Operation`, and a
bearer security scheme so the UI can send tokens. Clients can generate SDKs from the spec.

### Consistent conventions
**Level:** Beginner

Decide once and apply everywhere: plural nouns, camelCase JSON, ISO-8601 UTC timestamps, one
error format (Problem Details), one paging shape (`PageResponse`), one way to filter and sort.
Consistency matters more than any particular choice.

### Idempotency keys
**Level:** Intermediate · **Code:** [OrderService.java](../src/main/java/com/zerotohero/order/OrderService.java)

If a client's POST times out, did the order go through? With an `Idempotency-Key` header the client
can safely retry: the server stores the key with the result and returns the same order for a
repeat. The key row is written in the same transaction as the order, and a unique key constraint
protects against two simultaneous retries. Test: `sameIdempotencyKeyPlacesTheOrderOnce`.

### Versioning and evolution
**Level:** Intermediate

Adding optional fields and new endpoints is backward compatible; removing or renaming fields, or
changing types or meaning, is not. Prefer evolving without versions; when you must break, version
the URL (`/api/v2/books`) or a header, run both versions, and announce a deprecation date
(`Deprecation` and `Sunset` headers). Spring Framework 7 adds first-class API versioning support.

### Pagination styles
**Level:** Intermediate

Offset (`page`, `size`) is simple and supports "jump to page 5" but slows down and can skip or
repeat rows when data changes. Cursor/keyset (`after=<opaque cursor>`) is stable and fast for
infinite scroll. Always cap page size.

### Long-running operations
**Level:** Advanced

If work takes longer than a request should (report generation, video processing), return
`202 Accepted` with a `Location` to a status resource (`/api/reports/42`) that the client polls, or
notify with a webhook. Don't hold a connection open for minutes.

### REST, GraphQL and gRPC
**Level:** Advanced

REST suits resource-oriented public APIs with HTTP caching. GraphQL (Spring for GraphQL) lets
clients ask for exactly the fields they need, which suits varied frontends. gRPC (Protocol Buffers
over HTTP/2) is fast and strongly typed for service-to-service calls. Many systems use REST at the
edge and gRPC or messaging inside.

## Practice

### Document error responses
**Level:** Beginner

Add `@ApiResponse` annotations to `BookController` so Swagger UI shows the 400, 401, 403, 404 and
409 responses with the ProblemDetail schema.

### Generate a client
**Level:** Intermediate

Download `/v3/api-docs` and generate a TypeScript or Java client with OpenAPI Generator. Use it to
list books from a small script.

### Async report endpoint
**Level:** Advanced

Add `POST /api/reports/sales` returning 202 and a status URL, generate the report with `@Async`,
and let `GET /api/reports/{id}` return PENDING, then DONE with the result.

## Interview questions

- How do you make a POST endpoint safe to retry?
- Which API changes are backward compatible?
- Offset vs cursor pagination?
- When would you pick GraphQL or gRPC over REST?

## Further reading

- [springdoc-openapi](https://springdoc.org/)
- [OpenAPI specification](https://spec.openapis.org/oas/latest.html)
- [Stripe: Idempotent requests](https://docs.stripe.com/api/idempotent_requests)
- [Google API design guide](https://cloud.google.com/apis/design)
