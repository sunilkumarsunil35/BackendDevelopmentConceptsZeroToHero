package com.zerotohero.async;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** Module 15: slow work (sending email) runs on another thread so the HTTP response isn't delayed. */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final List<String> sent = new CopyOnWriteArrayList<>();

    @Async
    public CompletableFuture<String> sendOrderConfirmation(String customer, Long orderId) {
        String message = "Order " + orderId + " confirmed for " + customer;
        log.info("Sending '{}' on thread {}", message, Thread.currentThread());
        sent.add(message);
        return CompletableFuture.completedFuture(message);
    }

    public List<String> sent() {
        return List.copyOf(sent);
    }
}
