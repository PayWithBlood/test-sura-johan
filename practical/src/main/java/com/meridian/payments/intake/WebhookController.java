package com.meridian.payments.intake;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {
    private final WebhookIntakeService intake;
    private final String quetzalSecret;
    private final String andinaSecret;

    public WebhookController(WebhookIntakeService intake,
            @Value("${webhooks.quetzal.secret}") String quetzalSecret,
            @Value("${webhooks.andina.secret}") String andinaSecret) {
        this.intake = intake;
        this.quetzalSecret = quetzalSecret;
        this.andinaSecret = andinaSecret;
    }

    @PostMapping(path = "/quetzal", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> quetzal(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "Quetzal-Signature", required = false) String signature,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlation) {
        UUID id = correlationId(correlation);
        if (id == null) return ResponseEntity.badRequest().build();
        var result = intake.quetzal(rawBody, signature, quetzalSecret, id);
        return ResponseEntity.status(result.httpStatus()).build();
    }

    @PostMapping(path = "/andina", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> andina(
            @RequestBody byte[] rawBody,
            @RequestHeader(value = "X-Andina-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Andina-Signature", required = false) String signature,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlation) {
        UUID id = correlationId(correlation);
        if (id == null) return ResponseEntity.badRequest().build();
        var result = intake.andina(rawBody, timestamp, signature, andinaSecret, id);
        return ResponseEntity.status(result.httpStatus()).build();
    }

    private static UUID correlationId(String value) {
        if (value == null || value.isBlank()) return UUID.randomUUID();
        try { return UUID.fromString(value); }
        catch (IllegalArgumentException e) { return null; }
    }
}
