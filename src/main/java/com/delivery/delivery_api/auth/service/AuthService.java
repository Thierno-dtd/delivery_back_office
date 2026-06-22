package com.delivery.delivery_api.auth.service;

import com.delivery.delivery_api.auth.dto.request.LoginRequest;
import com.delivery.delivery_api.auth.dto.request.RefreshTokenRequest;
import com.delivery.delivery_api.auth.dto.request.RegisterRequest;
import com.delivery.delivery_api.auth.dto.response.AuthResponse;
import com.delivery.delivery_api.shared.audit.AuditClient;
import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ConflictException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.shared.exception.UnauthorizedException;
import com.delivery.delivery_api.shared.utils.KeyGeneratorUtil;
import com.delivery.delivery_api.user.entity.User;
import com.delivery.delivery_api.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuditClient auditClient;

    // Préfixes des clés Redis
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // ===== LOGIN =====

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            auditClient.logSecurity(
                    "LOGIN_FAILED",
                    request.getEmail(),
                    "MEDIUM",
                    getClientIp(httpRequest),
                    "Tentative de connexion avec mauvais mot de passe"
            );
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", request.getEmail()));

        if (!user.isActive()) {
            throw new BusinessException("Votre compte est désactivé. Contactez le support.",
                    "ACCOUNT_DISABLED", HttpStatus.FORBIDDEN);
        }

        // Générer les tokens avec claims métier
        Map<String, Object> claims = buildClaims(user);
        String accessToken = jwtService.generateAccessToken(claims, user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Stocker le refresh token dans Redis (7 jours)
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getEmail(),
                refreshToken,
                7, TimeUnit.DAYS
        );

        // Audit
        auditClient.log(
                "LOGIN_SUCCESS",
                user.getEmail(),
                "Connexion réussie",
                getClientIp(httpRequest)
        );

        log.info("Connexion réussie pour : {}", user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ===== REGISTER (CUSTOMER uniquement) =====

    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Utilisateur", "email", request.getEmail());
        }

        // Créer l'utilisateur de base
        User user = User.builder()
                .uuid(KeyGeneratorUtil.generateRandomToken(16))
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        // Générer les tokens
        Map<String, Object> claims = buildClaims(user);
        String accessToken = jwtService.generateAccessToken(claims, user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Stocker le refresh token dans Redis
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getEmail(),
                refreshToken,
                7, TimeUnit.DAYS
        );

        // Audit
        auditClient.log(
                "REGISTER_SUCCESS",
                user.getEmail(),
                "Nouveau compte créé",
                getClientIp(httpRequest)
        );

        log.info("Nouveau compte créé : {}", user.getEmail());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ===== REFRESH TOKEN =====

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Extraire l'email du refresh token
        String email;
        try {
            email = jwtService.extractEmail(refreshToken);
        } catch (Exception e) {
            throw new UnauthorizedException("Refresh token invalide");
        }

        // Vérifier que le token est dans Redis (pas révoqué)
        String storedToken = (String) redisTemplate.opsForValue()
                .get(REFRESH_TOKEN_PREFIX + email);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new UnauthorizedException("Refresh token expiré ou révoqué");
        }

        // Charger l'utilisateur et générer un nouvel access token
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        if (!user.isActive()) {
            throw new BusinessException("Compte désactivé", "ACCOUNT_DISABLED", HttpStatus.FORBIDDEN);
        }

        Map<String, Object> claims = buildClaims(user);
        String newAccessToken = jwtService.generateAccessToken(claims, user);

        return buildAuthResponse(user, newAccessToken, refreshToken);
    }

    // ===== LOGOUT =====

    @Transactional
    public void logout(String authHeader, HttpServletRequest httpRequest) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractEmail(token);

            // Blacklister l'access token jusqu'à son expiration
            long expiration = jwtService.extractExpiration(token).getTime() - System.currentTimeMillis();
            if (expiration > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + token,
                        "blacklisted",
                        expiration,
                        TimeUnit.MILLISECONDS
                );
            }

            // Supprimer le refresh token de Redis
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + email);

            auditClient.log("LOGOUT", email, "Déconnexion", getClientIp(httpRequest));
            log.info("Déconnexion : {}", email);

        } catch (Exception e) {
            log.warn("Erreur lors du logout : {}", e.getMessage());
        }
    }

    // ===== PRIVÉ =====

    private Map<String, Object> buildClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uuid", user.getUuid());
        // Le role sera ajouté dans les modules admin/customer/driver
        // via leurs propres méthodes de login si nécessaire
        return claims;
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(System.currentTimeMillis() + 86400000L)
                .uuid(user.getUuid())
                .email(user.getEmail())
                .firstName("")
                .lastName("")
                .role(user.getAuthorities().stream()
                        .findFirst()
                        .map(a -> a.getAuthority())
                        .orElse("ROLE_CUSTOMER"))
                .mustChangePassword(user.isMustChangePassword())   // ← ajoute cette ligne
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}