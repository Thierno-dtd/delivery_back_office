package com.delivery.delivery_api.shared.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.services.audit-url}")
    private String auditUrl;

    /**
     * Envoie un événement d'audit de façon asynchrone
     * — ne bloque jamais l'API principale
     */
    @Async
    public void log(String eventType, String userEmail, String details, String ipAddress) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(auditUrl + "/api/v1/audit/log")
                    .bodyValue(Map.of(
                            "eventType", eventType,
                            "userEmail", userEmail != null ? userEmail : "system",
                            "details", details,
                            "ipAddress", ipAddress != null ? ipAddress : "unknown",
                            "applicationName", "delivery-api"
                    ))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe(
                            null,
                            error -> log.warn("Audit service indisponible : {}", error.getMessage())
                    );
        } catch (Exception e) {
            // Ne jamais faire planter l'API si l'audit est down
            log.warn("Impossible d'envoyer l'événement audit [{}] : {}", eventType, e.getMessage());
        }
    }

    @Async
    public void logSecurity(String securityEvent, String userEmail,
                            String threatLevel, String ipAddress, String description) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(auditUrl + "/api/v1/audit/security")
                    .bodyValue(Map.of(
                            "securityEvent", securityEvent,
                            "userEmail", userEmail != null ? userEmail : "unknown",
                            "threatLevel", threatLevel,
                            "ipAddress", ipAddress != null ? ipAddress : "unknown",
                            "description", description
                    ))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe(
                            null,
                            error -> log.warn("Audit security service indisponible : {}", error.getMessage())
                    );
        } catch (Exception e) {
            log.warn("Impossible d'envoyer l'événement sécurité [{}] : {}", securityEvent, e.getMessage());
        }
    }
}