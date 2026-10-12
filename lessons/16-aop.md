# 16 · Aspect-oriented programming

> **Phase:** Production-ready · **Week:** 8 · **Code:** [`aop/`](../src/main/java/com/zerotohero/aop) · **Tests:** [`ExecutionTimeAspectTest.java`](../src/test/java/com/zerotohero/aop/ExecutionTimeAspectTest.java)

Some concerns cut across many classes: timing, auditing, retries, security, transactions. AOP lets
you write them once and apply them declaratively. You've already used AOP through
`@Transactional`, `@Cacheable` and `@Async`; now you'll write an aspect yourself.

## Concepts

### Aspects, advice, pointcuts and join points
**Level:** Intermediate · **Code:** [ExecutionTimeAspect.java](../src/main/java/com/zerotohero/aop/ExecutionTimeAspect.java)

An *aspect* is a class of cross-cutting code. A *join point* is a point in execution (in Spring:
a method call). A *pointcut* selects join points (`@annotation(...LogExecutionTime)`). *Advice*
is the code that runs: `@Before`, `@AfterReturning`, `@AfterThrowing`, `@After`, `@Around`.

### Around advice
**Level:** Intermediate · **Code:** [ExecutionTimeAspect.java](../src/main/java/com/zerotohero/aop/ExecutionTimeAspect.java)

`@Around` receives a `ProceedingJoinPoint`, decides whether and when to call `proceed()`, and can
change arguments, results or exceptions. `ExecutionTimeAspect` times every call, records a
Micrometer timer tagged with method and outcome, and rethrows exceptions unchanged.

### Annotation-driven pointcuts
**Level:** Intermediate · **Code:** [LogExecutionTime.java](../src/main/java/com/zerotohero/aop/LogExecutionTime.java)

A custom annotation (`@LogExecutionTime`, with `RetentionPolicy.RUNTIME`) makes the pointcut
explicit: the reader of `OrderService` sees that the method is timed. Prefer this over broad
execution pointcuts like `execution(* com.zerotohero..*Service.*(..))`, which apply silently.

### Spring AOP proxies vs AspectJ weaving
**Level:** Advanced · **Code:** [ExecutionTimeAspectTest.java](../src/test/java/com/zerotohero/aop/ExecutionTimeAspectTest.java)

Spring AOP wraps beans in runtime proxies (CGLIB subclasses by default), so it only intercepts
public calls coming from outside the bean, and only on Spring beans. Full AspectJ weaving (compile
or load time) can intercept anything, including self-calls and constructors, at the cost of build
complexity. `AopUtils.isAopProxy(service)` shows the proxy in the test.

### When not to use AOP
**Level:** Intermediate

Aspects are invisible at the call site, so overuse makes code hard to follow and debug. Use them for
true cross-cutting technical concerns, not business rules. Order multiple aspects with `@Order`.
Filters and interceptors (module 06) are often clearer for HTTP-level concerns.

## Practice

### Audit admin actions
**Level:** Intermediate

Create `@Audited(action = "BOOK_DELETED")` and an aspect that, after a successful call, writes an
`AuditEntry` with the current username (from `SecurityContextHolder`) and the method arguments.
Apply it to `BookService.delete` and test it.

### A simple retry aspect
**Level:** Advanced

Write `@RetryOn(value = OptimisticLockingFailureException.class, attempts = 3)` and an around
aspect that retries the method. Make sure it runs *outside* the transaction aspect (each attempt
needs a fresh transaction) by setting `@Order`.

## Interview questions

- Explain join point, pointcut and advice.
- Why doesn't Spring AOP intercept a call from one method to another in the same class?
- JDK dynamic proxy vs CGLIB proxy?
- Name three Spring features built on AOP.

## Further reading

- [Spring Framework: Aspect Oriented Programming](https://docs.spring.io/spring-framework/reference/core/aop.html)
