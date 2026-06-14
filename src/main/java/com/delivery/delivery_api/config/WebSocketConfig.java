package com.delivery.delivery_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Point d'entrée WebSocket
     * Le client mobile se connecte sur : ws://host/api/ws
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // fallback pour les clients qui ne supportent pas WebSocket natif
    }

    /**
     * Configuration du broker de messages
     *
     * /topic  → broadcast (1 → plusieurs clients)
     *           ex: /topic/order.{orderId} — tous ceux qui suivent une commande
     *
     * /queue  → messages privés (1 → 1 client)
     *           ex: /queue/notifications — notifs personnelles
     *
     * /app    → préfixe pour les messages entrants vers le serveur
     *           ex: /app/location.update — livreur envoie sa position
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
}