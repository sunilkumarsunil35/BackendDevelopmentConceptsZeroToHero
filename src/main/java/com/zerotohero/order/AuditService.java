package com.zerotohero.order;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Module 10: transaction propagation. REQUIRES_NEW suspends the caller's transaction and runs in
 * its own, so the audit entry is committed even when the order that called it rolls back.
 * It has to be a separate bean: calling a @Transactional method on {@code this} skips the proxy.
 */
@Service
public class AuditService {

    private final AuditRepository repository;
    private final Clock clock;

    public AuditService(AuditRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String detail) {
        repository.save(new AuditEntry(action, detail, Instant.now(clock)));
    }
}
