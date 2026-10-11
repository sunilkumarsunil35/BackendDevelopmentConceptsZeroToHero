# 03 · Build tools and project setup

> **Phase:** Foundations · **Week:** 2 · **Code:** [`pom.xml`](../pom.xml), [`run-tests.sh`](../run-tests.sh)

A Spring Boot project is a normal Maven (or Gradle) project with a parent that manages dependency
versions for you. This module explains what's in this repo's `pom.xml` and how the source tree is
laid out.

## Concepts

### Maven basics: the POM
**Level:** Beginner · **Code:** [pom.xml](../pom.xml)

`pom.xml` declares the project's coordinates (`groupId`, `artifactId`, `version`), its
dependencies and its plugins. Maven downloads dependencies from Maven Central into `~/.m2`.
Dependency *scopes* matter: `compile` (default), `runtime` (only needed when running, like the H2
and PostgreSQL drivers), `test` (only for tests).

### The Maven lifecycle
**Level:** Beginner

`mvn compile` → `mvn test` → `mvn package` (builds `target/*.jar`) → `mvn verify` → `mvn install`.
Each phase runs all earlier ones. `mvn spring-boot:run` starts the app from source. Add `-q` for
quiet output and `-B` for batch mode in CI.

### Spring Boot starters and the parent BOM
**Level:** Beginner · **Code:** [pom.xml](../pom.xml)

`spring-boot-starter-parent` pins compatible versions of hundreds of libraries, which is why most
dependencies in the POM have no `<version>`. A *starter* such as `spring-boot-starter-web` is a
curated bundle (Spring MVC, Jackson, embedded Tomcat). Only add a version when a library isn't
managed by Boot (springdoc and Resilience4j here).

### Spring Initializr
**Level:** Beginner

[start.spring.io](https://start.spring.io) generates a ready-to-run project: pick Maven, Java 21,
the latest stable Boot version and your starters. Your IDE has the same wizard built in. Generate
one now and compare it with this repo.

### Project layout
**Level:** Beginner

```
src/main/java/com/zerotohero/      application code, one package per feature
src/main/resources/application.yml configuration
src/main/resources/db/migration/   Flyway SQL migrations
src/test/java/com/zerotohero/      tests, mirroring the main packages
```

Organise by *feature* (`book`, `order`, `security`) rather than by layer (`controllers`,
`services`). Keep the main application class in the root package so component scanning finds
everything below it.

### Gradle in one page
**Level:** Intermediate

Gradle does the same job with a Groovy or Kotlin build script. `implementation` ≈ compile scope,
`runtimeOnly` ≈ runtime, `testImplementation` ≈ test; `./gradlew bootRun`, `./gradlew test`,
`./gradlew bootJar`. Many companies use either, so be able to read both.

### Wrappers and reproducible builds
**Level:** Intermediate

`mvnw` / `gradlew` wrapper scripts download the exact build-tool version the project expects, so
every developer and CI machine builds the same way. Generate one with `mvn wrapper:wrapper`. Commit
it; never commit `target/` or `build/` (see [`.gitignore`](../.gitignore)).

## Practice

### Generate and run a project
**Level:** Beginner

Use start.spring.io to create a project with the Web starter, add a `@RestController` that returns
`"pong"` at `/ping`, run it with `mvn spring-boot:run` and call it with curl.

### Read the dependency tree
**Level:** Intermediate

Run `mvn dependency:tree` in this repo. Find which library brings in Jackson, which brings in
Hibernate, and which version of Tomcat is used. Then find where that version comes from.

## Interview questions

- What does `spring-boot-starter-parent` give you?
- What is the difference between `compile`, `runtime`, `provided` and `test` scopes?
- How do you resolve two libraries needing different versions of the same dependency?

## Further reading

- [Maven: Getting started](https://maven.apache.org/guides/getting-started/)
- [Spring Boot: Build systems](https://docs.spring.io/spring-boot/reference/using/build-systems.html)
- [Spring Initializr](https://start.spring.io)
