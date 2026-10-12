# 14 · Caching

> **Phase:** Production-ready · **Week:** 8 · **Code:** [`cache/CacheConfig.java`](../src/main/java/com/zerotohero/cache/CacheConfig.java), [`book/BookService.java`](../src/main/java/com/zerotohero/book/BookService.java) · **Tests:** [`BookCachingTest.java`](../src/test/java/com/zerotohero/cache/BookCachingTest.java)

A cache keeps the result of an expensive call so the next identical call is cheap. Done well, it
cuts latency and database load; done badly, it serves stale or wrong data. "There are only two
hard things in computer science: cache invalidation and naming things."

## Concepts

### Spring's cache abstraction
**Level:** Beginner · **Code:** [BookService.java](../src/main/java/com/zerotohero/book/BookService.java)

`@EnableCaching`, then `@Cacheable(cacheNames = "books", key = "#id")` on a method: on a hit the
method isn't called at all. The abstraction is provider-independent: the same annotations work
with Caffeine, Redis, Hazelcast or a simple map.

### Keeping the cache correct: @CachePut and @CacheEvict
**Level:** Intermediate · **Code:** [BookService.java](../src/main/java/com/zerotohero/book/BookService.java)

`@CachePut` always runs the method and stores the new result (`update`); `@CacheEvict` removes an
entry (`delete`) or `allEntries = true` (placing an order changes stock, so `OrderService` evicts
all books). Every write path must update or evict, or readers get stale data.

### Caffeine: in-process caching
**Level:** Beginner · **Code:** [application.yml](../src/main/resources/application.yml)

`spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=10m` bounds size and age. In-process
caches are the fastest option but each instance has its own copy, so instances can disagree for up
to the expiry time. Always set a maximum size; an unbounded cache is a memory leak.

### Distributed caching with Redis
**Level:** Intermediate

Add `spring-boot-starter-data-redis`, set `spring.cache.type=redis` and `spring.data.redis.host`,
and every instance shares one cache. Values must be serializable (configure JSON serialization),
and every hit is a network call (~1 ms) instead of nanoseconds.

### Caching strategies
**Level:** Intermediate

*Cache-aside* (what `@Cacheable` does): read cache, on miss read the database and fill the cache.
*Write-through*: write to cache and database together. *Write-behind*: write to cache, flush to the
database later (risky). Choose TTLs per data type based on how stale it may be.

### Cache pitfalls
**Level:** Advanced

Self-invocation skips the cache proxy (module 10). Caching entities can leak lazy proxies; cache
DTOs. Cache stampede: many requests miss at once and all hit the database; use
`@Cacheable(sync = true)` or jittered TTLs. Don't cache per-user data under a shared key. Measure
hit rates (`cache.gets` metrics with `recordStats`) before and after.

### HTTP caching
**Level:** Intermediate

The cheapest request is the one the client never sends. `Cache-Control: max-age=60` lets browsers
and CDNs reuse a response; `ETag` + `If-None-Match` returns 304 Not Modified when nothing changed.
Spring's `ShallowEtagHeaderFilter` adds ETags; `ResponseEntity.ok().cacheControl(...)` sets headers.

## Practice

### Cache the book list
**Level:** Intermediate

Cache the first page of `GET /api/books` (no title filter) with a 30-second TTL in a separate cache.
Make sure creating, updating or deleting a book evicts it, and test that.

### Switch to Redis
**Level:** Advanced

Add Redis to `docker-compose.yml`, switch the cache type with a profile, configure a
`RedisCacheConfiguration` with JSON values and per-cache TTLs, and run two app instances to see
them share entries.

### Add ETags
**Level:** Intermediate

Return an `ETag` based on the book's `version` from `GET /api/books/{id}` and answer 304 when the
client sends a matching `If-None-Match`. Test it with MockMvc.

## Interview questions

- What's the difference between `@Cacheable`, `@CachePut` and `@CacheEvict`?
- Local vs distributed cache: trade-offs?
- What is a cache stampede and how do you prevent it?
- Why cache DTOs rather than JPA entities?

## Further reading

- [Spring Boot: Caching](https://docs.spring.io/spring-boot/reference/io/caching.html)
- [Spring Framework: Cache abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [Caffeine wiki](https://github.com/ben-manes/caffeine/wiki)
