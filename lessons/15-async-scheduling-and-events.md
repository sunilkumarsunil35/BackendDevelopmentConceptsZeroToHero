# 15 · Async processing, scheduling and events

> **Phase:** Production-ready · **Week:** 8 · **Code:** [`async/`](../src/main/java/com/zerotohero/async), [`order/OrderPlacedEvent.java`](../src/main/java/com/zerotohero/order/OrderPlacedEvent.java) · **Tests:** [`OrderServiceTest.java`](../src/test/java/com/zerotohero/order/OrderServiceTest.java), [`LowStockReporterTest.java`](../src/test/java/com/zerotohero/async/LowStockReporterTest.java)

Not everything should happen while the client waits. Emails, reports and cleanups can run in the
background, on a schedule, or in reaction to events, keeping requests fast and code decoupled.

## Concepts

### Application events
**Level:** Beginner · **Code:** [OrderService.java](../src/main/java/com/zerotohero/order/OrderService.java)

`ApplicationEventPublisher.publishEvent(new OrderPlacedEvent(...))` announces that something
happened; any number of `@EventListener` methods react. The publisher doesn't know the listeners,
so adding a new reaction doesn't touch `OrderService`. By default listeners run synchronously, on
the same thread, inside the same transaction.

### Transactional event listeners
**Level:** Intermediate · **Code:** [OrderEventsListener.java](../src/main/java/com/zerotohero/async/OrderEventsListener.java)

`@TransactionalEventListener(phase = AFTER_COMMIT)` runs only if the publishing transaction
commits, so no confirmation email for an order that rolled back. The test
`noConfirmationWhenTheOrderRollsBack` proves it. Other phases: `BEFORE_COMMIT`, `AFTER_ROLLBACK`,
`AFTER_COMPLETION`.

### @Async
**Level:** Intermediate · **Code:** [NotificationService.java](../src/main/java/com/zerotohero/async/NotificationService.java)

`@EnableAsync` + `@Async` on a public method of another bean runs it on a task executor. Return
`void` or `CompletableFuture<T>`. Exceptions in `void` methods go to an
`AsyncUncaughtExceptionHandler`, so log them. Like transactions, it's proxy-based: self-calls run
synchronously.

### Executors and thread pools
**Level:** Intermediate

Without virtual threads, configure a bounded `ThreadPoolTaskExecutor` (`spring.task.execution.pool.*`):
core size, max size and queue capacity, and decide what happens when the queue is full. With
`spring.threads.virtual.enabled=true` (this repo), Boot uses virtual threads for `@Async`, so you
limit concurrency with a semaphore when a downstream system needs protecting. Security and MDC
context don't propagate to new threads automatically; use a `TaskDecorator`.

### CompletableFuture composition
**Level:** Intermediate

Run independent calls in parallel and combine them:
`supplyAsync(a).thenCombine(supplyAsync(b), merge)`, `allOf(...)`, `exceptionally(...)`,
`orTimeout(...)`. Useful for fan-out to several services in one request.

### @Scheduled jobs
**Level:** Beginner · **Code:** [LowStockReporter.java](../src/main/java/com/zerotohero/async/LowStockReporter.java)

`@EnableScheduling` + `@Scheduled(fixedRate = ...)`, `fixedDelay` (waits after the previous run
ends), or `cron = "0 0 9 * * MON-FRI"` (Spring cron has six fields, starting with seconds). Keep the
schedule in configuration so tests and environments can change it. Test the job by calling it.

### Scheduling in a cluster
**Level:** Advanced

Three instances means the job runs three times. Use a distributed lock (ShedLock with a database
table, `@SchedulerLock(name = "lowStockReport")`), a single "leader" instance, or move the work to a
queue consumed by one worker. Make jobs idempotent so a double run is harmless.

## Practice

### Daily sales summary
**Level:** Intermediate

Add a scheduled job that, once a day, computes yesterday's order count and revenue and stores it in
a `daily_sales` table. Use an injected `Clock` and test it for a fixed date.

### Parallel enrichment
**Level:** Intermediate

Add an endpoint that returns a book with its price in three currencies, fetching the three rates in
parallel with `CompletableFuture` and a 2-second overall timeout.

### Add ShedLock
**Level:** Advanced

Add ShedLock with the JDBC provider and a Flyway migration for its table, protect
`LowStockReporter`, and explain what `lockAtMostFor` and `lockAtLeastFor` protect against.

## Interview questions

- `@EventListener` vs `@TransactionalEventListener`?
- What happens to an exception thrown in an `@Async void` method?
- `fixedRate` vs `fixedDelay`?
- How do you stop a scheduled job running on every instance?

## Further reading

- [Spring Framework: Task execution and scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [Spring Boot: Task execution and scheduling](https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html)
- [ShedLock](https://github.com/lukas-krecan/ShedLock)
