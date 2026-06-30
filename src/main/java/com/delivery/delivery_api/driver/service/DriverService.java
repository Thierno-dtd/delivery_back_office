package com.delivery.delivery_api.driver.service;

import com.delivery.delivery_api.agency.entity.Agency;
import com.delivery.delivery_api.agency.service.AgencyService;
import com.delivery.delivery_api.driver.dto.request.CreateDriverRequest;
import com.delivery.delivery_api.driver.dto.request.UpdateDriverRequest;
import com.delivery.delivery_api.driver.dto.response.DriverResponse;
import com.delivery.delivery_api.driver.entity.Driver;
import com.delivery.delivery_api.driver.enums.DriverStatus;
import com.delivery.delivery_api.driver.repository.DriverRepository;
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
import com.delivery.delivery_api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService implements IDriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AgencyService agencyService;
    private final PasswordEncoder passwordEncoder;
    private final AuditClient auditClient;
    private final NotificationClient notificationClient;

    private static final String PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";

    // ===== CRÉATION =====

    @Override
    @Transactional
    public DriverResponse create(CreateDriverRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Utilisateur", "email", request.getEmail());
        }

        if (driverRepository.existsByTelephone(request.getTelephone())) {
            throw new ConflictException("Livreur", "téléphone", request.getTelephone());
        }

        // Valider que l'agence existe et est active
        agencyService.validateAgencyActive(request.getAgencyId());
        Agency agency = agencyService.getByIdOrThrow(request.getAgencyId());

        // Générer un mot de passe temporaire sécurisé
        String temporaryPassword = generateTemporaryPassword();

        User user = User.builder()
                .uuid(KeyGeneratorUtil.generateRandomToken(16))
                .email(request.getEmail())
                .password(passwordEncoder.encode(temporaryPassword))
                .active(true)
                .emailVerified(true)
                .mustChangePassword(true)
                .role("DRIVER")
                .build();
        user = userRepository.save(user);

        Driver driver = Driver.builder()
                .uuid(KeyGeneratorUtil.generateRandomToken(16))
                .user(user)
                .agency(agency)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .telephone(request.getTelephone())
                .birthday(request.getBirthday())
                .address(request.getAddress())
                .status(DriverStatus.OFFLINE)
                .phoneVerified(false)
                .build();
        driver = driverRepository.save(driver);

        // Envoyer les identifiants par email
        notificationClient.sendEmail(
                request.getEmail(),
                "Bienvenue chez " + agency.getName() + " — Vos identifiants",
                "driver-welcome",
                Map.of(
                        "firstName", request.getFirstName(),
                        "lastName", request.getLastName(),
                        "email", request.getEmail(),
                        "temporaryPassword", temporaryPassword,
                        "agencyName", agency.getName()
                )
        );

        auditClient.log(
                "DRIVER_CREATED",
                getConnectedUserEmail(),
                String.format("Nouveau livreur créé : %s %s — agence : %s",
                        request.getFirstName(), request.getLastName(), agency.getName()),
                null
        );

        log.info("Livreur créé : {} pour agence {}", request.getEmail(), agency.getName());

        return toResponse(driver, user);
    }

    // ===== LECTURE =====

    @Override
    @Transactional(readOnly = true)
    public DriverResponse findByUuid(String uuid) {
        Driver driver = getByUuidOrThrow(uuid);
        return toResponse(driver, driver.getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getCurrentProfile() {
        Driver driver = getAuthenticatedDriver();
        return toResponse(driver, driver.getUser());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DriverResponse> findByAgency(Long agencyId, int page, int size) {
        agencyService.validateAgencyExists(agencyId);
        Page<Driver> drivers = driverRepository.findByAgencyId(
                agencyId, PaginationUtil.build(page, size));
        List<DriverResponse> content = drivers.getContent().stream()
                .map(d -> toResponse(d, d.getUser()))
                .toList();
        return PageResponse.from(drivers, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverResponse> findAvailableByAgency(Long agencyId) {
        agencyService.validateAgencyExists(agencyId);
        return driverRepository.findByAgencyIdAndStatus(agencyId, DriverStatus.AVAILABLE)
                .stream()
                .map(d -> toResponse(d, d.getUser()))
                .toList();
    }

    // ===== MISE À JOUR =====

    @Override
    @Transactional
    public DriverResponse update(String uuid, UpdateDriverRequest request) {
        Driver driver = getByUuidOrThrow(uuid);
        applyUpdate(driver, request);
        driver = driverRepository.save(driver);

        log.info("Livreur mis à jour : {}", uuid);

        return toResponse(driver, driver.getUser());
    }

    @Override
    @Transactional
    public DriverResponse updateOwnProfile(UpdateDriverRequest request) {
        Driver driver = getAuthenticatedDriver();
        applyUpdate(driver, request);
        driver = driverRepository.save(driver);

        log.info("Livreur a mis à jour son propre profil : {}", driver.getUser().getEmail());

        return toResponse(driver, driver.getUser());
    }

    // ===== CHANGEMENT DE STATUT =====

    @Override
    @Transactional
    public void updateStatus(DriverStatus status) {
        Driver driver = getAuthenticatedDriver();
        driverRepository.updateStatus(driver.getUuid(), status);

        log.info("Statut livreur {} → {}", driver.getUser().getEmail(), status);
    }

    // ===== VÉRIFICATION TÉLÉPHONE =====

    @Override
    @Transactional
    public void verifyPhone(String telephone) {
        Driver driver = driverRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Livreur", "téléphone", telephone));

        driverRepository.verifyPhone(telephone);

        auditClient.log(
                "DRIVER_PHONE_VERIFIED",
                driver.getUser().getEmail(),
                "Téléphone vérifié : " + telephone,
                null
        );

        log.info("Téléphone livreur vérifié : {}", telephone);
    }

    // ===== UTILITAIRES =====

    @Override
    @Transactional(readOnly = true)
    public Driver getAuthenticatedDriver() {
        User user = userService.getAuthenticatedUser();
        return driverRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Livreur", "userId", user.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Driver getByUuidOrThrow(String uuid) {
        return driverRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur", "uuid", uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public void validateDriverBelongsToAgency(String driverUuid, Long agencyId) {
        Driver driver = getByUuidOrThrow(driverUuid);
        if (!driver.getAgency().getId().equals(agencyId)) {
            throw new BusinessException(
                    "Ce livreur n'appartient pas à votre agence",
                    "DRIVER_AGENCY_MISMATCH",
                    HttpStatus.FORBIDDEN
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countByAgency(Long agencyId) {
        return driverRepository.countByAgencyId(agencyId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAvailableByAgency(Long agencyId) {
        return driverRepository.countByAgencyIdAndStatus(agencyId, DriverStatus.AVAILABLE);
    }

    // ===== PRIVÉ =====

    private void applyUpdate(Driver driver, UpdateDriverRequest request) {
        if (request.getTelephone() != null
                && !request.getTelephone().equals(driver.getTelephone())) {

            if (driverRepository.existsByTelephone(request.getTelephone())) {
                throw new ConflictException("Livreur", "téléphone", request.getTelephone());
            }
            driver.setTelephone(request.getTelephone());
            driver.setPhoneVerified(false);
        }

        if (request.getFirstName() != null) driver.setFirstName(request.getFirstName());
        if (request.getLastName() != null) driver.setLastName(request.getLastName());
        if (request.getBirthday() != null) driver.setBirthday(request.getBirthday());
        if (request.getAddress() != null) driver.setAddress(request.getAddress());
        if (request.getPicture() != null) driver.setPicture(request.getPicture());
        if (request.getIdentityDoc() != null) driver.setIdentityDoc(request.getIdentityDoc());
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

    private String getConnectedUserEmail() {
        try {
            return SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private DriverResponse toResponse(Driver driver, User user) {
        return DriverResponse.builder()
                .uuid(driver.getUuid())
                .firstName(driver.getFirstName())
                .lastName(driver.getLastName())
                .email(user.getEmail())
                .telephone(driver.getTelephone())
                .phoneVerified(driver.isPhoneVerified())
                .agencyUuid(driver.getAgency().getUuid())
                .agencyName(driver.getAgency().getName())
                .birthday(driver.getBirthday())
                .picture(driver.getPicture())
                .address(driver.getAddress())
                .status(driver.getStatus())
                .profileComplete(driver.isProfileComplete())
                .createdAt(driver.getCreatedAt())
                .build();
    }
}