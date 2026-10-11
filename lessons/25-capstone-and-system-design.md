# 25 · Capstone and system design

> **Phase:** Advanced · **Week:** 12

You now know the building blocks. This module ties them together: build a complete service on
your own, and practise the system design conversations that senior backend interviews use.

## Concepts

### A system design framework
**Level:** Advanced

1. Clarify requirements: functional (what it does) and non-functional (scale, latency, consistency,
   availability). 2. Estimate: requests per second, data size, read/write ratio. 3. Define the API.
4. Design the data model. 5. Draw the high-level architecture. 6. Deep-dive the hardest parts
(bottlenecks, failure modes). 7. Discuss trade-offs and what you'd change at 10x scale.

### Back-of-the-envelope estimation
**Level:** Advanced

1 million requests/day ≈ 12 per second on average; peaks are often 5–10x. A day has about
86,400 seconds. Memory read ~100 ns, SSD read ~100 µs, same-datacenter round trip ~0.5 ms,
cross-continent ~150 ms. Use these to decide whether something fits in one database, one cache,
one machine.

### CAP and consistency models
**Level:** Advanced

During a network partition a distributed system chooses between consistency and availability.
Most product features tolerate eventual consistency (a review count a few seconds stale); some
don't (account balances, stock for the last item). Decide per feature, and design user-visible
behaviour around it.

### Classic designs to practise
**Level:** Advanced

URL shortener (id generation, redirects, caching), rate limiter (module 23, distributed),
notification service (fan-out, retries, user preferences), e-commerce checkout (module 10 and 20:
stock, payment, saga), news feed (fan-out on write vs read), and a job scheduler (module 15 at
scale). For each, know the data model, the hot path and the failure modes.

## Practice

### Capstone: event ticketing service
**Level:** Advanced

Build a new Spring Boot service from scratch: events with limited seats, holds that expire after
10 minutes, purchases with idempotency keys, JWT auth with roles, Flyway, Testcontainers tests,
caching for event listings, an outbox for `TicketPurchased` events, metrics and a Dockerfile. No two
buyers may ever get the same seat.

### Design a URL shortener
**Level:** Advanced

Write a one-page design for 100 million new links per month and a 100:1 read/write ratio: API,
id scheme, storage, caching, analytics and what happens if the cache cluster fails. Then implement
the core with Spring Boot and Redis.

### Review your own code
**Level:** Intermediate

Pick your capstone or this repo and review it against every module's interview questions. Write
down three things you'd change before running it in production, and change them.

### Mock interview
**Level:** Advanced

With a friend or a timer, answer one system design prompt in 45 minutes using the framework above,
then one coding task (implement an endpoint with validation, a transaction and tests) in 45 minutes.

## Interview questions

- Design a rate limiter for an API used by 10,000 customers across 20 instances.
- How would you guarantee a seat is never sold twice?
- Your service must stay up if the cache goes down. What changes?
- How do you migrate a monolith's orders module to a separate service without downtime?

## Further reading

- [System Design Primer](https://github.com/donnemartin/system-design-primer)
- [ByteByteGo: System design fundamentals](https://bytebytego.com/)
- *Designing Data-Intensive Applications* by Martin Kleppmann
