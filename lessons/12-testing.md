# 12 · Testing Spring Boot applications

> **Phase:** Building APIs · **Week:** 6 · **Code:** every file under [`src/test`](../src/test/java/com/zerotohero) · **Run:** `sh run-tests.sh`

Tests let you change code without fear. Spring Boot gives you a ladder of test types, from
millisecond unit tests to full-application tests. Use the cheapest one that proves what you need.

## Concepts

### The testing pyramid
**Level:** Beginner

Many fast unit tests, fewer slice tests, a handful of full integration tests, very few end-to-end
tests. Unit tests find logic bugs; integration tests find wiring, SQL and configuration bugs. This
repo has all levels: about 86 tests that run in under a minute.

### JUnit 5 and AssertJ
**Level:** Beginner · **Code:** [JavaFeaturesTest.java](../src/test/java/com/zerotohero/basics/JavaFeaturesTest.java)

`@Test`, `@BeforeEach`, `@ParameterizedTest` with `@ValueSource`/`@CsvSource`
([`IsbnValidatorTest`](../src/test/java/com/zerotohero/validation/IsbnValidatorTest.java)),
`@Nested`, `@DisplayName`. AssertJ's fluent assertions (`assertThat(list).extracting(...).containsExactly(...)`,
`assertThatThrownBy(...)`) give readable failures. Name tests after behaviour:
`rejectsADuplicateIsbn`.

### Unit tests with Mockito
**Level:** Beginner · **Code:** [BookServiceTest.java](../src/test/java/com/zerotohero/book/BookServiceTest.java)

`@ExtendWith(MockitoExtension.class)`, `@Mock` collaborators, `@InjectMocks` the class under test.
Stub with `when(...).thenReturn(...)`, check interactions with `verify(...)`. Mock what you don't
own sparingly; prefer real objects for simple values. Don't mock the class under test.

### Web slice: @WebMvcTest and MockMvc
**Level:** Intermediate · **Code:** [BookControllerWebTest.java](../src/test/java/com/zerotohero/book/BookControllerWebTest.java)

Starts only controllers, advice, filters and security, with services replaced by `@MockitoBean`.
Test the HTTP contract: status codes, JSON fields (`jsonPath`), headers, validation errors, and
authorization with `@WithMockUser(roles = "ADMIN")`.

### Data slice: @DataJpaTest
**Level:** Intermediate · **Code:** [BookRepositoryTest.java](../src/test/java/com/zerotohero/book/BookRepositoryTest.java)

Starts JPA, Flyway and an embedded database; each test runs in a transaction that is rolled back.
Test custom queries, projections, constraints and query counts.

### Full application: @SpringBootTest
**Level:** Intermediate · **Code:** [SecurityIntegrationTest.java](../src/test/java/com/zerotohero/security/SecurityIntegrationTest.java)

Starts the whole context. With `@AutoConfigureMockMvc` you send real requests through the full
filter chain without a network port; with `webEnvironment = RANDOM_PORT` you can use a real HTTP
client (`TestRestTemplate`/`WebTestClient`). Spring caches contexts between test classes with the
same configuration, so keep configurations few to keep the suite fast.

### Testing asynchronous code
**Level:** Intermediate · **Code:** [OrderServiceTest.java](../src/test/java/com/zerotohero/order/OrderServiceTest.java)

Don't `Thread.sleep`. Use Awaitility: `await().atMost(5, SECONDS).untilAsserted(...)`. For
scheduled jobs, call the method directly. For time-based logic, inject a `Clock` or a fake nano
clock ([`TokenBucketTest`](../src/test/java/com/zerotohero/ratelimit/TokenBucketTest.java)).

### Testing HTTP clients
**Level:** Intermediate · **Code:** [ExchangeRateClientTest.java](../src/test/java/com/zerotohero/client/ExchangeRateClientTest.java)

`@RestClientTest` with `MockRestServiceServer` fakes the remote server for `RestClient` and
`RestTemplate`. For WebClient or full-fidelity fakes, use WireMock.

### Testcontainers
**Level:** Advanced

Run the real PostgreSQL, Kafka or Redis in Docker during tests:
`@Testcontainers` + `@Container @ServiceConnection static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:17");`.
`@ServiceConnection` wires the connection details automatically. Slower than H2, but tests the
database you actually run.

### Test data and isolation
**Level:** Intermediate

Tests must not depend on each other's order. Use rollback (`@DataJpaTest`), unique data per test,
or assertions relative to a "before" value (as `OrderServiceTest` does with stock). Avoid
`@DirtiesContext` unless you must; it throws away the cached context.

## Practice

### Test the authors API
**Level:** Beginner

Write `@WebMvcTest` tests for the authors controller from module 06, covering 200, 404 and 400.

### Switch to Testcontainers
**Level:** Advanced

Add `org.testcontainers:postgresql` and `spring-boot-testcontainers` (test scope). Create a
`@TestConfiguration` with a `@ServiceConnection` PostgreSQL container and run `FlywayMigrationTest`
against it. Note anything H2 allowed that PostgreSQL didn't.

### Measure coverage
**Level:** Intermediate

Add the JaCoCo Maven plugin, run `mvn verify`, open the report and add tests for the least-covered
class. Decide which uncovered lines aren't worth testing and why.

## Interview questions

- When would you use `@WebMvcTest` instead of `@SpringBootTest`?
- `@Mock` vs `@MockitoBean`: what's the difference?
- How do you keep a Spring test suite fast?
- How do you test code that runs on another thread?
- Why might a test pass on H2 but fail on PostgreSQL?

## Further reading

- [Spring Boot: Testing](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [JUnit 5 user guide](https://junit.org/junit5/docs/current/user-guide/)
- [Testcontainers for Java](https://java.testcontainers.org/)
