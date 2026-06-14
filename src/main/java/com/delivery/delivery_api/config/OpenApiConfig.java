package com.delivery.delivery_api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .addSecurityItem(securityRequirement())
                .components(components());
    }

    private Info apiInfo() {
        return new Info()
                .title("Delivery API - Gabon")
                .version("1.0.0")
                .description("""
                        API REST de l'application de livraison au Gabon.
                        
                        **Rôles disponibles :**
                        - `SUPER_ADMIN` : gestion globale (agences, managers, stats)
                        - `MANAGER` : gestion de son agence (livreurs, commandes)
                        - `DRIVER` : mise à jour position et statut de livraison
                        - `CUSTOMER` : création commandes, suivi, paiement, notation
                        
                        **Authentification :** Bearer JWT — obtenez votre token via `POST /auth/login`
                        """)
                .contact(new Contact()
                        .name("Delivery Gabon")
                        .email("dev@delivery.ga"))
                .license(new License()
                        .name("Propriétaire")
                        .url("https://delivery.ga"));
    }

    private List<Server> servers() {
        if ("prod".equals(activeProfile)) {
            return List.of(
                    new Server().url("https://api.delivery.ga/api").description("Production")
            );
        }
        return List.of(
                new Server().url("http://localhost:8080/api").description("Développement"),
                new Server().url("https://staging.delivery.ga/api").description("Staging")
        );
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("Bearer Authentication");
    }

    private Components components() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .name("Authorization")
                                .description("Entrez votre token JWT. Exemple : eyJhbGci...")
                );
    }
}