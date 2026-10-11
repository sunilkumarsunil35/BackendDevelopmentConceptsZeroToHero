# 05 · Spring Boot essentials

> **Phase:** Foundations · **Week:** 3 · **Code:** [`ZeroToHeroApplication.java`](../src/main/java/com/zerotohero/ZeroToHeroApplication.java), [`boot/`](../src/main/java/com/zerotohero/boot), [`application.yml`](../src/main/resources/application.yml) · **Tests:** [`ApplicationStartupTest.java`](../src/test/java/com/zerotohero/boot/ApplicationStartupTest.java)

Spring Boot takes the Spring Framework and makes it start with almost no configuration: an
embedded web server, sensible defaults, and auto-configuration that reacts to what's on the
classpath. This module covers what happens when you press Run.

## Concepts

### @SpringBootApplication and the main method
**Level:** Beginner · **Code:** [ZeroToHeroApplication.java](../src/main/java/com/zerotohero/ZeroToHeroApplication.java)

`@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.
`SpringApplication.run` creates the context, starts embedded Tomcat on port 8080 and blocks. The
app is a plain `java -jar` program; no external application server is needed.

### Auto-configuration
**Level:** Intermediate

Boot ships hundreds of `@AutoConfiguration` classes guarded by conditions such as
`@ConditionalOnClass(DataSource.class)` and `@ConditionalOnMissingBean`. Add the H2 driver and you
get a `DataSource`; define your own `DataSource` bean and Boot's backs off. Run with `--debug` (or
`debug=true`) to print the *conditions evaluation report* and see what was configured and why.

### application.yml and externalized configuration
**Level:** Beginner · **Code:** [application.yml](../src/main/resources/application.yml)

Configuration comes from many sources, with a fixed precedence: command-line args beat environment
variables, which beat `application-{profile}.yml`, which beats `application.yml`. Relaxed binding
maps `APP_JWT_SECRET` to `app.jwt.secret`. Keep secrets out of the repo: use
`${APP_JWT_SECRET:dev-default}` placeholders and set real values in the environment.

### Profiles
**Level:** Beginner · **Code:** [StartupLogger.java](../src/main/java/com/zerotohero/boot/StartupLogger.java)

`SPRING_PROFILES_ACTIVE=postgres` loads `application-postgres.yml` on top of the defaults.
`@Profile("!test")` makes a bean exist only outside tests. Use profiles for environment
differences (database URL, log level), not for feature flags.

### Reading configuration in code
**Level:** Beginner · **Code:** [AppInfoController.java](../src/main/java/com/zerotohero/boot/AppInfoController.java)

`@Value("${spring.application.name}")` for single values, `@ConfigurationProperties` for groups
(module 04), and `Environment` when you need to look things up dynamically (like active profiles).

### Logging
**Level:** Beginner

Boot uses SLF4J with Logback. Log with `private static final Logger log = LoggerFactory.getLogger(X.class)`
and placeholders: `log.info("Order {} placed", id)`, never string concatenation. Set levels per
package: `logging.level.com.zerotohero=DEBUG`. Levels: ERROR (someone must act), WARN (unexpected
but handled), INFO (business events), DEBUG (diagnostics). Never log passwords, tokens or card
numbers.

### Startup hooks and the embedded server
**Level:** Intermediate · **Code:** [StartupLogger.java](../src/main/java/com/zerotohero/boot/StartupLogger.java)

`ApplicationRunner`/`CommandLineRunner` run once after startup. `server.port`, `server.shutdown=graceful`
and `spring.lifecycle.timeout-per-shutdown-phase` control the embedded server. Graceful shutdown
(the default since Boot 3.4) lets in-flight requests finish before the process exits, which matters in Kubernetes (module 24).

### DevTools and a fast feedback loop
**Level:** Beginner

Add `spring-boot-devtools` (scope `runtime`, optional) to restart the app automatically when classes
change. Pair it with your IDE's build-on-save. It's disabled automatically in a packaged jar.

## Practice

### Add a profile
**Level:** Beginner

Create `application-dev.yml` that sets `logging.level.com.zerotohero=DEBUG` and changes
`app.greeting.prefix`. Run with `--spring.profiles.active=dev` and check `/api/info` and
`/api/hello`.

### Read the auto-configuration report
**Level:** Intermediate

Start the app with `--debug`. Find `DataSourceAutoConfiguration`, `FlywayAutoConfiguration` and
`CacheAutoConfiguration` in the report and explain why each matched.

### Write your own auto-configuration
**Level:** Advanced

Create a tiny `greeting-starter` module with an `@AutoConfiguration` class that registers a
`Greeter` only `@ConditionalOnMissingBean`. Register it in
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` and use it from
another project.

## Interview questions

- What does `@SpringBootApplication` do?
- How does Spring Boot decide which beans to auto-configure, and how do you override one?
- In what order are configuration sources applied?
- How would you pass a database password to a Boot app in production?

## Further reading

- [Spring Boot: Externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html)
- [Spring Boot: Profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html)
- [Spring Boot: Creating your own auto-configuration](https://docs.spring.io/spring-boot/reference/features/developing-auto-configuration.html)
