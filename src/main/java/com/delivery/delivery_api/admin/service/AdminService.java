package com.delivery.delivery_api.admin.service;

import com.delivery.delivery_api.admin.dto.request.CreateAdminRequest;
import com.delivery.delivery_api.admin.dto.request.UpdateAdminRequest;
import com.delivery.delivery_api.admin.dto.response.AdminResponse;
import com.delivery.delivery_api.admin.entity.Admin;
import com.delivery.delivery_api.admin.enums.AdminRole;
import com.delivery.delivery_api.admin.repository.AdminRepository;
import com.delivery.delivery_api.agency.service.AgencyService;
import com.delivery.delivery_api.shared.audit.AuditClient;
import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ConflictException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.shared.notification.NotificationClient;
import com.delivery.delivery_api.shared.response.PageResponse;
import com.delivery.delivery_api.shared.utils.KeyGeneratorUtil;
import com.delivery.delivery_api.shared.utils.PaginationUtil;
import com.delivery.delivery_api.user.entity.User;
import com.delivery.delivery_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService implements IAdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditClient auditClient;
    private final NotificationClient notificationClient;
    private final AgencyService agencyService;

    private static final String PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";

    @Override
    @Transactional
    public AdminResponse create(CreateAdminRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Utilisateur", "email", request.getEmail());
        }

        if (request.getRole() == AdminRole.MANAGER && request.getAgencyId() == null) {
            throw new BusinessException("Un manager doit être rattaché à une agence");
        }

        if (request.getRole() == AdminRole.SUPER_ADMIN && request.getAgencyId() != null) {
            throw new BusinessException("Un super admin ne doit pas être rattaché à une agence");
        }

        if (request.getRole() == AdminRole.MANAGER) {
            agencyService.validateAgencyActive(request.getAgencyId());

            adminRepository.findManagerByAgencyId(request.getAgencyId())
                    .ifPresent(existing -> {
                        throw new ConflictException("Cette agence a déjà un manager assigné");
                    });
        }

        if (request.getRole() == AdminRole.GESTIONNAIRE) {
            agencyService.validateAgencyActive(request.getAgencyId());
        }

        String temporaryPassword = generateTemporaryPassword();

        User user = User.builder()
                .uuid(KeyGeneratorUtil.generateRandomToken(16))
                .email(request.getEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .active(true)
                .emailVerified(true)
                .mustChangePassword(true)
                .role(request.getRole().name())
                .build();
        user = userRepository.save(user);

        Admin admin = Admin.builder()
                .uuid(KeyGeneratorUtil.generateRandomToken(16))
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .agencyId(request.getAgencyId())
                .build();
        admin = adminRepository.save(admin);

        notificationClient.sendEmail(
                request.getEmail(),
                "Bienvenue sur Delivery Gabon — Vos identifiants de connexion",
                "admin-welcome",
                Map.of(
                        "firstName", request.getFirstName(),
                        "lastName", request.getLastName(),
                        "email", request.getEmail(),
                        "temporaryPassword", temporaryPassword,
                        "role", request.getRole().name()
                )
        );

        auditClient.log(
                "ADMIN_CREATED",
                request.getEmail(),
                String.format("Nouveau %s créé : %s %s",
                        request.getRole(), request.getFirstName(), request.getLastName()),
                null
        );

        log.info("Admin créé : {} ({}) — email envoyé avec mot de passe temporaire",
                request.getEmail(), request.getRole());

        return toResponse(admin, user);
    }

    private String generateTemporaryPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        password.append("ABCDEFGHJKLMNPQRSTUVWXYZ".charAt(random.nextInt(24)));
        password.append("abcdefghijkmnpqrstuvwxyz".charAt(random.nextInt(24)));
        password.append("23456789".charAt(random.nextInt(8)));

        for (int i = 0; i < 9; i++) {
            password.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }

        List<Character> chars = password.chars()
                .mapToObj(c -> (char) c)
                .collect(java.util.stream.Collectors.toList());
        java.util.Collections.shuffle(chars, random);

        StringBuilder shuffled = new StringBuilder();
        chars.forEach(shuffled::append);

        return shuffled.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponse findByUuid(String uuid) {
        Admin admin = adminRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "uuid", uuid));
        return toResponse(admin, admin.getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminResponse findByUserEmail(String email) {
        Admin admin = adminRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "email", email));
        return toResponse(admin, admin.getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminResponse> findByRole(AdminRole role, int page, int size) {
        Page<Admin> admins = adminRepository.findByRole(role, PaginationUtil.build(page, size));
        List<AdminResponse> content = admins.getContent().stream()
                .map(a -> toResponse(a, a.getUser()))
                .toList();
        return PageResponse.from(admins, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminResponse> findByAgencyId(Long agencyId) {
        return adminRepository.findByAgencyId(agencyId).stream()
                .map(a -> toResponse(a, a.getUser()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Admin getAuthenticatedAdmin(String email) {
        return adminRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "email", email));
    }

    @Override
    @Transactional
    public AdminResponse update(String uuid, UpdateAdminRequest request) {
        Admin admin = adminRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "uuid", uuid));

        if (request.getFirstName() != null) admin.setFirstName(request.getFirstName());
        if (request.getLastName() != null) admin.setLastName(request.getLastName());

        if (request.getAgencyId() != null) {
            if (admin.isSuperAdmin()) {
                throw new BusinessException("Un super admin ne peut pas être rattaché à une agence");
            }
            admin.setAgencyId(request.getAgencyId());
        }

        admin = adminRepository.save(admin);
        log.info("Admin mis à jour : {}", uuid);

        return toResponse(admin, admin.getUser());
    }

    @Override
    @Transactional
    public void resetPassword(String uuid) {
        Admin admin = adminRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "uuid", uuid));

        String temporaryPassword = generateTemporaryPassword();
        String email = admin.getUser().getEmail();

        userRepository.resetPasswordAndClearFlag(email, passwordEncoder.encode(temporaryPassword));
        userRepository.updateActiveStatus(admin.getUuid(), true);

        User user = admin.getUser();
        user.setMustChangePassword(true);
        userRepository.save(user);

        notificationClient.sendEmail(
                email,
                "Delivery Gabon — Réinitialisation de votre mot de passe",
                "admin-password-reset",
                Map.of(
                        "firstName", admin.getFirstName(),
                        "temporaryPassword", temporaryPassword
                )
        );

        auditClient.log("ADMIN_PASSWORD_RESET", email,
                "Mot de passe réinitialisé par un super admin", null);

        log.info("Mot de passe réinitialisé pour : {}", email);
    }

    @Override
    @Transactional
    public void deactivate(String uuid) {
        Admin admin = adminRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "uuid", uuid));

        admin.getUser().setActive(false);
        userRepository.save(admin.getUser());

        auditClient.log(
                "ADMIN_DEACTIVATED",
                admin.getUser().getEmail(),
                "Admin désactivé : " + admin.getFirstName() + " " + admin.getLastName(),
                null
        );

        log.info("Admin désactivé : {}", uuid);
    }

    private AdminResponse toResponse(Admin admin, User user) {
        return AdminResponse.builder()
                .uuid(admin.getUuid())
                .firstName(admin.getFirstName())
                .lastName(admin.getLastName())
                .email(user.getEmail())
                .role(admin.getRole())
                .agencyId(admin.getAgencyId())
                .active(user.isActive())
                .createdAt(admin.getCreatedAt())
                .build();
    }
}