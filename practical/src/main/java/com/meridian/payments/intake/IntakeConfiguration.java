package com.meridian.payments.intake;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class IntakeConfiguration {
    @Bean Clock intakeClock() { return Clock.systemUTC(); }
    @Bean WebhookIntakeService webhookIntakeService(Inbox inbox, EventPublisher publisher, Clock clock) {
        return new WebhookIntakeService(inbox, publisher, clock);
    }
}
