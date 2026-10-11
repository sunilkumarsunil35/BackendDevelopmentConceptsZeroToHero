# 08 · Databases and SQL fundamentals

> **Phase:** Building APIs · **Week:** 4 · **Code:** [`db/migration/`](../src/main/resources/db/migration)

JPA hides SQL, but it doesn't remove it. Every performance problem and most data bugs in backend
systems come down to schema design, indexes and the SQL actually sent to the database. Learn the
relational basics before the ORM.

## Concepts

### Tables, keys and relationships
**Level:** Beginner · **Code:** [V1__create_catalog.sql](../src/main/resources/db/migration/V1__create_catalog.sql)

A primary key identifies a row; a foreign key (`books.author_id references authors(id)`) links
rows and is enforced by the database. One-to-many uses a foreign key on the "many" side;
many-to-many needs a join table (e.g. `book_tags(book_id, tag_id)`).

### SQL you must know
**Level:** Beginner

`SELECT ... FROM ... WHERE ... ORDER BY ... LIMIT`, `INSERT`, `UPDATE`, `DELETE`, `JOIN` (inner
vs left), `GROUP BY` with `COUNT`/`SUM`/`AVG` and `HAVING`, subqueries, and `EXISTS`. Practice on
the H2 console or `psql`: e.g. total revenue per author from `order_items` joined to `books`.

### Normalization
**Level:** Intermediate

Store each fact once. First normal form: atomic columns. Second and third: every non-key column
depends on the key, the whole key, and nothing but the key. Denormalize deliberately, for read
performance, and copy values that must not change later (the order line copies `unit_price`
because the book price may change).

### Constraints protect your data
**Level:** Beginner · **Code:** [V1__create_catalog.sql](../src/main/resources/db/migration/V1__create_catalog.sql)

`NOT NULL`, `UNIQUE` (the ISBN), `CHECK (stock >= 0)` and foreign keys are your last line of
defence when application code has a bug or two requests race. `FlywayMigrationTest` shows the
`CHECK` rejecting negative stock even when Java code is bypassed.

### Indexes
**Level:** Intermediate

An index (usually a B-tree) turns a full-table scan into a quick lookup. Index columns you filter,
join or sort on, including foreign keys (`ix_books_author`). A composite index `(a, b)` helps
queries on `a` or `a and b`, not `b` alone. Indexes cost write speed and space, so add them for real
queries. Use `EXPLAIN` / `EXPLAIN ANALYZE` to see the plan.

### ACID and isolation levels
**Level:** Intermediate

Atomicity, Consistency, Isolation, Durability. Isolation levels trade safety for concurrency:
READ COMMITTED (PostgreSQL's default) prevents dirty reads; REPEATABLE READ prevents
non-repeatable reads; SERIALIZABLE prevents all anomalies, including lost updates and write skew,
at the cost of retries. Module 10 applies this.

### SQL vs NoSQL
**Level:** Intermediate

Relational databases give joins, constraints and transactions. Document stores (MongoDB),
key-value stores (Redis), wide-column stores (Cassandra) and search engines (Elasticsearch) trade
some of that for scale or flexibility in specific access patterns. Default to PostgreSQL; add
others for a proven need.

## Practice

### Write five reporting queries
**Level:** Beginner

Against the bookstore schema: books under a price, books per author, total revenue per book, the
best-selling author, and customers who ordered more than once. Run them in the H2 console
(`spring.h2.console.enabled=true`) or PostgreSQL.

### Design a reviews table
**Level:** Intermediate

Design `reviews` (one review per customer per book, 1–5 stars, text). Write the migration with
the right keys, `UNIQUE` and `CHECK` constraints and indexes for "latest reviews for a book".

### Read a query plan
**Level:** Advanced

In PostgreSQL (`docker compose up`), run `EXPLAIN ANALYZE` on a title search before and after adding
an index. Then explain why `LIKE '%java%'` can't use a normal B-tree index and what a trigram index
(`pg_trgm`) changes.

## Interview questions

- What's the difference between an inner join and a left join?
- When does an index not help?
- What is a lost update, and which isolation level prevents it?
- Why copy the price into the order line instead of joining to the book?

## Further reading

- [PostgreSQL tutorial](https://www.postgresql.org/docs/current/tutorial.html)
- [Use The Index, Luke](https://use-the-index-luke.com/)
- *Designing Data-Intensive Applications* by Martin Kleppmann, chapters 2, 3 and 7
