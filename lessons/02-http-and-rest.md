# 02 · How the web works: HTTP, REST and JSON

> **Phase:** Foundations · **Week:** 1 · **Try it:** [`requests.http`](../requests.http)

A backend developer's job is mostly answering HTTP requests. Before writing a controller,
understand what a request and response are, what the status codes mean, and what makes an API
"RESTful". Everything here is language-independent.

## Concepts

### The request/response cycle
**Level:** Beginner

A client opens a TCP connection (TLS for HTTPS), sends a request (method, path, headers, optional
body) and receives a response (status code, headers, optional body). HTTP is stateless: each
request carries everything the server needs, which is why APIs send a token with every call
(module 13). Use your browser's network tab or `curl -v http://localhost:8080/api/books/1` to see a
real exchange.

### HTTP methods and their guarantees
**Level:** Beginner

`GET` reads, `POST` creates or triggers, `PUT` replaces, `PATCH` partially updates, `DELETE`
removes. *Safe* methods (GET, HEAD) change nothing. *Idempotent* methods (GET, PUT, DELETE) have
the same effect whether sent once or five times; POST is not idempotent, which is why module 19
adds an `Idempotency-Key` header to order creation.

### Status codes
**Level:** Beginner

`2xx` success (200 OK, 201 Created, 204 No Content), `3xx` redirects, `4xx` the client's fault
(400 Bad Request, 401 Unauthorized = not logged in, 403 Forbidden = logged in but not allowed,
404 Not Found, 409 Conflict, 422 Unprocessable Content, 429 Too Many Requests), `5xx` the server's
fault (500, 502, 503). Returning the right code lets clients react correctly without parsing text.

### Headers that matter
**Level:** Beginner

`Content-Type` (what the body is, e.g. `application/json`), `Accept` (what the client wants back),
`Authorization` (credentials), `Location` (URL of a newly created resource), `Cache-Control` and
`ETag` (caching), `Retry-After` (when to retry after 429/503), and custom headers such as
`X-Correlation-Id` (module 18).

### JSON
**Level:** Beginner

JSON has objects, arrays, strings, numbers, booleans and `null`. Spring Boot uses Jackson to
convert Java objects to JSON and back. Decide your conventions once: camelCase field names, ISO-8601
timestamps (`2026-10-11T07:45:47Z`), and money as a decimal number or string, never a float you
compute with.

### REST resource design
**Level:** Beginner

Model nouns, not verbs: `/api/books`, `/api/books/42`, `/api/orders/7/items`. Use the HTTP method
for the action. Use query parameters for filtering, sorting and paging
(`/api/books?title=java&sort=price,desc&page=0&size=20`). Return 201 with a `Location` header on
create. Keep URLs lowercase and plural.

### Statelessness, cookies and tokens
**Level:** Intermediate

Traditional web apps keep a session on the server and send the browser a session cookie. APIs
usually stay stateless and send a bearer token (a JWT) on every request, so any server instance
can handle any request. That makes horizontal scaling easy (module 23) but makes logout and
revocation harder (module 13).

### HTTPS, TLS and CORS
**Level:** Intermediate

TLS encrypts traffic and proves the server's identity with a certificate; in production it usually
ends at a load balancer. CORS is a browser rule: JavaScript on `https://shop.example` may only call
`https://api.example` if the API's response headers allow that origin. It is not a security
boundary for your API; it protects users' browsers. See `corsConfigurationSource` in
[`SecurityConfig`](../src/main/java/com/zerotohero/security/SecurityConfig.java).

## Practice

### Explore an API with curl
**Level:** Beginner

Run the app (`mvn spring-boot:run`) and use `curl -i` to: list books, fetch one, fetch a missing
one (note the 404 body), and create one without a token (note the 401). Write down each status
code and the headers you see.

### Design the endpoints for a library
**Level:** Beginner

On paper, design the REST endpoints for members borrowing and returning books: URLs, methods,
request and response bodies, and status codes including the error cases.

## Interview questions

- What is the difference between 401 and 403?
- Which HTTP methods are idempotent, and why does it matter for retries?
- PUT vs PATCH: when would you use each?
- What does CORS protect, and why doesn't it stop `curl`?

## Further reading

- [MDN: An overview of HTTP](https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview)
- [MDN: HTTP response status codes](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)
- [RFC 9110: HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110)
