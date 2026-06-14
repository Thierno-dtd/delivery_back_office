package com.delivery.delivery_api.shared.utils;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class KeyGeneratorUtil {

    private KeyGeneratorUtil() {
        // Classe utilitaire — pas d'instanciation
    }

    /**
     * Génère une clé aléatoire encodée en Base64
     * @param bits : 256 (32 bytes) ou 512 (64 bytes)
     */
    public static String generateBase64Key(int bits) {
        if (bits != 256 && bits != 512) {
            throw new IllegalArgumentException("Taille supportée : 256 ou 512 bits");
        }
        int bytes = bits / 8;
        byte[] keyBytes = new byte[bytes];
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes);
    }

    /**
     * Génère une clé JWT sécurisée pour HMAC-SHA256 (256 bits minimum)
     */
    public static String generateJwtSecret256() {
        return generateBase64Key(256);
    }

    /**
     * Génère une clé JWT sécurisée pour HMAC-SHA512 (512 bits)
     */
    public static String generateJwtSecret512() {
        return generateBase64Key(512);
    }

    /**
     * Génère une SecretKey directement utilisable par jjwt
     */
    public static SecretKey generateSecretKey() {
        return Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
    }

    /**
     * Génère un token aléatoire (pour refresh token, reset password, etc.)
     * @param length : longueur en bytes (ex: 32 → token de 64 chars hex)
     */
    public static String generateRandomToken(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Génère un code OTP numérique
     * @param digits : nombre de chiffres (4 ou 6)
     */
    public static String generateOtp(int digits) {
        if (digits < 4 || digits > 8) {
            throw new IllegalArgumentException("OTP : entre 4 et 8 chiffres");
        }
        int max = (int) Math.pow(10, digits);
        int otp = new SecureRandom().nextInt(max);
        return String.format("%0" + digits + "d", otp);
    }

    // ===== MAIN pour générer et afficher les clés =====
    // Lance cette méthode une fois pour obtenir ta clé JWT de prod
    public static void main(String[] args) {
        System.out.println("=== Générateur de clés Delivery API ===");
        System.out.println();
        System.out.println("Clé JWT 256 bits (HS256) :");
        System.out.println(generateJwtSecret256());
        System.out.println();
        System.out.println("Clé JWT 512 bits (HS512) :");
        System.out.println(generateJwtSecret512());
        System.out.println();
        System.out.println("Refresh token (32 bytes) :");
        System.out.println(generateRandomToken(32));
        System.out.println();
        System.out.println("OTP 6 chiffres :");
        System.out.println(generateOtp(6));
    }
}