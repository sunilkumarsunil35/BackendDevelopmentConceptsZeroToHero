package com.zerotohero.core;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Module 04: bean lifecycle callbacks. Spring calls these after injection and before shutdown. */
@Component
public class LifecycleDemo {

    private static final Logger log = LoggerFactory.getLogger(LifecycleDemo.class);
    private boolean ready;

    @PostConstruct
    void init() {
        ready = true;
        log.info("LifecycleDemo initialised: dependencies are injected, safe to warm up");
    }

    @PreDestroy
    void shutdown() {
        log.info("LifecycleDemo shutting down: release resources here");
    }

    public boolean isReady() {
        return ready;
    }
}
