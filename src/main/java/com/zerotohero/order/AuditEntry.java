package com.zerotohero.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_log")
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(nullable = false, length = 500)
    private String detail;

    @Column(nullable = false)
    private Instant createdAt;

    protected AuditEntry() {
    }

    public AuditEntry(String action, String detail, Instant createdAt) {
        this.action = action;
        this.detail = detail.length() > 500 ? detail.substring(0, 500) : detail;
        this.createdAt = createdAt;
    }

    public String getAction() {
        return action;
    }

    public String getDetail() {
        return detail;
    }
}
