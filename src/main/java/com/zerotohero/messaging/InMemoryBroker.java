package com.zerotohero.messaging;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.springframework.stereotype.Component;

/** Module 20: a tiny broker with topics and subscribers, standing in for Kafka in tests and demos. */
@Component
public class InMemoryBroker implements MessagePublisher {

    private final List<Message> published = new CopyOnWriteArrayList<>();
    private final List<Subscription> subscribers = new CopyOnWriteArrayList<>();

    private record Subscription(String topic, Consumer<Message> handler) {
    }

    @Override
    public void publish(Message message) {
        published.add(message);
        subscribers.stream().filter(s -> s.topic().equals(message.topic())).forEach(s -> s.handler().accept(message));
    }

    public void subscribe(String topic, Consumer<Message> handler) {
        subscribers.add(new Subscription(topic, handler));
    }

    public List<Message> published() {
        return List.copyOf(published);
    }

    public void clear() {
        published.clear();
    }

    /**
     * Module 20: an idempotent consumer. Brokers deliver "at least once", so the same message can
     * arrive twice; remember processed event ids and skip repeats.
     */
    public static Consumer<Message> idempotent(Consumer<Message> handler) {
        Set<Long> seen = ConcurrentHashMap.newKeySet();
        return message -> {
            if (seen.add(message.eventId())) handler.accept(message);
        };
    }
}
