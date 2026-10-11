package com.zerotohero.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Module 16: an aspect adds behaviour around methods without touching their code. Here: time every
 * {@code @LogExecutionTime} method, log slow calls and record a Micrometer timer.
 */
@Aspect
@Component
public class ExecutionTimeAspect {

    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeAspect.class);
    private static final long SLOW_MS = 500;
    private final MeterRegistry registry;

    public ExecutionTimeAspect(MeterRegistry registry) {
        this.registry = registry;
    }

    // Pointcut: any method annotated with @LogExecutionTime. Advice type: @Around.
    @Around("@annotation(com.zerotohero.aop.LogExecutionTime)")
    public Object time(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        Timer.Sample sample = Timer.start(registry);
        String outcome = "success";
        try {
            return joinPoint.proceed();
        } catch (Throwable t) {
            outcome = t.getClass().getSimpleName();
            throw t;
        } finally {
            long nanos = sample.stop(Timer.builder("bookstore.method.time")
                    .tag("method", method).tag("outcome", outcome).register(registry));
            long ms = nanos / 1_000_000;
            if (ms > SLOW_MS) log.warn("{} took {} ms", method, ms);
            else log.debug("{} took {} ms", method, ms);
        }
    }
}
