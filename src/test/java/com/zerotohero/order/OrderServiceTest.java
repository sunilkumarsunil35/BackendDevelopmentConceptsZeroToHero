package com.zerotohero.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import com.zerotohero.async.NotificationService;
import com.zerotohero.book.Book;
import com.zerotohero.book.BookRepository;
import com.zerotohero.book.InsufficientStockException;
import com.zerotohero.order.OrderDtos.OrderLine;
import com.zerotohero.order.OrderDtos.OrderResponse;
import com.zerotohero.order.OrderDtos.PlaceOrderRequest;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Modules 10 and 15: transactions against a real database. These tests are not @Transactional
 * themselves, because we want to see real commits and rollbacks.
 */
@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {

    @Autowired
    OrderService orders;

    @Autowired
    BookRepository books;

    @Autowired
    AuditRepository audit;

    @Autowired
    NotificationService notifications;

    @Autowired
    TransactionTemplate tx;

    private int stockOf(long bookId) {
        return books.findById(bookId).orElseThrow().getStock();
    }

    @Test
    void placingAnOrderReducesStockAndTotalsThePrice() {
        int before = stockOf(3L);
        OrderResponse order = orders.placeOrder("asha", new PlaceOrderRequest(List.of(new OrderLine(3L, 2))));

        assertThat(order.total()).isEqualByComparingTo(new BigDecimal("99.98"));
        assertThat(order.items()).singleElement().extracting(OrderDtos.OrderItemResponse::title)
                .isEqualTo("Designing Data-Intensive Applications");
        assertThat(stockOf(3L)).isEqualTo(before - 2);
    }

    @Test
    void failureRollsBackEverythingButTheRequiresNewAuditEntry() {
        int stock7 = stockOf(7L);
        long attemptsBefore = audit.findByAction("ORDER_ATTEMPT").size();
        // The first line is fine, the second asks for more than exists, so the whole order fails.
        var request = new PlaceOrderRequest(List.of(new OrderLine(7L, 1), new OrderLine(9L, 10)));

        assertThatThrownBy(() -> orders.placeOrder("ravi", request)).isInstanceOf(InsufficientStockException.class);

        assertThat(stockOf(7L)).as("stock change for book 7 was rolled back").isEqualTo(stock7);
        assertThat(audit.findByAction("ORDER_ATTEMPT")).as("REQUIRES_NEW committed independently")
                .hasSize((int) attemptsBefore + 1);
    }

    @Test
    void optimisticLockingRejectsAStaleUpdate() {
        Book first = books.findById(8L).orElseThrow();
        Book second = books.findById(8L).orElseThrow(); // two copies of the same row, both version N

        first.removeStock(1);
        books.save(first); // version becomes N+1

        second.removeStock(1);
        assertThatThrownBy(() -> books.save(second)) // still says version N: rejected
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void sameIdempotencyKeyPlacesTheOrderOnce() {
        int before = stockOf(4L);
        var request = new PlaceOrderRequest(List.of(new OrderLine(4L, 1)));

        OrderResponse first = orders.placeOrderIdempotent("meera", "key-123", request);
        OrderResponse retry = orders.placeOrderIdempotent("meera", "key-123", request);

        assertThat(retry.id()).isEqualTo(first.id());
        assertThat(stockOf(4L)).isEqualTo(before - 1);
    }

    @Test
    void confirmationIsSentAsynchronouslyAfterCommit() {
        OrderResponse order = orders.placeOrder("kiran", new PlaceOrderRequest(List.of(new OrderLine(1L, 1))));
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                assertThat(notifications.sent()).contains("Order " + order.id() + " confirmed for kiran"));
    }

    @Test
    void noConfirmationWhenTheOrderRollsBack() {
        OrderResponse order = tx.execute(status -> {
            OrderResponse placed = orders.placeOrder("dev", new PlaceOrderRequest(List.of(new OrderLine(1L, 1))));
            status.setRollbackOnly(); // the surrounding transaction rolls back after the order was "placed"
            return placed;
        });
        await().during(Duration.ofMillis(300)).atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> assertThat(notifications.sent()).noneMatch(m -> m.startsWith("Order " + order.id() + " ")));
        assertThat(orders.idsFor("dev")).doesNotContain(order.id());
    }
}
