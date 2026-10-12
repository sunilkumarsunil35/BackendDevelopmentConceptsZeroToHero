# 22 · Reactive programming with WebFlux

> **Phase:** Advanced · **Week:** 11 · **Code:** [`reactive/ReactiveExamples.java`](../src/main/java/com/zerotohero/reactive/ReactiveExamples.java) · **Tests:** [`ReactiveExamplesTest.java`](../src/test/java/com/zerotohero/reactive/ReactiveExamplesTest.java)

Reactive programming handles many concurrent I/O-bound operations with a few threads by never
blocking: work is described as a pipeline of asynchronous values. Spring WebFlux is the reactive
counterpart of Spring MVC, built on Project Reactor.

## Concepts

### Mono and Flux
**Level:** Intermediate · **Code:** [ReactiveExamples.java](../src/main/java/com/zerotohero/reactive/ReactiveExamples.java)

`Mono<T>` emits zero or one value, `Flux<T>` zero to many, then completes or errors. They're lazy:
nothing happens until something subscribes (in WebFlux, the framework subscribes for you). Think
of them as `Optional`/`Stream` whose values arrive later.

### Operators
**Level:** Intermediate · **Code:** [ReactiveExamples.java](../src/main/java/com/zerotohero/reactive/ReactiveExamples.java)

`map` (sync transform), `filter`, `flatMap` (async transform that returns a publisher; results may
interleave), `concatMap` (keeps order), `zip` (combine independent results), `collectList`,
`distinct`, `sort`. Learning to choose between `map` and `flatMap` is most of the battle.

### Error handling, retries and timeouts
**Level:** Intermediate · **Code:** [ReactiveExamplesTest.java](../src/test/java/com/zerotohero/reactive/ReactiveExamplesTest.java)

Errors travel down the pipeline as signals: `onErrorResume`, `onErrorReturn`, `retryWhen(Retry.backoff(3, ofMillis(100)))`,
`timeout(...)`. `StepVerifier.withVirtualTime` tests a 10-second delay instantly.

### Backpressure
**Level:** Advanced · **Code:** [ReactiveExamplesTest.java](../src/test/java/com/zerotohero/reactive/ReactiveExamplesTest.java)

Subscribers request how many items they can handle, so a fast producer can't overwhelm a slow
consumer. `limitRate(n)` requests in batches; `onBackpressureBuffer`/`Drop` decide what happens to
excess.

### WebFlux controllers and WebClient
**Level:** Intermediate

Same annotations as MVC, but return `Mono`/`Flux`:
`@GetMapping("/books/{id}") Mono<BookResponse> get(@PathVariable Long id)`. Or use functional
routes (`RouterFunction`). `WebClient` is the non-blocking HTTP client:
`webClient.get().uri(...).retrieve().bodyToMono(Rates.class)`. Server-sent events stream a `Flux`
with `produces = TEXT_EVENT_STREAM_VALUE`.

### Never block the event loop
**Level:** Advanced

WebFlux runs on a few Netty event-loop threads. A blocking call (JDBC, `Thread.sleep`, a blocking
HTTP client) on one of them stalls every request it serves. Use reactive drivers (R2DBC, reactive
Mongo/Redis) or move blocking work to `Schedulers.boundedElastic()`. BlockHound detects mistakes
in tests.

### Reactive vs virtual threads
**Level:** Advanced

Virtual threads (module 23) give most of the scalability benefit of reactive code with ordinary
blocking-style code, JDBC and simple stack traces. Choose WebFlux for streaming, backpressure, or
an already-reactive stack; choose MVC with virtual threads for typical CRUD services.

## Practice

### Reactive book endpoint
**Level:** Intermediate

Create a small separate WebFlux project with R2DBC and H2 that serves `GET /books` as a `Flux` and
`GET /books/{id}` as a `Mono`, tested with `WebTestClient`.

### Stream live prices
**Level:** Advanced

Add a server-sent events endpoint that emits a simulated price for a book every second with
`Flux.interval`, and stop it after a client disconnects.

## Interview questions

- `map` vs `flatMap` vs `concatMap`?
- What is backpressure?
- What happens if you call JDBC inside a WebFlux handler?
- Reactive or virtual threads for a new CRUD service, and why?

## Further reading

- [Spring Framework: WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor reference](https://projectreactor.io/docs/core/release/reference/)
