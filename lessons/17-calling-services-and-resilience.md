# 17 · Calling other services and resilience

> **Phase:** Production-ready · **Week:** 9 · **Code:** [`client/`](../src/main/java/com/zerotohero/client) · **Tests:** [`ExchangeRateClientTest.java`](../src/test/java/com/zerotohero/client/ExchangeRateClientTest.java), [`PriceConversionServiceTest.java`](../src/test/java/com/zerotohero/client/PriceConversionServiceTest.java)

Backends call other backends: payment providers, exchange-rate APIs, other microservices. Those
calls are slow, and sometimes they fail or hang. A resilient service limits how long it waits,
retries sensibly, stops hammering a broken dependency, and degrades gracefully.

## Concepts

### RestClient
**Level:** Beginner · **Code:** [ExchangeRateClient.java](../src/main/java/com/zerotohero/client/ExchangeRateClient.java)

`RestClient` (Spring 6.1+) is the modern synchronous HTTP client with a fluent API:
`restClient.get().uri("/latest?from={f}&to={t}", f, t).retrieve().body(RatesResponse.class)`.
Build it from the auto-configured `RestClient.Builder` so it shares Jackson settings and
observability. `RestTemplate` still works but is in maintenance mode.

### WebClient and HTTP interfaces
**Level:** Intermediate

`WebClient` is the non-blocking client from WebFlux (module 22). *HTTP interfaces* let you declare
a client as a Java interface:
`@GetExchange("/latest") RatesResponse latest(@RequestParam String from, @RequestParam String to);`
and create it with `HttpServiceProxyFactory`, like Feign but built in.

### Handling errors from remote calls
**Level:** Intermediate · **Code:** [ExchangeRateClientTest.java](../src/test/java/com/zerotohero/client/ExchangeRateClientTest.java)

`retrieve()` throws `HttpClientErrorException` for 4xx and `HttpServerErrorException` for 5xx;
customise with `.onStatus(...)`. Translate remote errors into your own exceptions at the client
boundary so the rest of your code doesn't depend on one provider's error format.

### Timeouts
**Level:** Intermediate

Without timeouts, one hung dependency ties up all your threads. Always set a connect timeout and a
read timeout, e.g. via `ClientHttpRequestFactorySettings` or
`spring.http.client.connect-timeout`/`read-timeout`. Your timeout must be shorter than your own
caller's timeout.

### Retries with backoff
**Level:** Intermediate · **Code:** [PriceConversionService.java](../src/main/java/com/zerotohero/client/PriceConversionService.java)

Retry only transient failures (timeouts, 503, connection resets), only idempotent operations (or
with idempotency keys), a few times, with exponential backoff and jitter. Unbounded retries turn a
small outage into a retry storm.

### Circuit breaker
**Level:** Advanced · **Code:** [PriceConversionServiceTest.java](../src/test/java/com/zerotohero/client/PriceConversionServiceTest.java)

A circuit breaker watches the failure rate. Above a threshold it *opens* and fails fast without
calling the dependency, giving it time to recover; after a wait it goes *half-open* and lets a few
trial calls through. `circuitOpensAfterRepeatedFailuresAndStopsCallingTheService` shows the client
receiving no calls once the circuit is open. Resilience4j provides it, configured in
`application.yml`.

### Fallbacks and graceful degradation
**Level:** Intermediate · **Code:** [PriceConversionService.java](../src/main/java/com/zerotohero/client/PriceConversionService.java)

When the dependency is down, return something useful: the last known rate (flagged `stale`), a
default, a cached value, or a clear 503. Decide per feature what "degraded" looks like.

### Bulkheads and rate limiters
**Level:** Advanced

A bulkhead caps concurrent calls to one dependency so it can't consume every thread. A rate limiter
caps calls per second to respect a provider's quota. Both are in Resilience4j
(`@Bulkhead`, `@RateLimiter`) alongside `@CircuitBreaker` and `@Retry` annotations.

## Practice

### Add timeouts
**Level:** Intermediate

Configure 2-second connect and 3-second read timeouts for `ExchangeRateClient`. Write a test with a
slow fake server (WireMock's `withFixedDelay`) that proves the call fails fast.

### Rewrite with annotations
**Level:** Intermediate

Rewrite `PriceConversionService` with `@CircuitBreaker(name = "exchangeRates", fallbackMethod = "...")`
and `@Retry`. Compare readability and testability with the programmatic version.

### HTTP interface client
**Level:** Intermediate

Replace `ExchangeRateClient` with an `@HttpExchange` interface created via `HttpServiceProxyFactory`
and `RestClientAdapter`. Keep `ExchangeRateClientTest` passing.

## Interview questions

- What is a circuit breaker, and what are its three states?
- Which failures should be retried, and why should retries use backoff and jitter?
- Why must every remote call have a timeout?
- What is a bulkhead?

## Further reading

- [Spring Framework: REST clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Resilience4j documentation](https://resilience4j.readme.io/docs)
- *Release It!* by Michael T. Nygard
