package com.meridian.payments.intake;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Configuration
@Profile("local")
public class LocalAdaptersConfiguration {
    @Bean Inbox localInbox() {
        Set<String> keys = ConcurrentHashMap.newKeySet();
        return (key, body, provider, correlation, receivedAt) -> keys.add(key);
    }
    @Bean LocalMessageQueue localMessageQueue() { return new LocalMessageQueue(); }

    /** Non-durable local substitute; production must use an outbox-backed broker adapter. */
    public static final class LocalMessageQueue implements EventPublisher {
        private final ConcurrentLinkedQueue<CanonicalPaymentEvent> events = new ConcurrentLinkedQueue<>();
        @Override public void publish(CanonicalPaymentEvent event) { events.add(event); }
        public CanonicalPaymentEvent poll() { return events.poll(); }
        public void clear() { events.clear(); }
        public int size() { return events.size(); }
    }
}
