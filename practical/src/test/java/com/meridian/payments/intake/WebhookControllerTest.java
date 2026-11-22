package com.meridian.payments.intake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebhookControllerTest {
    @Autowired MockMvc mvc;
    @Autowired LocalAdaptersConfiguration.LocalMessageQueue queue;
    @Autowired AdjustableClock clock;

    @TestConfiguration
    static class TestClockConfig {
        @Bean @Primary AdjustableClock testClock() { return new AdjustableClock(); }
    }

    static class AdjustableClock extends Clock {
        private Instant now = Instant.parse("2026-11-28T19:03:11Z");
        void set(Instant value) { now = value; }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @BeforeEach void clearQueue() { queue.clear(); }

    @Test void andinaHttpRequestPublishesCanonicalEvent() throws Exception {
        clock.set(Instant.parse("2026-11-28T19:03:11Z"));
        mvc.perform(post("/webhooks/andina")
                .contentType("application/json")
                .header("X-Andina-Timestamp", "2026-11-28T14:03:11-05:00")
                .header("X-Andina-Signature", "MFQjfviySi6CjqbWtQcy5spKtXxHIxAvmg5W3PTzPJk=")
                .content(sample("reference/providers/andina-co/samples/02-transaccion-aprobada.json")))
            .andExpect(status().isOk());
        var event = queue.poll();
        assertNotNull(event);
        assertEquals("Andina", event.provider());
        assertEquals("Captured", event.status());
        assertEquals("POL-CO-000187342", event.policyNumber());
    }

    @Test void quetzalHttpRequestPublishesCanonicalEvent() throws Exception {
        clock.set(Instant.parse("2026-11-28T18:00:03Z"));
        mvc.perform(post("/webhooks/quetzal")
                .contentType("application/json")
                .header("Quetzal-Signature", "t=1795888803,v1=2d07770582c02f15235ac4e027b7732beef86be103c574a1c3b988a4275f5031")
                .content(sample("reference/providers/quetzal-mx/samples/01-charge-succeeded.json")))
            .andExpect(status().isAccepted());
        var event = queue.poll();
        assertNotNull(event);
        assertEquals("Quetzal", event.provider());
        assertEquals("1250.00", event.amount().toPlainString());
    }

    @Test void missingAndinaSignatureIsRejected() throws Exception {
        mvc.perform(post("/webhooks/andina")
                .contentType("application/json")
                .header("X-Andina-Timestamp", "2026-11-28T14:03:11-05:00")
                .content(sample("reference/providers/andina-co/samples/02-transaccion-aprobada.json")))
            .andExpect(status().isUnauthorized());
        assertEquals(0, queue.size());
    }

    private static byte[] sample(String name) throws Exception {
        String text = Files.readString(Path.of("..", name), StandardCharsets.UTF_8)
                .replace("\r\n", "\n").replace("\r", "\n");
        while (text.endsWith("\n")) text = text.substring(0, text.length() - 1);
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
