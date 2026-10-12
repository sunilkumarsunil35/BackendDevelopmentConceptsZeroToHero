package com.zerotohero.async;

import com.zerotohero.order.OrderPlacedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Module 15: react to an order only after its transaction commits. If the order rolls back, no
 * confirmation is sent. (A plain @EventListener would run immediately, before the commit.)
 */
@Component
public class OrderEventsListener {

    private final NotificationService notifications;

    public OrderEventsListener(NotificationService notifications) {
        this.notifications = notifications;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        notifications.sendOrderConfirmation(event.customer(), event.orderId());
    }
}
