# 11 · Database migrations with Flyway

> **Phase:** Building APIs · **Week:** 6 · **Code:** [`db/migration/`](../src/main/resources/db/migration) · **Tests:** [`FlywayMigrationTest.java`](../src/test/java/com/zerotohero/migration/FlywayMigrationTest.java)

Your schema changes as the app grows, and every environment (your laptop, CI, staging,
production) must end up with the same schema. Migrations are versioned SQL scripts applied in
order and recorded in the database, so the schema is code: reviewed, versioned and repeatable.

## Concepts

### Why not ddl-auto?
**Level:** Beginner · **Code:** [application.yml](../src/main/resources/application.yml)

`spring.jpa.hibernate.ddl-auto=update` guesses schema changes from entities: it never drops or
renames columns, can't migrate data, and does different things on different databases. Use
`validate` (as this repo does) so Hibernate only checks that entities match the schema Flyway built.

### Versioned migrations
**Level:** Beginner · **Code:** [V1__create_catalog.sql](../src/main/resources/db/migration/V1__create_catalog.sql)

Files named `V<version>__<description>.sql` in `db/migration` run once, in version order. Flyway
records each in `flyway_schema_history` with a checksum. Never edit a migration that has run
anywhere shared; add a new one.

### Seed and reference data
**Level:** Beginner · **Code:** [V2__seed_catalog.sql](../src/main/resources/db/migration/V2__seed_catalog.sql)

Reference data every environment needs (countries, statuses, the demo catalog) can be a migration.
Test-only data belongs in test setup instead.

### Repeatable migrations and callbacks
**Level:** Intermediate

`R__views.sql` re-runs whenever its checksum changes, which suits views and functions. Callbacks
(`afterMigrate.sql`) run at lifecycle points. Use them sparingly.

### Zero-downtime schema changes
**Level:** Advanced

During a rolling deploy old and new app versions run against the same database, so every change
must work with both. Use *expand and contract*: add the new column (nullable) → deploy code that
writes both → backfill → switch reads → deploy code that stops using the old column → drop it in a
later release. Renames are two migrations, never one.

### Testing migrations
**Level:** Intermediate · **Code:** [FlywayMigrationTest.java](../src/test/java/com/zerotohero/migration/FlywayMigrationTest.java)

Every test that starts JPA runs the migrations and `ddl-auto=validate`, so a mismatch fails the
build. For full confidence run them against the real database engine with Testcontainers (module
12), because H2 accepts SQL that PostgreSQL rejects and vice versa.

## Practice

### Add a column the safe way
**Level:** Intermediate

Add a nullable `published_year` column to `books` in `V5__...sql`, backfill the seeded rows, then
make it `NOT NULL` in `V6__...sql`. Update the entity and the tests.

### Rename a column without downtime
**Level:** Advanced

Plan (as a list of releases and migrations) how to rename `books.title` to `books.name` while the
app keeps serving traffic during each deploy.

## Interview questions

- Why is `ddl-auto=update` dangerous in production?
- What happens if someone edits a migration that has already run?
- How do you make a breaking schema change without downtime?

## Further reading

- [Flyway documentation](https://documentation.red-gate.com/flyway)
- [Spring Boot: Database initialization](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
- [Martin Fowler: Evolutionary Database Design](https://martinfowler.com/articles/evodb.html)
