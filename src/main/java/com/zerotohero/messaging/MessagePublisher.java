package com.zerotohero.messaging;

/**
 * Module 20: the port to a message broker. The course uses an in-memory broker so it runs anywhere;
 * swap in a Kafka or RabbitMQ implementation (see lessons/20-messaging-and-events.md) without
 * changing OutboxRelay.
 */
public interface MessagePublisher {
    void publish(Message message);
}
