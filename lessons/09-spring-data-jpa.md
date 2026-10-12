# 09 · Spring Data JPA and Hibernate

> **Phase:** Building APIs · **Week:** 5 · **Code:** [`book/`](../src/main/java/com/zerotohero/book), [`order/Order.java`](../src/main/java/com/zerotohero/order/Order.java) · **Tests:** [`BookRepositoryTest.java`](../src/test/java/com/zerotohero/book/BookRepositoryTest.java)

JPA is the Java standard for mapping objects to tables; Hibernate implements it; Spring Data JPA
writes repository implementations for you. Together they remove most boilerplate, as long as you
understand what SQL they generate.

## Concepts

### Entities and mapping
**Level:** Beginner · **Code:** [Book.java](../src/main/java/com/zerotohero/book/Book.java)

`@Entity` + `@Table`, `@Id` + `@GeneratedValue(strategy = IDENTITY)`, `@Column(nullable, length, precision, scale)`.
Entities need a no-arg constructor (it can be `protected`). Put domain rules on the entity
(`Book.removeStock`) instead of exposing setters for everything.

### Repositories and derived queries
**Level:** Beginner · **Code:** [BookRepository.java](../src/main/java/com/zerotohero/book/BookRepository.java)

Extend `JpaRepository<Book, Long>` and get `save`, `findById`, `findAll`, `delete`, paging and more.
Method names become queries: `findByIsbn`, `existsByIsbn`, `countByAuthorId`,
`findByTitleContainingIgnoreCase(String, Pageable)`.

### @Query with JPQL and native SQL
**Level:** Intermediate · **Code:** [BookRepository.java](../src/main/java/com/zerotohero/book/BookRepository.java)

When names get long, write JPQL (queries over entities, not tables) with named parameters:
`@Query("select b from Book b where b.stock < :threshold")`. Use `nativeQuery = true` for
database-specific SQL. Use `@Modifying` for bulk updates and remember they bypass the persistence
context.

### Relationships and fetch types
**Level:** Intermediate · **Code:** [Order.java](../src/main/java/com/zerotohero/order/Order.java)

`@ManyToOne` (always set `fetch = LAZY`; the default is EAGER), `@OneToMany(mappedBy = ...)` for the
inverse side, `cascade = ALL` and `orphanRemoval = true` when the parent owns the children
(Order → OrderItem). Keep both sides in sync in one helper method (`Order.addItem`). Avoid
`@ManyToMany` with extra columns; use a join entity.

### The persistence context and dirty checking
**Level:** Intermediate · **Code:** [BookService.java](../src/main/java/com/zerotohero/book/BookService.java)

Inside a transaction, each loaded entity is *managed*. Change a field and Hibernate writes the
UPDATE at flush/commit; no `save()` needed (`BookService.update`). Entity states: transient →
managed → detached → removed. Outside a transaction, changes are not tracked.

### The N+1 problem and how to fix it
**Level:** Intermediate · **Code:** [BookRepositoryTest.java](../src/test/java/com/zerotohero/book/BookRepositoryTest.java)

Loading 10 books and then touching each lazy author runs 1 + 10 queries. Fix with a fetch join
(`join fetch b.author`), `@EntityGraph(attributePaths = "author")`, or a DTO projection. The test
`nPlusOneProblemAndTheFetchJoinFix` counts the statements with Hibernate statistics: 11 vs 1.

### Projections
**Level:** Intermediate · **Code:** [BookSummary.java](../src/main/java/com/zerotohero/book/BookSummary.java)

Return an interface or record projection (`List<BookSummary>`) to select only the columns you need.
Projections are read-only and skip the persistence context, so they're ideal for list screens and
reports.

### Paging and sorting
**Level:** Beginner · **Code:** [BookRepository.java](../src/main/java/com/zerotohero/book/BookRepository.java)

Pass a `Pageable` and get a `Page<T>` with content and totals. A `Page` runs an extra `count`
query; return `Slice<T>` when you only need "is there a next page". For deep pages on big tables
use keyset pagination (`where id > :lastId order by id limit 20`).

### Auditing
**Level:** Beginner · **Code:** [JpaConfig.java](../src/main/java/com/zerotohero/book/JpaConfig.java)

`@EnableJpaAuditing` plus `@CreatedDate`/`@LastModifiedDate` (and `@CreatedBy` with an
`AuditorAware` bean) fill audit columns automatically.

### Open Session in View
**Level:** Advanced · **Code:** [application.yml](../src/main/resources/application.yml)

Boot enables `spring.jpa.open-in-view` by default, keeping a database session open while the view
or JSON is rendered so lazy loading "just works". It hides N+1 queries and holds connections longer.
This repo sets it to `false`: load what you need in the service, inside the transaction.

## Practice

### Add tags (many-to-many)
**Level:** Intermediate

Add a `Tag` entity and a `book_tags` join table via a new Flyway migration. Add
`findByTagsName(String)` and a test. Then make sure listing books with tags doesn't cause N+1.

### Write a revenue report query
**Level:** Intermediate

Add a repository method that returns `record AuthorRevenue(String author, BigDecimal revenue)`
using a JPQL constructor expression or interface projection, grouped by author. Test it with
`@DataJpaTest`.

### Keyset pagination
**Level:** Advanced

Add `GET /api/books/scroll?afterId=0&size=20` using keyset pagination. Compare its SQL with
offset paging for page 1,000.

## Interview questions

- What is the persistence context, and what is dirty checking?
- Why should `@ManyToOne` be lazy? What is a `LazyInitializationException`?
- Explain the N+1 problem and three ways to fix it.
- `save()` vs `saveAndFlush()`: what's the difference?
- Why disable Open Session in View?

## Further reading

- [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/)
- [Hibernate ORM user guide](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html)
- [Vlad Mihalcea: High-Performance Java Persistence (blog)](https://vladmihalcea.com/tutorials/hibernate/)
