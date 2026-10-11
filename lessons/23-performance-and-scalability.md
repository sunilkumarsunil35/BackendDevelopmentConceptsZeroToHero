# 23 · Performance and scalability

> **Phase:** Advanced · **Week:** 11 · **Code:** [`ratelimit/`](../src/main/java/com/zerotohero/ratelimit), [`application.yml`](../src/main/resources/application.yml) · **Tests:** [`TokenBucketTest.java`](../src/test/java/com/zerotohero/ratelimit/TokenBucketTest.java), [`BookRepositoryTest.java`](../src/test/java/com/zerotohero/book/BookRepositoryTest.java)

Performance work starts with measurement, not guesses. Most backend slowness comes from the
database, remote calls and missing limits, rarely from Java itself. Scalability is keeping
performance as traffic grows, usually by adding instances.

## Concepts

### Measure first
**Level:** Intermediate

Load-test with realistic data and traffic (k6, Gatling, JMeter) and look at p50/p95/p99 latency,
not averages. Profile with Java Flight Recorder and JDK Mission Control or async-profiler. Find the
one bottleneck, fix it, measure again.

### Database performance
**Level:** Intermediate · **Code:** [BookRepositoryTest.java](../src/test/java/com/zerotohero/book/BookRepositoryTest.java)

Fix N+1 queries (module 09), add indexes for real queries (module 08), select only needed columns
(projections), page every list, batch writes (`hibernate.jdbc.batch_size`), and turn off Open
Session in View. Log slow queries and count statements in tests.

### Connection pooling with HikariCP
**Level:** Intermediate · **Code:** [application-postgres.yml](../src/main/resources/application-postgres.yml)

Opening a database connection is expensive, so Boot pools them with HikariCP. Bigger isn't better:
a pool of about 10 per instance often beats 100, because the database has limited cores. Total
connections = pool size × instances, which must fit the database's `max_connections`. Watch
`hikaricp.connections.pending`.

### Virtual threads
**Level:** Intermediate · **Code:** [application.yml](../src/main/resources/application.yml)

`spring.threads.virtual.enabled=true` serves each request on a virtual thread, so thousands of
requests blocked on I/O don't need thousands of OS threads. The bottleneck then moves to what's
behind those threads (the connection pool, downstream services), so keep limits there.

### Rate limiting
**Level:** Intermediate · **Code:** [RateLimitFilter.java](../src/main/java/com/zerotohero/ratelimit/RateLimitFilter.java)

Protect the service from abusive or runaway clients. The token bucket
([`TokenBucket`](../src/main/java/com/zerotohero/ratelimit/TokenBucket.java)) allows bursts up to a
capacity and a steady refill rate; over the limit, return 429 with `Retry-After`. Per-instance
buckets are approximate; use Redis (Bucket4j) or the API gateway for a shared limit.

### Horizontal scaling and statelessness
**Level:** Intermediate

Run several identical instances behind a load balancer. That only works if instances are stateless:
no in-memory sessions (JWTs, module 13), shared caches or tolerant local ones (module 14), and
scheduled jobs that don't run everywhere (module 15).

### Scaling the database
**Level:** Advanced

Vertical scaling first. Then read replicas for read-heavy traffic (route `readOnly` transactions),
caching, partitioning large tables, and finally sharding, which makes queries and transactions
across shards hard. Most applications never need sharding.

### JVM tuning basics
**Level:** Advanced

Set container-aware memory (`-XX:MaxRAMPercentage=75`), use the default G1 collector unless
measurements say otherwise (ZGC for very low pause targets), and look at GC logs before changing
flags. Startup time matters for scaling out: consider Class Data Sharing, CRaC or a GraalVM native
image (module 24).

## Practice

### Load-test the catalog
**Level:** Intermediate

Write a k6 script that hits `GET /api/books` and `GET /api/books/{id}` with 50 virtual users for one
minute. Record p95 latency with and without the cache, and with virtual threads on and off.

### Per-user rate limits
**Level:** Advanced

Change `RateLimitFilter` to limit authenticated users by username and anonymous users by IP, with
different limits, and add `X-RateLimit-Remaining` headers. Then replace the map with Bucket4j and
Redis.

### Batch inserts
**Level:** Intermediate

Insert 10,000 books in a test with and without `hibernate.jdbc.batch_size=50` and
`order_inserts=true`, and compare statement counts and time. Explain why `IDENTITY` ids limit
batching and what `SEQUENCE` changes.

## Interview questions

- Your API's p99 latency doubled after a release. How do you investigate?
- How do you size a connection pool?
- What must be true for a service to scale horizontally?
- Explain the token-bucket algorithm.

## Further reading

- [HikariCP: About pool sizing](https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing)
- [Spring Boot: Virtual threads](https://docs.spring.io/spring-boot/reference/features/spring-application.html#features.spring-application.virtual-threads)
- [Grafana k6 documentation](https://grafana.com/docs/k6/latest/)
