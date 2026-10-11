package com.zerotohero.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/** Module 19: remembers which order a client's Idempotency-Key produced. */
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

    @Id
    @Column(name = "idem_key", length = 100)
    private String key;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Instant createdAt;

    protected IdempotencyKey() {
    }

    public IdempotencyKey(String key, Long orderId, Instant createdAt) {
        this.key = key;
        this.orderId = orderId;
        this.createdAt = createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }
}
