package com.zerotohero.order;

import com.zerotohero.aop.LogExecutionTime;
import com.zerotohero.book.Book;
import com.zerotohero.book.BookRepository;
import com.zerotohero.error.NotFoundException;
import com.zerotohero.messaging.OutboxService;
import com.zerotohero.order.OrderDtos.OrderLine;
import com.zerotohero.order.OrderDtos.OrderResponse;
import com.zerotohero.order.OrderDtos.PlaceOrderRequest;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Module 10: one business operation = one transaction. Placing an order reads books, reduces
 * stock, saves the order, writes an outbox event and publishes an application event. If any step
 * throws, everything is rolled back together.
 */
@Service
public class OrderService {

    private final OrderRepository orders;
    private final BookRepository books;
    private final IdempotencyKeyRepository idempotencyKeys;
    private final AuditService audit;
    private final OutboxService outbox;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final Counter ordersPlaced;

    public OrderService(OrderRepository orders, BookRepository books, IdempotencyKeyRepository idempotencyKeys,
                        AuditService audit, OutboxService outbox, ApplicationEventPublisher events, Clock clock,
                        MeterRegistry meterRegistry) {
        this.orders = orders;
        this.books = books;
        this.idempotencyKeys = idempotencyKeys;
        this.audit = audit;
        this.outbox = outbox;
        this.events = events;
        this.clock = clock;
        // Module 18: a business metric, visible at /actuator/metrics/bookstore.orders.placed
        this.ordersPlaced = Counter.builder("bookstore.orders.placed").description("Orders placed").register(meterRegistry);
    }

    @LogExecutionTime
    @Transactional
    @CacheEvict(cacheNames = "books", allEntries = true) // stock changed, so cached books are stale
    public OrderResponse placeOrder(String customer, PlaceOrderRequest request) {
        audit.record("ORDER_ATTEMPT", customer + " ordered " + request.items());

        Map<Long, Book> byId = books.findAllById(request.items().stream().map(OrderLine::bookId).toList())
                .stream().collect(Collectors.toMap(Book::getId, Function.identity()));

        Order order = new Order(customer, Instant.now(clock));
        for (OrderLine line : request.items()) {
            Book book = Optional.ofNullable(byId.get(line.bookId()))
                    .orElseThrow(() -> new NotFoundException("Book", line.bookId()));
            book.removeStock(line.quantity()); // throws InsufficientStockException -> rollback
            order.addItem(new OrderItem(book, line.quantity()));
        }
        orders.save(order);

        // Module 20: the event is stored in the same transaction as the order (transactional outbox).
        outbox.append("Order", order.getId().toString(), "OrderPlaced",
                Map.of("orderId", order.getId(), "customer", customer, "total", order.getTotal()));
        // Module 15: listeners marked AFTER_COMMIT only run if this transaction commits.
        events.publishEvent(new OrderPlacedEvent(order.getId(), customer, order.getTotal()));
        ordersPlaced.increment();
        return OrderResponse.from(order);
    }

    /**
     * Module 19: an idempotent POST. Retrying with the same key returns the first order instead of
     * placing a second one. The key row is inserted in the same transaction as the order.
     */
    @Transactional
    @CacheEvict(cacheNames = "books", allEntries = true)
    public OrderResponse placeOrderIdempotent(String customer, String key, PlaceOrderRequest request) {
        Optional<IdempotencyKey> existing = idempotencyKeys.findById(customer + ":" + key);
        if (existing.isPresent()) {
            return get(existing.get().getOrderId(), customer);
        }
        // A call on `this` bypasses the Spring proxy, so placeOrder's own annotations don't run here.
        // That's fine: we are already inside a transaction, and this method evicts the cache itself.
        OrderResponse created = placeOrder(customer, request);
        idempotencyKeys.save(new IdempotencyKey(customer + ":" + key, created.id(), Instant.now(clock)));
        return created;
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id, String customer) {
        Order order = orders.findWithItemsById(id)
                .filter(o -> o.getCustomer().equals(customer))
                .orElseThrow(() -> new NotFoundException("Order", id));
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public List<Long> idsFor(String customer) {
        return orders.findByCustomerOrderByCreatedAtDesc(customer).stream().map(Order::getId).toList();
    }
}
