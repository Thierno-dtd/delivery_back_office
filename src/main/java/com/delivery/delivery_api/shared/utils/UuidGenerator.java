package com.delivery.delivery_api.shared.utils;

import java.util.UUID;

public class UuidGenerator {

    private UuidGenerator() {}

    /**
     * Génère un UUID v4 standard
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }

    /**
     * Génère un UUID court sans tirets (pour les codes de commande etc.)
     */
    public static String generateShort() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    /**
     * Génère un code de commande lisible
     * Exemple : DEL-A3F9B2
     */
    public static String generateOrderCode() {
        String raw = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "DEL-" + raw;
    }

    /**
     * Génère un code d'agence
     * Exemple : AGC-001 (basé sur un compteur externe)
     */
    public static String generateAgencyCode(long counter) {
        return String.format("AGC-%03d", counter);
    }
}