# 04 · Spring Core: IoC and dependency injection

> **Phase:** Foundations · **Week:** 2 · **Code:** [`core/`](../src/main/java/com/zerotohero/core) · **Tests:** [`CoreConceptsTest.java`](../src/test/java/com/zerotohero/core/CoreConceptsTest.java), [`GreetingServiceUnitTest.java`](../src/test/java/com/zerotohero/core/GreetingServiceUnitTest.java)

Spring's core idea: you don't create your collaborators with `new`; you declare what you need and
the *container* creates and wires the objects (beans) for you. This is the foundation every other
Spring feature builds on.

## Concepts

### Inversion of control and the ApplicationContext
**Level:** Beginner

Without Spring, `OrderService` would call `new BookRepository(...)` itself and be stuck with it.
With inversion of control, the `ApplicationContext` creates every bean at startup, works out the
dependency graph, and hands each bean what it needs. You get loose coupling, easy testing and one
place to configure everything.

### Dependency injection: prefer constructors
**Level:** Beginner · **Code:** [GreetingService.java](../src/main/java/com/zerotohero/core/GreetingService.java)

Constructor injection makes dependencies explicit, lets fields be `final`, and means the object can
never exist half-built. With a single constructor no `@Autowired` is needed. Avoid field injection
(`@Autowired private Foo foo;`): it hides dependencies and needs reflection to test. See
[`GreetingServiceUnitTest`](../src/test/java/com/zerotohero/core/GreetingServiceUnitTest.java),
which tests the service with plain lambdas and no Spring at all.

### Stereotype annotations and component scanning
**Level:** Beginner · **Code:** [EnglishGreeter.java](../src/main/java/com/zerotohero/core/EnglishGreeter.java)

`@Component` marks a class as a bean. `@Service`, `@Repository` and `@Controller`/`@RestController`
are specialisations that document intent (and `@Repository` also translates database exceptions).
`@SpringBootApplication` scans its own package and everything below it.

### @Configuration and @Bean
**Level:** Beginner · **Code:** [BeanScopesConfig.java](../src/main/java/com/zerotohero/core/BeanScopesConfig.java)

Use `@Bean` methods for objects you can't annotate (library classes like `Clock`, `ObjectMapper`,
`RestClient`) or that need custom construction. Injecting a `Clock` instead of calling
`Instant.now()` makes time controllable in tests; every service in this repo does that.

### Choosing between several beans: @Primary and @Qualifier
**Level:** Beginner · **Code:** [GreetingService.java](../src/main/java/com/zerotohero/core/GreetingService.java)

With two `Greeter` beans, injecting `Greeter` is ambiguous. `@Primary` picks a default;
`@Qualifier("hindi")` asks for a specific one. You can also inject `List<Greeter>` or
`Map<String, Greeter>` to get all of them, which is how plugin-style strategies are built.

### Bean scopes
**Level:** Intermediate · **Code:** [BeanScopesConfig.java](../src/main/java/com/zerotohero/core/BeanScopesConfig.java)

`singleton` (default, one per container), `prototype` (new instance per lookup), and web scopes
`request` and `session`. Singletons are shared across threads, so they must be stateless or
thread-safe. Injecting a prototype into a singleton gives you one instance forever; use
`ObjectProvider<T>` to get a fresh one each time.

### Bean lifecycle
**Level:** Intermediate · **Code:** [LifecycleDemo.java](../src/main/java/com/zerotohero/core/LifecycleDemo.java)

Instantiate → inject dependencies → `@PostConstruct` → in use → `@PreDestroy` on shutdown. Use
`@PostConstruct` for warm-up that needs injected dependencies; use `@PreDestroy` to release
resources. For work that should wait until the whole app is ready, use `ApplicationRunner` or an
`ApplicationReadyEvent` listener (module 05).

### Type-safe configuration with @ConfigurationProperties
**Level:** Intermediate · **Code:** [GreetingProperties.java](../src/main/java/com/zerotohero/core/GreetingProperties.java)

Bind a whole prefix (`app.greeting.*`) to a record instead of scattering `@Value` strings. Add
`@Validated` and constraints so a bad value fails at startup, not at 3 a.m. The test
`failsFastOnInvalidConfiguration` proves it.

### Proxies: how Spring adds behaviour
**Level:** Advanced

`@Transactional`, `@Cacheable`, `@Async`, `@PreAuthorize` and your own aspects work because Spring
wraps the bean in a proxy that runs extra code around each call. Two consequences you must know:
calls from a bean to its own methods (`this.foo()`) skip the proxy, and only public methods are
intercepted by default. Module 10 and module 16 show this in practice.

## Practice

### Add a third greeter
**Level:** Beginner

Add a `TamilGreeter` bean named `"tamil"` and call it through `GreetingService.greetWith`. Write a
test with `ApplicationContextRunner` like the ones in `CoreConceptsTest`.

### Replace a hard-coded clock
**Level:** Beginner

Find code (yours or ours) that calls `LocalDate.now()` directly. Inject a `Clock` instead and write a
test that uses `Clock.fixed(...)` to check behaviour on a specific date.

### Strategy pattern with a Map of beans
**Level:** Intermediate

Create a `ShippingCalculator` interface with `standard`, `express` and `international` beans, and a
service that picks one by name from an injected `Map<String, ShippingCalculator>`. Adding a new
option should need no change to the service.

## Interview questions

- What is inversion of control, and how is dependency injection one way of doing it?
- Why is constructor injection preferred over field injection?
- What happens if two beans of the same type exist and you inject that type?
- Are Spring singletons thread-safe? What do you have to do to make a bean safe?
- Why does calling a `@Transactional` method from the same class not start a transaction?

## Further reading

- [Spring Framework: The IoC container](https://docs.spring.io/spring-framework/reference/core/beans.html)
- [Spring Boot: Type-safe configuration properties](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.typesafe-configuration-properties)
