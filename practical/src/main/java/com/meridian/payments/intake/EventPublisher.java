package com.meridian.payments.intake;
public interface EventPublisher { void publish(CanonicalPaymentEvent event); }