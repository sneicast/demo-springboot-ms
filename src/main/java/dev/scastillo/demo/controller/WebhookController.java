package dev.scastillo.demo.controller;

import dev.scastillo.demo.service.FeatureFlagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final FeatureFlagService featureFlagService;

    @Value("${growthbook.webhook.secret:}")
    private String webhookSecret;

    @PostMapping("/growthbook")
    public ResponseEntity<String> handleGrowthBookWebhook(
            @RequestHeader(value = "X-GrowthBook-Signature", required = false) String signature,
            @RequestBody(required = false) String payload
    ) {
        log.info("value signature: {}", signature);
        log.info("payload: {}", payload);
        if (!webhookSecret.isEmpty() && !validateSignature(signature, payload)) {
            log.warn("Invalid webhook signature");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
        }

        try {
            featureFlagService.refreshFeatures();
            log.info("Features refreshed successfully via webhook");
            return ResponseEntity.ok("Features refreshed");
        } catch (Exception e) {
            log.error("Failed to refresh features via webhook: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to refresh features");
        }

//        featureFlagService.refreshFeatures();
//        return ResponseEntity.ok("Features refreshed");
    }

    private boolean validateSignature(String signature, String payload) {
        if (signature == null || signature.isEmpty() || !webhookSecret.equals(signature)) {
            // If no signature provided but secret is configured, reject
            return webhookSecret.isEmpty();
        }


        return true;
    }
}
