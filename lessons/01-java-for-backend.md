# 01 · Java for backend developers

> **Phase:** Foundations · **Week:** 1 · **Code:** [`basics/JavaFeatures.java`](../src/main/java/com/zerotohero/basics/JavaFeatures.java) · **Tests:** [`JavaFeaturesTest.java`](../src/test/java/com/zerotohero/basics/JavaFeaturesTest.java)

Spring Boot is "just Java". Every later module assumes you can read and write modern Java
comfortably: classes and interfaces, collections, lambdas and streams, records, exceptions, and
the Java 21 features that Spring Boot 3 is built on. You don't need to know all of Java; you need
the parts below fluently.

Install JDK 21 (Temurin is a good default), an IDE (IntelliJ IDEA Community or VS Code with the
Java extension pack), and check with `java -version`.

## Concepts

### Classes, objects and interfaces
**Level:** Beginner

A class bundles state (fields) and behaviour (methods). An interface is a contract: a list of
methods without a fixed implementation. Backend code leans heavily on interfaces because they let
you swap implementations, which is the basis of dependency injection in module 04.

```java
public interface PaymentGateway { Receipt charge(Money amount); }
public class StripeGateway implements PaymentGateway { ... }
public class FakeGateway implements PaymentGateway { ... } // for tests
```

Learn: access modifiers (`private`, `public`, package-private), `final` fields, constructors,
`static` members, `equals`/`hashCode` contracts, and composition over inheritance.

### Collections: List, Set, Map
**Level:** Beginner

`ArrayList` for ordered data, `HashSet` for uniqueness, `HashMap` for lookups by key,
`LinkedHashMap` when insertion order matters, `TreeMap` when keys must be sorted. Prefer the
immutable factories `List.of`, `Set.of`, `Map.of` for fixed data; they throw on `null` and on
modification. Know the cost of the common operations: `HashMap.get` is O(1) on average,
`ArrayList.contains` is O(n).

### Lambdas and functional interfaces
**Level:** Beginner · **Code:** [JavaFeatures.java](../src/main/java/com/zerotohero/basics/JavaFeatures.java)

A lambda is a short anonymous function: `p -> p.price()`. It can be used wherever a *functional
interface* (an interface with one abstract method) is expected: `Function<T,R>`, `Predicate<T>`,
`Supplier<T>`, `Consumer<T>`. Method references (`Product::price`) are a shorter form. See
`discountThenTax`, which builds one function from two with `andThen`.

### Streams
**Level:** Beginner · **Code:** [JavaFeatures.java](../src/main/java/com/zerotohero/basics/JavaFeatures.java)

Streams describe a pipeline over a collection: `filter`, `map`, `sorted`, `limit`, then a terminal
operation such as `toList()`, `count()` or `collect(...)`. `Collectors.groupingBy` is the stream
version of SQL's `GROUP BY` (see `totalByCategory`). Streams are lazy and single-use. Use a plain
loop when it's clearer, especially when you need to throw checked exceptions or break early.

### Optional
**Level:** Beginner · **Code:** [JavaFeatures.java](../src/main/java/com/zerotohero/basics/JavaFeatures.java)

`Optional<T>` says in the type that a value may be absent. Spring Data's `findById` returns one.
Use `map`, `filter`, `orElse`, `orElseThrow`; avoid `get()` without a check. Use `Optional` for
return values, not for fields or method parameters.

### Records
**Level:** Beginner · **Code:** [JavaFeatures.java](../src/main/java/com/zerotohero/basics/JavaFeatures.java)

`record Product(String name, String category, BigDecimal price)` gives you a constructor,
accessors, `equals`, `hashCode` and `toString`. Records are immutable and perfect for DTOs
(request and response bodies), configuration properties and events. A compact constructor
validates input once so every instance is valid.

### Exceptions: checked vs unchecked
**Level:** Beginner · **Code:** [JavaFeatures.java](../src/main/java/com/zerotohero/basics/JavaFeatures.java)

