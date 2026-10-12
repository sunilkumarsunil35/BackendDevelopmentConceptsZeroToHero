package com.zerotohero.messaging;

/** Module 20: what travels over the broker. The key keeps all events for one order in order. */
public record Message(String topic, String key, String type, String payload, long eventId) {
}
