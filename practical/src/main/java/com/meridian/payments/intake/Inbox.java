package com.meridian.payments.intake;
import java.time.Instant;
/** Atomic durable inbox/outbox port; production adapter uses Cosmos conditional create + Change Feed. */
public interface Inbox { boolean accept(String eventKey,byte[] rawBody,String provider,String correlationId,Instant receivedAt); }