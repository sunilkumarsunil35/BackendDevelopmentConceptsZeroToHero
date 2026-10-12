# Backend Development Concepts: Zero to Hero (Java + Spring Boot)

A complete backend course in Java and Spring Boot, from your first REST endpoint to transactions,
security, caching, messaging, resilience, observability and deployment. There are 25 modules and
265 topics: 194 concepts and 71 practice tasks. Every module has a lesson with interview
questions, and almost every concept points to working, tested code in one running application: a
bookstore API. The tests need only a JDK and Maven.

## Track your progress

[**Open the progress tracker**](https://claude.ai/artifact/BKKXKQAwYGDs6onUyBEkao) on your phone or
computer. Mark each topic To do, Learning, Done or Revise, write notes, and see your roadmap week,
streak and module progress. Progress syncs across every device where you're signed in to
claude.ai, so you can tick a topic on your phone on the bus and see it on your laptop at home.

That link is the repo owner's private synced copy. Anyone can use the same tracker from
[`tracker/index.html`](tracker/index.html), published at
https://sunilkumarsunil35.github.io/BackendDevelopmentConceptsZeroToHero/. That copy saves progress
in the browser. To sync it across devices, open its **Sync across devices** panel, create a GitHub
token with only the `gist` permission, paste it and choose Connect. Do that once on each device:
progress then saves to a private gist in your GitHub account and loads from it whenever you open the
page. The token stays in that browser. The **Backup and move progress** panel still copies a backup
code you can paste into another browser or into the synced tracker.

## Run the tests

You need JDK 21 or newer and Maven 3.9+.

```bash
sh run-tests.sh
```

This runs 86 tests: plain unit tests, `@WebMvcTest` and `@DataJpaTest` slices, and full
`@SpringBootTest` integration tests. CI runs the same script on every push
(`.github/workflows/ci.yml`).

## Run the app

```bash
mvn spring-boot:run
```

Then open http://localhost:8080/swagger-ui.html, or try the requests in [`requests.http`](requests.http).
Two demo users exist: `reader` / `reader-pass` (can order) and `admin` / `admin-pass` (can also
edit the catalog). The app uses an in-memory H2 database by default; `docker compose up --build`
runs it against PostgreSQL instead.

## Layout

```
BackendDevelopmentConceptsZeroToHero/
├── lessons/                     25 lessons: concepts, practice tasks, interview questions, reading
├── src/main/java/com/zerotohero/
│   ├── basics/                  01 modern Java
│   ├── core/ boot/              04-05 IoC, DI, configuration, profiles
│   ├── web/ book/ error/ validation/   06-09 REST, validation, errors, JPA
│   ├── order/                   10 transactions, locking, idempotency
│   ├── security/                13 JWT authentication and authorization
│   ├── cache/ async/ aop/       14-16 caching, async, events, scheduling, aspects
│   ├── client/ observability/ docs/    17-19 resilience, metrics, health, OpenAPI
│   ├── messaging/ reactive/ ratelimit/ 20, 22, 23 outbox, Reactor, rate limiting
├── src/main/resources/          application.yml and Flyway migrations
├── src/test/java/com/zerotohero/       tests for every module
├── tracker/index.html           the progress tracker
├── tools/build_tracker.py       regenerates the tracker's topic list from the lessons
├── Dockerfile, docker-compose.yml, k8s/   24 deployment
└── requests.http                ready-made API calls
```

**Study tip:** read a lesson, then open the linked code and its test. Break something on purpose
(remove `@Transactional`, make `@ManyToOne` eager, delete a validation annotation) and see which
test fails and why. Then do the practice tasks without looking at the answers in the code.

## 12-week roadmap

Spend 1 to 2 hours a day. Each week, re-explain two topics from earlier weeks out loud, as if in an
interview.

| Week | Modules | Goal |
| ---- | ------- | ---- |
| 1  | 01 Java, 02 HTTP and REST | Read and write modern Java; explain any HTTP exchange |
| 2  | 03 Build tools, 04 Spring Core | Set up a project; explain IoC, DI and bean scopes |
| 3  | 05 Boot essentials, 06 REST controllers | Build a CRUD API with DTOs, paging and proper status codes |
| 4  | 07 Validation and errors, 08 SQL | Reject bad input cleanly; design tables, keys and indexes |
| 5  | 09 Spring Data JPA, 10 Transactions | Map entities without N+1; reason about rollbacks and locking |
| 6  | 11 Flyway, 12 Testing | Version the schema; test every layer with the right tool |
| 7  | 13 Security and JWT | Stateless auth, roles, method security, OWASP basics |
| 8  | 14 Caching, 15 Async and events, 16 AOP | Speed up reads safely; move work off the request thread |
| 9  | 17 Resilience, 18 Observability, 19 API design | Survive failing dependencies; see inside production |
| 10 | 20 Messaging, 21 Microservices | Outbox, idempotent consumers, sagas, service boundaries |
| 11 | 22 Reactive, 23 Performance | Reactor basics; measure and fix bottlenecks; rate limiting |
| 12 | 24 Deployment, 25 Capstone | Ship with Docker, Kubernetes and CI; build a service on your own |

## Concepts by module

| #  | Lesson | Concepts |
| -- | ------ | -------- |
| 01 | [Java for backend developers](lessons/01-java-for-backend.md) | Classes, objects and interfaces, Collections: List, Set, Map, Lambdas and functional interfaces, Streams, Optional, Records, Exceptions: checked vs unchecked, Generics, Sealed types and pattern matching, BigDecimal for money, Concurrency basics and virtual threads |
| 02 | [How the web works: HTTP, REST and JSON](lessons/02-http-and-rest.md) | The request/response cycle, HTTP methods and their guarantees, Status codes, Headers that matter, JSON, REST resource design, Statelessness, cookies and tokens, HTTPS, TLS and CORS |
| 03 | [Build tools and project setup](lessons/03-build-tools-and-project-setup.md) | Maven basics: the POM, The Maven lifecycle, Spring Boot starters and the parent BOM, Spring Initializr, Project layout, Gradle in one page, Wrappers and reproducible builds |
| 04 | [Spring Core: IoC and dependency injection](lessons/04-spring-core-ioc-and-di.md) | Inversion of control and the ApplicationContext, Dependency injection: prefer constructors, Stereotype annotations and component scanning, @Configuration and @Bean, Choosing between several beans: @Primary and @Qualifier, Bean scopes, Bean lifecycle, Type-safe configuration with @ConfigurationProperties, Proxies: how Spring adds behaviour |
| 05 | [Spring Boot essentials](lessons/05-spring-boot-essentials.md) | @SpringBootApplication and the main method, Auto-configuration, application.yml and externalized configuration, Profiles, Reading configuration in code, Logging, Startup hooks and the embedded server, DevTools and a fast feedback loop |
| 06 | [Building REST APIs with Spring MVC](lessons/06-rest-controllers.md) | @RestController and request mapping, Reading input: path variables, query params, headers, Request bodies and JSON mapping, Responses: status codes, headers and ResponseEntity, DTOs: never expose entities, Layered architecture: controller, service, repository, Pagination, sorting and filtering, Content negotiation and message converters, Filters and interceptors |
| 07 | [Validation and error handling](lessons/07-validation-and-error-handling.md) | Bean Validation annotations, Nested and collection validation, Custom constraints, Validating parameters and configuration, Centralised handling with @RestControllerAdvice, Problem Details (RFC 9457), What not to leak |
| 08 | [Databases and SQL fundamentals](lessons/08-databases-and-sql.md) | Tables, keys and relationships, SQL you must know, Normalization, Constraints protect your data, Indexes, ACID and isolation levels, SQL vs NoSQL |
| 09 | [Spring Data JPA and Hibernate](lessons/09-spring-data-jpa.md) | Entities and mapping, Repositories and derived queries, @Query with JPQL and native SQL, Relationships and fetch types, The persistence context and dirty checking, The N+1 problem and how to fix it, Projections, Paging and sorting, Auditing, Open Session in View |
| 10 | [Transactions and concurrency](lessons/10-transactions-and-concurrency.md) | @Transactional basics, Rollback rules, Read-only transactions, Propagation, The self-invocation trap, Isolation levels in practice, Optimistic locking with @Version, Pessimistic locking, Transactions and side effects |
| 11 | [Database migrations with Flyway](lessons/11-database-migrations.md) | Why not ddl-auto?, Versioned migrations, Seed and reference data, Repeatable migrations and callbacks, Zero-downtime schema changes, Testing migrations |
| 12 | [Testing Spring Boot applications](lessons/12-testing.md) | The testing pyramid, JUnit 5 and AssertJ, Unit tests with Mockito, Web slice: @WebMvcTest and MockMvc, Data slice: @DataJpaTest, Full application: @SpringBootTest, Testing asynchronous code, Testing HTTP clients, Testcontainers, Test data and isolation |
| 13 | [Spring Security and JWT](lessons/13-spring-security-and-jwt.md) | Authentication vs authorization, The security filter chain, Password storage, JWT: structure and trade-offs, Issuing tokens, Validating tokens: OAuth2 resource server, Method security, CSRF and CORS, OAuth2 and OpenID Connect with an identity provider, Security checklist (OWASP API Top 10) |
| 14 | [Caching](lessons/14-caching.md) | Spring's cache abstraction, Keeping the cache correct: @CachePut and @CacheEvict, Caffeine: in-process caching, Distributed caching with Redis, Caching strategies, Cache pitfalls, HTTP caching |
| 15 | [Async processing, scheduling and events](lessons/15-async-scheduling-and-events.md) | Application events, Transactional event listeners, @Async, Executors and thread pools, CompletableFuture composition, @Scheduled jobs, Scheduling in a cluster |
| 16 | [Aspect-oriented programming](lessons/16-aop.md) | Aspects, advice, pointcuts and join points, Around advice, Annotation-driven pointcuts, Spring AOP proxies vs AspectJ weaving, When not to use AOP |
| 17 | [Calling other services and resilience](lessons/17-calling-services-and-resilience.md) | RestClient, WebClient and HTTP interfaces, Handling errors from remote calls, Timeouts, Retries with backoff, Circuit breaker, Fallbacks and graceful degradation, Bulkheads and rate limiters |
| 18 | [Observability: logs, metrics, traces and health](lessons/18-observability.md) | Spring Boot Actuator, Health checks, liveness and readiness, Metrics with Micrometer, Structured logging and correlation ids, Distributed tracing, The four golden signals and SLOs |
| 19 | [API design and documentation](lessons/19-api-design-and-documentation.md) | OpenAPI and springdoc, Consistent conventions, Idempotency keys, Versioning and evolution, Pagination styles, Long-running operations, REST, GraphQL and gRPC |
| 20 | [Messaging and event-driven architecture](lessons/20-messaging-and-events.md) | Queues vs topics (pub/sub), Commands vs events, Apache Kafka essentials, RabbitMQ essentials, The dual-write problem and the transactional outbox, Delivery guarantees and idempotent consumers, Failures: retries and dead-letter queues, Event sourcing and CQRS |
| 21 | [Microservices architecture](lessons/21-microservices.md) | Monolith, modular monolith, microservices, Finding service boundaries (DDD), Database per service, API gateway, Service discovery and configuration, Sagas: transactions across services, Resilient communication, Contract testing |
| 22 | [Reactive programming with WebFlux](lessons/22-reactive-programming.md) | Mono and Flux, Operators, Error handling, retries and timeouts, Backpressure, WebFlux controllers and WebClient, Never block the event loop, Reactive vs virtual threads |
| 23 | [Performance and scalability](lessons/23-performance-and-scalability.md) | Measure first, Database performance, Connection pooling with HikariCP, Virtual threads, Rate limiting, Horizontal scaling and statelessness, Scaling the database, JVM tuning basics |
| 24 | [Deployment: Docker, Kubernetes and CI/CD](lessons/24-deployment-and-devops.md) | The executable jar, Docker images, Docker Compose for local environments, Twelve-factor configuration, Kubernetes basics, Probes and graceful shutdown, CI/CD pipelines, Native images and fast startup |
| 25 | [Capstone and system design](lessons/25-capstone-and-system-design.md) | A system design framework, Back-of-the-envelope estimation, CAP and consistency models, Classic designs to practise |

## Practice tasks

Each lesson ends with practice tasks. Most extend the bookstore, so you can check your work with
its tests. B = Beginner, I = Intermediate, A = Advanced.

| #  | Module | Tasks |
| -- | ------ | ----- |
| 01 | Java for backend developers | Group orders by customer (B), Model order states with a sealed interface (I), Measure platform vs virtual threads (I) |
| 02 | How the web works: HTTP, REST and JSON | Explore an API with curl (B), Design the endpoints for a library (B) |
| 03 | Build tools and project setup | Generate and run a project (B), Read the dependency tree (I) |
| 04 | Spring Core: IoC and dependency injection | Add a third greeter (B), Replace a hard-coded clock (B), Strategy pattern with a Map of beans (I) |
| 05 | Spring Boot essentials | Add a profile (B), Read the auto-configuration report (I), Write your own auto-configuration (A) |
| 06 | Building REST APIs with Spring MVC | Build an authors resource (B), Add PATCH for price changes (I), Add filtering by price range (I) |
| 07 | Validation and error handling | Validate a date range (I), Handle malformed JSON (B), Validation groups (A) |
| 08 | Databases and SQL fundamentals | Write five reporting queries (B), Design a reviews table (I), Read a query plan (A) |
| 09 | Spring Data JPA and Hibernate | Add tags (many-to-many) (I), Write a revenue report query (I), Keyset pagination (A) |
| 10 | Transactions and concurrency | Cancel an order (I), Prove the self-invocation trap (I), Race for the last copy (A) |
| 11 | Database migrations with Flyway | Add a column the safe way (I), Rename a column without downtime (A) |
| 12 | Testing Spring Boot applications | Test the authors API (B), Switch to Testcontainers (A), Measure coverage (I) |
| 13 | Spring Security and JWT | Users in the database (I), Refresh tokens (A), Use Keycloak (A) |
| 14 | Caching | Cache the book list (I), Switch to Redis (A), Add ETags (I) |
| 15 | Async processing, scheduling and events | Daily sales summary (I), Parallel enrichment (I), Add ShedLock (A) |
| 16 | Aspect-oriented programming | Audit admin actions (I), A simple retry aspect (A) |
| 17 | Calling other services and resilience | Add timeouts (I), Rewrite with annotations (I), HTTP interface client (I) |
| 18 | Observability: logs, metrics, traces and health | Expose Prometheus metrics (I), JSON logs (B), Add tracing (A) |
| 19 | API design and documentation | Document error responses (B), Generate a client (I), Async report endpoint (A) |
| 20 | Messaging and event-driven architecture | Publish to real Kafka (A), Dead-letter handling (A), Clean up the outbox (I) |
| 21 | Microservices architecture | Draw the split (I), Order saga (A), Enforce module boundaries (A) |
| 22 | Reactive programming with WebFlux | Reactive book endpoint (I), Stream live prices (A) |
| 23 | Performance and scalability | Load-test the catalog (I), Per-user rate limits (A), Batch inserts (I) |
| 24 | Deployment: Docker, Kubernetes and CI/CD | Run with PostgreSQL (B), Publish an image from CI (I), Deploy to a local cluster (A) |
| 25 | Capstone and system design | Capstone: event ticketing service (A), Design a URL shortener (A), Review your own code (I), Mock interview (A) |

## Annotation cheat sheet

| Annotation | What it does | Module |
| ---------- | ------------ | ------ |
| `@SpringBootApplication` | Configuration + auto-configuration + component scan | 05 |
| `@Component`, `@Service`, `@Repository` | Register a class as a bean | 04 |
| `@Configuration` + `@Bean` | Create beans in code | 04 |
| `@Primary`, `@Qualifier` | Choose between beans of the same type | 04 |
| `@ConfigurationProperties` + `@Validated` | Bind and validate a group of settings | 04 |
| `@Profile` | Bean exists only in some environments | 05 |
| `@RestController`, `@GetMapping`, `@PostMapping` ... | Map HTTP requests to methods | 06 |
| `@PathVariable`, `@RequestParam`, `@RequestBody`, `@RequestHeader` | Read request input | 06 |
| `@Valid`, `@NotBlank`, `@Size`, custom `@Constraint` | Validate input | 07 |
| `@RestControllerAdvice` + `@ExceptionHandler` | Turn exceptions into error responses | 07 |
| `@Entity`, `@Id`, `@ManyToOne(fetch = LAZY)`, `@OneToMany(mappedBy)` | Map tables and relationships | 09 |
| `@Query`, `@EntityGraph` | Custom queries; load associations without N+1 | 09 |
| `@Transactional(readOnly, propagation, rollbackFor)` | Transaction boundaries | 10 |
| `@Version` | Optimistic locking | 10 |
| `@EnableMethodSecurity` + `@PreAuthorize` | Method-level authorization | 13 |
| `@Cacheable`, `@CachePut`, `@CacheEvict` | Caching | 14 |
| `@Async`, `@Scheduled` | Background and scheduled work | 15 |
| `@TransactionalEventListener(phase = AFTER_COMMIT)` | React only after a commit | 15 |
| `@Aspect` + `@Around` | Cross-cutting behaviour | 16 |
| `@WebMvcTest`, `@DataJpaTest`, `@SpringBootTest`, `@MockitoBean` | Testing at each level | 12 |

## Which tool for which problem

| If you need to... | Reach for | Module |
| ----------------- | --------- | ------ |
| Return a clear error for bad input | Bean Validation + `ProblemDetail` | 07 |
| Make several writes all-or-nothing | `@Transactional` on the service method | 10 |
| Stop two users overwriting each other | `@Version` (optimistic) or `SELECT ... FOR UPDATE` | 10 |
| Let clients retry a POST safely | Idempotency key stored with the result | 19 |
| Speed up repeated reads | `@Cacheable` with Caffeine or Redis | 14 |
| Fix a slow list endpoint | Paging, projections, fetch joins, indexes | 09, 08 |
| Send an email after an order | `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` | 15 |
| Publish an event without losing it | Transactional outbox | 20 |
| Survive a flaky dependency | Timeout + retry with backoff + circuit breaker + fallback | 17 |
| Protect the API from abuse | Rate limiting (token bucket), 429 + `Retry-After` | 23 |
| Find out why production is slow | Metrics, traces, correlation ids, profiling | 18, 23 |
| Change the schema without downtime | Flyway + expand and contract | 11 |
| Run many blocking calls concurrently | Virtual threads | 01, 23 |

## Spring Boot interview tips

- Prefer constructor injection; you can then test without Spring.
- Know why `@Transactional` and `@Cacheable` don't work on self-calls or private methods: proxies.
- Never return entities from controllers. Map to DTOs.
- Make `@ManyToOne` lazy and disable Open Session in View; explain the N+1 problem with numbers.
- Only unchecked exceptions roll back a transaction by default.
- Every remote call needs a timeout. Retries need backoff and an idempotent operation.
- 401 means "who are you?"; 403 means "I know who you are, and no".
- A JWT is signed, not encrypted, and hard to revoke: keep it short-lived.
- Keep secrets out of the repo; pass them as environment variables.

## In the interview

1. Restate the requirement and ask about scale, consistency and failure expectations.
2. Sketch the API (methods, paths, status codes) and the data model first.
3. Walk through the happy path, then the failure paths: invalid input, missing data, concurrent
   updates, a slow dependency.
4. Say where the transaction boundary is and why.
5. Say how you'd test it: which test type for which layer.
6. Finish with what you'd monitor in production.

## Keeping the tracker in sync

The tracker's topic list is generated from the lesson headings. After adding or renaming a topic in
a lesson, run:

```bash
python3 tools/build_tracker.py
```

CI fails if you forget (`--check`). Topic ids come from the heading text, so renaming a heading
resets that topic's progress.
