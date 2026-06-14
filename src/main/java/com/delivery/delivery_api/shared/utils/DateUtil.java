package com.delivery.delivery_api.shared.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private DateUtil() {}

    public static final ZoneId GABON_ZONE = ZoneId.of("Africa/Libreville");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Heure actuelle en timezone Gabon (UTC+1)
     */
    public static LocalDateTime nowGabon() {
        return ZonedDateTime.now(GABON_ZONE).toLocalDateTime();
    }

    /**
     * Formate une date en dd/MM/yyyy
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * Formate une date en dd/MM/yyyy HH:mm
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * Vérifie si une date d'expiration est dépassée
     */
    public static boolean isExpired(LocalDateTime expirationDate) {
        return LocalDateTime.now().isAfter(expirationDate);
    }

    /**
     * Ajoute des minutes à maintenant (utile pour OTP, tokens temporaires)
     */
    public static LocalDateTime nowPlusMinutes(long minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }

    /**
     * Ajoute des jours à maintenant (utile pour refresh token)
     */
    public static LocalDateTime nowPlusDays(long days) {
        return LocalDateTime.now().plusDays(days);
    }
}
