package com.delivery.delivery_api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    // ===== ENDPOINTS PUBLICS =====
    private static final String[] PUBLIC_URLS = {
            // Auth
            "/v1/auth/**",
            // Swagger
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v1/api-docs/**",
            "/v3/api-docs/**",
            // Actuator
            "/actuator/health",
            "/actuator/info",
            // H2 console (dev uniquement)
            "/h2-console/**",
            // WebSocket
            "/ws/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF (API REST stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Pas de session HTTP — tout passe par JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Règles d'accès
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()

                        // Gestion globale — super admin uniquement
                        .requestMatchers("/v1/admins/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/v1/agencies/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/v1/zones/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/v1/fees/**").hasRole("SUPER_ADMIN")

                        // Gestion opérationnelle agence — manager et gestionnaire
                        .requestMatchers("/v1/drivers/**").hasAnyRole("SUPER_ADMIN", "MANAGER", "GESTIONNAIRE")
                        .requestMatchers("/v1/orders/**").hasAnyRole("SUPER_ADMIN", "MANAGER", "GESTIONNAIRE", "CUSTOMER", "DRIVER")
                        .requestMatchers("/v1/packages/**").hasAnyRole("SUPER_ADMIN", "MANAGER", "GESTIONNAIRE", "CUSTOMER", "DRIVER")
                        .requestMatchers("/v1/tracking/**").hasAnyRole("SUPER_ADMIN", "MANAGER", "GESTIONNAIRE", "CUSTOMER", "DRIVER")
                        .requestMatchers("/v1/payments/**").hasAnyRole("SUPER_ADMIN", "MANAGER", "GESTIONNAIRE", "CUSTOMER")
                        .requestMatchers("/v1/ratings/**").hasAnyRole("CUSTOMER", "DRIVER")

                        // Profil utilisateur — tout le monde authentifié
                        .requestMatchers("/v1/users/**").authenticated()
                        .requestMatchers("/v1/customers/**").hasAnyRole("SUPER_ADMIN", "MANAGER", "GESTIONNAIRE", "CUSTOMER")
                        .requestMatchers("/v1/devices/**").hasAnyRole("CUSTOMER", "DRIVER")

                        .anyRequest().authenticated()
                )

                // H2 console (dev) — désactiver frameOptions
                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin()))

                // Provider d'authentification
                .authenticationProvider(authenticationProvider())

                // Filtre JWT avant le filtre d'auth standard
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "https://*.delivery.ga"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}