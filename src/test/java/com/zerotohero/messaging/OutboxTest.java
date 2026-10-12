package com.zerotohero.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.zerotohero.order.OrderDtos.OrderLine;
import com.zerotohero.order.OrderDtos.PlaceOrderRequest;
import com.zerotohero.order.OrderService;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.IllegalTransactionStateException;

/** Module 20: the transactional outbox, end to end. */
@SpringBootTest
@ActiveProfiles("test")
class OutboxTest {

    @Autowired
    OrderService orders;

    @Autowired
    OutboxRelay relay;

    @Autowired
    OutboxRepository outbox;

    @Autowired
    OutboxService outboxService;

    @Autowired
    InMemoryBroker broker;

    @Test
    void orderEventIsStoredThenRelayedExactlyOnce() {
        relay.publishPending(); // flush anything earlier tests left behind
        broker.clear();

        var order = orders.placeOrder("zoya", new PlaceOrderRequest(List.of(new OrderLine(3L, 1))));
        assertThat(outbox.countByPublishedAtIsNull()).isEqualTo(1);

        assertThat(relay.publishPending()).isEqualTo(1);
        assertThat(broker.published()).singleElement().satisfies(m -> {
            assertThat(m.topic()).isEqualTo("order-events");
            assertThat(m.key()).isEqualTo(order.id().toString());
            assertThat(m.type()).isEqualTo("OrderPlaced");
            assertThat(m.payload()).contains("\"customer\":\"zoya\"");
        });

        assertThat(relay.publishPending()).as("already published rows are skipped").isZero();
    }

    @Test
    void appendRefusesToRunOutsideATransaction() {
        assertThatThrownBy(() -> outboxService.append("Order", "1", "Test", "x"))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    @Test
    void idempotentConsumerIgnoresRedeliveries() {
        AtomicInteger handled = new AtomicInteger();
        var consumer = InMemoryBroker.idempotent(m -> handled.incrementAndGet());
        Message m = new Message("order-events", "1", "OrderPlaced", "{}", 42);
        consumer.accept(m);
        consumer.accept(m); // the broker delivered it twice
        assertThat(handled).hasValue(1);
    }
}
