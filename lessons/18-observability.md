# 18 · Observability: logs, metrics, traces and health

> **Phase:** Production-ready · **Week:** 9 · **Code:** [`observability/`](../src/main/java/com/zerotohero/observability), [`aop/ExecutionTimeAspect.java`](../src/main/java/com/zerotohero/aop/ExecutionTimeAspect.java) · **Tests:** [`ObservabilityTest.java`](../src/test/java/com/zerotohero/observability/ObservabilityTest.java)

In production you can't attach a debugger. Observability is being able to answer "what is the
system doing and why?" from the outside, using logs, metrics and traces, plus health checks that
let the platform route traffic and restart broken instances.

## Concepts

### Spring Boot Actuator
**Level:** Beginner · **Code:** [application.yml](../src/main/resources/application.yml)

Actuator adds operational endpoints under `/actuator`: `health`, `info`, `metrics`, `env`,
`loggers`, `threaddump`, `prometheus` and more. Expose only what you need
(`management.endpoints.web.exposure.include`) and secure the rest; here metrics are admin-only.

### Health checks, liveness and readiness
**Level:** Intermediate · **Code:** [CatalogHealthIndicator.java](../src/main/java/com/zerotohero/observability/CatalogHealthIndicator.java)

`/actuator/health` aggregates indicators (database, disk, your own `HealthIndicator`). With probes
enabled, `/actuator/health/liveness` says "restart me if I'm stuck" and `/actuator/health/readiness`
says "don't send me traffic yet". Kubernetes uses both (module 24). Don't put external dependencies
in liveness, or one outage restarts everything.

### Metrics with Micrometer
**Level:** Intermediate · **Code:** [OrderService.java](../src/main/java/com/zerotohero/order/OrderService.java)

Micrometer is a facade like SLF4J, for metrics. Boot records HTTP, JVM, database pool and cache
metrics automatically. Add business metrics: `Counter` (`bookstore.orders.placed`), `Timer`
(`bookstore.method.time`), `Gauge`, `DistributionSummary`. Keep tag values low-cardinality (never a
user id or order id as a tag). Export to Prometheus with `micrometer-registry-prometheus` and
graph in Grafana.

### Structured logging and correlation ids
**Level:** Intermediate · **Code:** [CorrelationIdFilter.java](../src/main/java/com/zerotohero/observability/CorrelationIdFilter.java)

Put a request id into the MDC so every log line for that request includes it, and return it in a
response header so users can quote it. Boot 3.4+ can log JSON directly
(`logging.structured.format.console=ecs` or `logstash`), which log platforms index without parsing.

### Distributed tracing
**Level:** Advanced

A trace follows one request across services; each hop is a span. Micrometer Tracing with an
OpenTelemetry bridge (`micrometer-tracing-bridge-otel` + an OTLP exporter) propagates
`traceparent` headers through `RestClient`/`WebClient` automatically and puts `traceId`/`spanId` in
logs. View traces in Jaeger, Zipkin, Grafana Tempo or a vendor tool. Sample in production.

### The four golden signals and SLOs
**Level:** Advanced

Monitor latency, traffic, errors and saturation for every service. Define service level objectives
("99% of `GET /api/books` under 200 ms over 30 days") and alert on burning the error budget, not on
every spike. Alert on symptoms users feel, not on CPU.

## Practice

### Expose Prometheus metrics
**Level:** Intermediate

Add `micrometer-registry-prometheus`, expose `/actuator/prometheus`, run Prometheus and Grafana in
`docker-compose.yml`, and build a dashboard with request rate, p95 latency and `bookstore.orders.placed`.

### JSON logs
**Level:** Beginner

Switch console logging to structured JSON with one property, place an order, and find the
correlation id in each log line.

### Add tracing
**Level:** Advanced

Add Micrometer Tracing with OpenTelemetry and Jaeger in Docker. Call the price endpoint and find the
outgoing exchange-rate call as a child span.

## Interview questions

- Logs vs metrics vs traces: what is each best at?
- Liveness vs readiness: what goes wrong if you mix them up?
- Why are high-cardinality metric tags dangerous?
- How would you find why one request was slow across five services?

## Further reading

- [Spring Boot: Actuator](https://docs.spring.io/spring-boot/reference/actuator/index.html)
- [Micrometer documentation](https://docs.micrometer.io/micrometer/reference/)
- [Google SRE book: Monitoring distributed systems](https://sre.google/sre-book/monitoring-distributed-systems/)