Checked exceptions (`IOException`) must be declared or caught; unchecked ones
(`RuntimeException` and subclasses) need not be. Spring prefers unchecked exceptions, and by
default `@Transactional` only rolls back on unchecked ones (module 10). Wrap low-level checked
exceptions in a meaningful unchecked exception and keep the cause, as `parsePort` does. Use
try-with-resources for anything `AutoCloseable`.

### Generics
**Level:** Intermediate

`List<Book>`, `Optional<T>`, `JpaRepository<Book, Long>`, `PageResponse<T>`: generics let one class
work with many types safely. Learn bounded types (`<T extends Comparable<T>>`) and wildcards
(`List<? extends Number>`), and read them in Spring's signatures. See
[`PageResponse`](../src/main/java/com/zerotohero/web/PageResponse.java) for a generic static factory.

### Sealed types and pattern matching
**Level:** Intermediate · **Code:** [JavaFeatures.java](../src/main/java/com/zerotohero/basics/JavaFeatures.java)

A `sealed interface Payment permits Card, Upi, Cash` lists every subtype. A `switch` over it with
record patterns (`case Card(String last4, BigDecimal amount) ->`) needs no `default`, and the
compiler tells you when a new subtype is unhandled. Guards (`when amount > ...`) refine a case.
This models domain states (order status, payment type) far better than `instanceof` chains.

### BigDecimal for money
**Level:** Beginner

Never use `double` for money: `0.1 + 0.2 != 0.3`. Use `BigDecimal`, create it from a `String`,
compare with `compareTo` (not `equals`, which also compares scale), and always say how to round:
`setScale(2, RoundingMode.HALF_UP)`. The bookstore stores prices as `numeric(10,2)` and maps them
to `BigDecimal`.

### Concurrency basics and virtual threads
**Level:** Intermediate · **Code:** [JavaFeatures.java](../src/main/java/com/zerotohero/basics/JavaFeatures.java)

A web server handles many requests at once, so your code runs on many threads. Shared mutable
state needs care: prefer immutable objects, `ConcurrentHashMap`, `AtomicInteger`, or no sharing at
all. Java 21's virtual threads are cheap enough to create one per task, so blocking I/O no longer
needs a big thread pool. `runBlockingTasks` runs 1,000 tasks that each sleep 100 ms in well under
a second. Module 23 switches the whole server to virtual threads.

## Practice

### Group orders by customer
**Level:** Beginner

Write `Map<String, BigDecimal> spendByCustomer(List<Order> orders)` with a stream, where
`Order` is a record of customer and amount. Then return the top 3 customers by spend.

### Model order states with a sealed interface
**Level:** Intermediate

Create `sealed interface OrderState permits Placed, Paid, Shipped, Cancelled` as records carrying
the data each state needs (e.g. `Shipped(String trackingId)`). Write
`String nextAction(OrderState s)` with a pattern-matching switch, then add a `Refunded` state and
watch the compiler point at the switch.

### Measure platform vs virtual threads
**Level:** Intermediate

Run 10,000 tasks that each sleep 50 ms, first with `Executors.newFixedThreadPool(100)` and then
with `Executors.newVirtualThreadPerTaskExecutor()`. Time both and explain the difference.

## Interview questions

- What is the contract between `equals` and `hashCode`, and what breaks in a `HashMap` if you violate it?
- Why are records a good fit for DTOs? What can't a record do?
- When would you choose a checked exception over an unchecked one?
- Why is `BigDecimal.equals` surprising, and what should you use instead?
- What problem do virtual threads solve, and what do they not solve (hint: CPU-bound work, `synchronized` pinning in older JDKs)?

## Further reading

- [dev.java: Learn Java](https://dev.java/learn/)
- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
- [JEP 441: Pattern Matching for switch](https://openjdk.org/jeps/441)
- *Effective Java*, 3rd edition, by Joshua Bloch
