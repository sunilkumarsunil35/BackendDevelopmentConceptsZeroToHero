# 21 · Microservices architecture

> **Phase:** Advanced · **Week:** 10

The bookstore is a *modular monolith*: one deployable with clear feature packages. That's the
right starting point for most teams. This module covers when and how to split it into
microservices, and the problems you take on when you do.

## Concepts

### Monolith, modular monolith, microservices
**Level:** Intermediate

A monolith is one deployable; a modular monolith enforces boundaries between modules inside it
(Spring Modulith can verify them); microservices are independently deployable services, each owning
its data. Microservices buy independent deployment and scaling per team at the cost of network
calls, distributed data and operational complexity. Split when team or scaling pain is real, not
on day one.

### Finding service boundaries (DDD)
**Level:** Intermediate

Use Domain-Driven Design's *bounded contexts*: catalog, ordering, payments and shipping each have
their own model and language ("book" in the catalog vs "line item" in ordering). Services should be
cohesive inside and loosely coupled outside. If two services always change together, they're one
service.

### Database per service
**Level:** Intermediate

Each service owns its tables and nobody else reads them directly. Other services get data through
APIs or events, and may keep a local copy (a read model). Sharing a database couples deployments
and schemas, which defeats the purpose.

### API gateway
**Level:** Intermediate

One entry point for clients that routes to services and handles cross-cutting concerns: TLS,
authentication, rate limiting, request logging. Spring Cloud Gateway, Kong, NGINX or a cloud
gateway. A *backend for frontend* is a gateway tailored to one client (web vs mobile).

### Service discovery and configuration
**Level:** Intermediate

Services need to find each other's addresses. On Kubernetes, DNS-based Services do it for you; off
Kubernetes, Eureka or Consul. Centralised configuration (Spring Cloud Config, Kubernetes ConfigMaps
and Secrets) keeps settings out of images.

### Sagas: transactions across services
**Level:** Advanced

There is no `@Transactional` across services. A saga is a sequence of local transactions where each
step publishes an event or command, and failures run *compensating* actions (refund payment,
release stock). *Choreography*: services react to each other's events. *Orchestration*: one
coordinator tells each service what to do. Design every step to be idempotent.

### Resilient communication
**Level:** Advanced

Every synchronous call needs a timeout, retries with backoff and a circuit breaker (module 17).
Prefer asynchronous events (module 20) where the user doesn't need an immediate answer. Long chains
of synchronous calls multiply latency and failure probability.

### Contract testing
**Level:** Advanced

Services deploy independently, so you need to know you haven't broken a consumer. Consumer-driven
contract tests (Pact, Spring Cloud Contract) let each consumer publish its expectations, which the
provider's build verifies.

## Practice

### Draw the split
**Level:** Intermediate

Draw how you'd split the bookstore into catalog, ordering and notification services: which tables
each owns, which calls are synchronous, which events flow between them, and what happens when
ordering can't reach the catalog.

### Order saga
**Level:** Advanced

Design (and optionally implement in this repo with the in-memory broker) an orchestrated saga:
reserve stock → charge payment → confirm order, with compensations for payment failure. Write a test
for the failure path.

### Enforce module boundaries
**Level:** Advanced

Add Spring Modulith, declare `book`, `order` and `messaging` as modules, run its verification test,
and fix any dependency that crosses a boundary through an internal class.

## Interview questions

- When would you not choose microservices?
- How do you keep data consistent across services without distributed transactions?
- Choreography vs orchestration for sagas?
- Why should each service own its database?

## Further reading

- [microservices.io patterns](https://microservices.io/patterns/)
- [Spring Cloud](https://spring.io/projects/spring-cloud)
- [Spring Modulith](https://docs.spring.io/spring-modulith/reference/)
- *Building Microservices*, 2nd edition, by Sam Newman
