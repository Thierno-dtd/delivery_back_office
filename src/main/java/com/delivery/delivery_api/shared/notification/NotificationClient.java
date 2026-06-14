package com.delivery.delivery_api.shared.notification;

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
public class NotificationClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.services.notification-url}")
    private String notificationUrl;

    /**
     * Envoie une notification push FCM (mobile)
     */
    @Async
    public void sendPush(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(notificationUrl + "/api/v1/notifications/push")
                    .bodyValue(Map.of(
                            "fcmToken", fcmToken,
                            "title", title,
                            "body", body,
                            "data", data != null ? data : Map.of()
                    ))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe(
                            null,
                            error -> log.warn("Notification service indisponible : {}", error.getMessage())
                    );
        } catch (Exception e) {
            log.warn("Impossible d'envoyer la notification push : {}", e.getMessage());
        }
    }

    /**
     * Envoie un email
     */
    @Async
    public void sendEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(notificationUrl + "/api/v1/notifications/email")
                    .bodyValue(Map.of(
                            "to", to,
                            "subject", subject,
                            "templateName", templateName,
                            "variables", variables != null ? variables : Map.of()
                    ))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe(
                            null,
                            error -> log.warn("Email service indisponible : {}", error.getMessage())
                    );
        } catch (Exception e) {
            log.warn("Impossible d'envoyer l'email à {} : {}", to, e.getMessage());
        }
    }

    /**
     * Envoie un SMS via Twilio
     */
    @Async
    public void sendSms(String phoneNumber, String message) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(notificationUrl + "/api/v1/notifications/sms")
                    .bodyValue(Map.of(
                            "phoneNumber", phoneNumber,
                            "message", message
                    ))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe(
                            null,
                            error -> log.warn("SMS service indisponible : {}", error.getMessage())
                    );
        } catch (Exception e) {
            log.warn("Impossible d'envoyer le SMS à {} : {}", phoneNumber, e.getMessage());
        }
    }
}