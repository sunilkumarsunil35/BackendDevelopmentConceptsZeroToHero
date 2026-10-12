package com.zerotohero.messaging;

import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Module 20: polls the outbox and publishes unsent events in order. If publishing fails the row
 * stays unpublished and is retried on the next run, so delivery is "at least once".
 */
@Component
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private final OutboxRepository repository;
    private final MessagePublisher publisher;
    private final Clock clock;

    public OutboxRelay(OutboxRepository repository, MessagePublisher publisher, Clock clock) {
        this.repository = repository;
        this.publisher = publisher;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval:PT5S}", initialDelayString = "${app.outbox.poll-interval:PT5S}")
    @Transactional
    public int publishPending() {
        int sent = 0;
        for (OutboxEvent event : repository.findByPublishedAtIsNullOrderByIdAsc(Limit.of(100))) {
            String topic = event.getAggregateType().toLowerCase() + "-events";
            publisher.publish(new Message(topic, event.getAggregateId(), event.getEventType(), event.getPayload(),
                    event.getId()));
            event.markPublished(Instant.now(clock));
            sent++;
        }
        if (sent > 0) log.info("Published {} outbox event(s)", sent);
        return sent;
    }
}
