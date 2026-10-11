# 20 · Messaging and event-driven architecture

> **Phase:** Advanced · **Week:** 10 · **Code:** [`messaging/`](../src/main/java/com/zerotohero/messaging) · **Tests:** [`OutboxTest.java`](../src/test/java/com/zerotohero/messaging/OutboxTest.java)

Instead of calling each other directly, services can publish events to a message broker and let
interested services react in their own time. That decouples services, absorbs traffic spikes, and
keeps working when a consumer is down, at the cost of eventual consistency and new failure modes.

## Concepts

### Queues vs topics (pub/sub)
**Level:** Beginner

A queue delivers each message to one consumer (work distribution). A topic delivers each message to
every subscribed group (fan-out). Kafka models both with topics and consumer groups: each group
gets every message, and within a group each partition is read by one consumer.

### Commands vs events
**Level:** Intermediate

A command asks one service to do something (`ReserveStock`) and expects it to happen. An event
states a fact that already happened (`OrderPlaced`) and doesn't care who listens. Name events in
the past tense and include enough data for consumers, but not your whole database row.

### Apache Kafka essentials
**Level:** Intermediate

Topics are split into partitions; order is guaranteed only within a partition, so key messages by
aggregate id (the outbox relay uses the order id). Consumers track offsets; messages are kept for a
retention period and can be replayed. With Spring for Apache Kafka: `KafkaTemplate.send(topic, key, value)`
and `@KafkaListener(topics = "order-events", groupId = "shipping")`.

### RabbitMQ essentials
**Level:** Intermediate

Producers publish to exchanges (direct, topic, fanout) that route to queues by binding keys;
messages are removed once acknowledged. With Spring AMQP: `RabbitTemplate.convertAndSend(...)` and
`@RabbitListener(queues = "...")`. Choose RabbitMQ for flexible routing and task queues, Kafka for
high-throughput event streams and replay.

### The dual-write problem and the transactional outbox
**Level:** Advanced · **Code:** [OutboxRelay.java](../src/main/java/com/zerotohero/messaging/OutboxRelay.java)

Saving an order and then publishing to Kafka are two systems: if the app crashes between them, the
event is lost (or published for an order that rolled back). The outbox pattern writes the event to
an `outbox_events` table in the same transaction as the order
([`OutboxService`](../src/main/java/com/zerotohero/messaging/OutboxService.java), `MANDATORY`
propagation), and a relay publishes unsent rows afterwards. Debezium can do the relay by reading the
database log (change data capture).

### Delivery guarantees and idempotent consumers
**Level:** Advanced · **Code:** [InMemoryBroker.java](../src/main/java/com/zerotohero/messaging/InMemoryBroker.java)

Real systems give *at-least-once* delivery: a message can arrive twice (the relay crashed after
publishing but before marking the row). So consumers must be idempotent: store processed event ids
(`InMemoryBroker.idempotent`), or make the operation naturally idempotent (set status = SHIPPED
rather than toggle it). "Exactly once" end to end is at-least-once plus idempotency.

### Failures: retries and dead-letter queues
**Level:** Advanced

A message that always fails (a poison message) must not block the partition forever. Retry a few
times with backoff, then move it to a dead-letter topic or queue for inspection
(`DefaultErrorHandler` with `DeadLetterPublishingRecoverer` in Spring Kafka).

### Event sourcing and CQRS
**Level:** Advanced

Event sourcing stores the sequence of events as the source of truth and derives current state by
replaying them. CQRS separates the write model from read models optimised for queries, often built
from events. Powerful for audit-heavy domains; complex, so don't start with it.

## Practice

### Publish to real Kafka
**Level:** Advanced

Add `spring-kafka`, Kafka in `docker-compose.yml`, and a `KafkaMessagePublisher implements MessagePublisher`
active under a `kafka` profile. Consume `order-events` in a `@KafkaListener` that logs shipments.
`OutboxRelay` shouldn't change.

### Dead-letter handling
**Level:** Advanced

Make the consumer fail for orders over 500, configure 3 retries with backoff and a dead-letter
topic, and verify the message lands there.

### Clean up the outbox
**Level:** Intermediate

Add a scheduled job that deletes outbox rows published more than 7 days ago, in batches, and test it.

## Interview questions

- What is the dual-write problem, and how does the outbox pattern solve it?
- How does Kafka guarantee ordering, and what do you key messages by?
- What does at-least-once delivery mean for consumers?
- When would you choose RabbitMQ over Kafka?

## Further reading

- [Spring for Apache Kafka](https://docs.spring.io/spring-kafka/reference/)
- [Spring AMQP](https://docs.spring.io/spring-amqp/reference/)
- [microservices.io: Transactional outbox](https://microservices.io/patterns/data/transactional-outbox.html)
- *Designing Data-Intensive Applications*, chapter 11
