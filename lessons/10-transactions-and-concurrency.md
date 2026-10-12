# 10 · Transactions and concurrency

> **Phase:** Building APIs · **Week:** 5 · **Code:** [`order/OrderService.java`](../src/main/java/com/zerotohero/order/OrderService.java), [`order/AuditService.java`](../src/main/java/com/zerotohero/order/AuditService.java) · **Tests:** [`OrderServiceTest.java`](../src/test/java/com/zerotohero/order/OrderServiceTest.java)

A transaction makes several database changes succeed or fail together. Placing an order reads
books, reduces stock, writes the order and its lines, and records an outbox event: if any step
fails, none of it should remain. And when two customers buy the last copy at the same moment, only
one may win.

## Concepts

### @Transactional basics
**Level:** Beginner · **Code:** [OrderService.java](../src/main/java/com/zerotohero/order/OrderService.java)

Put `@Transactional` on service methods that represent one business operation. Spring opens a
transaction before the method and commits after it, or rolls back if it throws. Put it on the
service, not the controller or repository.

### Rollback rules
**Level:** Beginner · **Code:** [OrderServiceTest.java](../src/test/java/com/zerotohero/order/OrderServiceTest.java)

By default only unchecked exceptions (`RuntimeException`, `Error`) roll back; checked exceptions
commit. Change it with `rollbackFor`/`noRollbackFor`. `failureRollsBackEverythingButTheRequiresNewAuditEntry`
shows a two-line order where the second line fails and the first line's stock change is undone.

### Read-only transactions
**Level:** Beginner · **Code:** [BookService.java](../src/main/java/com/zerotohero/book/BookService.java)

`@Transactional(readOnly = true)` lets Hibernate skip dirty checking and lets drivers route to
replicas. `BookService` is read-only at class level and overrides it on writing methods.

### Propagation
**Level:** Intermediate · **Code:** [AuditService.java](../src/main/java/com/zerotohero/order/AuditService.java)

`REQUIRED` (default) joins an existing transaction or starts one. `REQUIRES_NEW` suspends the
caller's and runs independently: the audit entry commits even when the order fails. `MANDATORY`
throws if there is no transaction ([`OutboxService`](../src/main/java/com/zerotohero/messaging/OutboxService.java)
uses it to guarantee the event is saved with the order). Also know `SUPPORTS`, `NOT_SUPPORTED`,
`NEVER` and `NESTED`.

### The self-invocation trap
**Level:** Intermediate · **Code:** [OrderService.java](../src/main/java/com/zerotohero/order/OrderService.java)

Transactions are applied by a proxy, so `this.placeOrder()` from inside the same class skips it:
no new transaction, no cache eviction, no timing aspect. That's why `AuditService` is a separate
bean, and why `placeOrderIdempotent` adds its own `@CacheEvict`. Private and final methods aren't
proxied either.

### Isolation levels in practice
**Level:** Intermediate

`@Transactional(isolation = Isolation.REPEATABLE_READ)` raises isolation for one method. Most apps
keep the database default (READ COMMITTED) and solve specific races with locking or constraints
instead, because higher isolation causes more conflicts and retries.

### Optimistic locking with @Version
**Level:** Intermediate · **Code:** [Book.java](../src/main/java/com/zerotohero/book/Book.java)

A `@Version` column makes every UPDATE include `where version = ?`. If someone else changed the row
first, zero rows match and Spring throws `ObjectOptimisticLockingFailureException`, which
[`GlobalExceptionHandler`](../src/main/java/com/zerotohero/error/GlobalExceptionHandler.java) maps to
409. Use it when conflicts are rare; on a conflict, reload and retry or ask the user.

### Pessimistic locking
**Level:** Advanced

`@Lock(LockModeType.PESSIMISTIC_WRITE)` on a repository method issues `SELECT ... FOR UPDATE`, so
other transactions wait. Use it for hot rows with frequent conflicts (a ticket counter), keep the
transaction short, and lock rows in a consistent order to avoid deadlocks. An atomic update
(`update books set stock = stock - :q where id = :id and stock >= :q`) is often simpler still.

### Transactions and side effects
**Level:** Advanced · **Code:** [OrderEventsListener.java](../src/main/java/com/zerotohero/async/OrderEventsListener.java)

Don't send emails or call other services inside a transaction that might roll back. Publish an
event and handle it with `@TransactionalEventListener(phase = AFTER_COMMIT)` (module 15), or write
to an outbox table (module 20). Also keep transactions short: no remote calls while holding locks.

## Practice

### Cancel an order
**Level:** Intermediate

Add `POST /api/orders/{id}/cancel` that sets status to CANCELLED and returns the stock, all in one
transaction. Only PLACED orders can be cancelled. Test that a failure halfway leaves nothing
changed.

### Prove the self-invocation trap
**Level:** Intermediate

Write a test where a method in `AuditService` calls another `REQUIRES_NEW` method on `this` and
show (with a rollback) that no new transaction was created. Then fix it by moving the method.

### Race for the last copy
**Level:** Advanced

Write a test that starts two threads ordering the last copy of a book at the same time (use a
`CountDownLatch`). Show that one fails with an optimistic-lock or stock error and stock never
goes below zero. Then implement a retry with `@Retryable` or a loop and test it.

## Interview questions

- Which exceptions trigger a rollback by default?
- Explain `REQUIRED` vs `REQUIRES_NEW` with an example.
- Why doesn't `@Transactional` work on a private method or a self-call?
- Optimistic vs pessimistic locking: when would you choose each?
- Why is sending an email inside a transaction a bad idea?

## Further reading

- [Spring Framework: Transaction management](https://docs.spring.io/spring-framework/reference/data-access/transaction.html)
- [Spring Data JPA: Locking](https://docs.spring.io/spring-data/jpa/reference/jpa/locking.html)
