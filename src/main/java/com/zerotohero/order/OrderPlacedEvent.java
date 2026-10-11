package com.zerotohero.order;

import java.math.BigDecimal;

/** Module 15: an application event. Listeners react to it without OrderService knowing about them. */
public record OrderPlacedEvent(Long orderId, String customer, BigDecimal total) {
}
