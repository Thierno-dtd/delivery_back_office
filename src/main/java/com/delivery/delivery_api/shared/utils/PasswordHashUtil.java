package com.delivery.delivery_api.shared.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String rawPassword = "TON_MOT_DE_PASSE_ICI";
        String hashed = encoder.encode(rawPassword);
        System.out.println("Mot de passe hashé :");
        System.out.println(hashed);
    }
}