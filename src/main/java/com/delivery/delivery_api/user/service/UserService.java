package com.delivery.delivery_api.user.service;

import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.shared.exception.UnauthorizedException;
import com.delivery.delivery_api.user.dto.request.UpdateProfileRequest;
import com.delivery.delivery_api.user.dto.response.UserResponse;
import com.delivery.delivery_api.user.entity.User;
import com.delivery.delivery_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        User user = getAuthenticatedUser();
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = getAuthenticatedUser();

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {

            if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
                throw new BusinessException(
                        "Le mot de passe actuel est requis pour changer le mot de passe"
                );
            }

            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BusinessException(
                        "Mot de passe actuel incorrect",
                        "WRONG_PASSWORD",
                        HttpStatus.BAD_REQUEST
                );
            }

            userRepository.updatePassword(
                    user.getEmail(),
                    passwordEncoder.encode(request.getNewPassword())
            );

            log.info("Mot de passe mis à jour pour : {}", user.getEmail());
        }

        User updated = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", user.getEmail()));

        return toResponse(updated);
    }

    @Override
    @Transactional
    public void toggleActiveStatus(String uuid, boolean active) {
        if (!userRepository.existsByUuid(uuid)) {
            throw new ResourceNotFoundException("Utilisateur", "uuid", uuid);
        }
        userRepository.updateActiveStatus(uuid, active);
        log.info("Statut utilisateur {} → active={}", uuid, active);
    }

    @Override
    public User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new UnauthorizedException("Utilisateur non authentifié");
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUuidOrThrow(String uuid) {
        return userRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "uuid", uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .uuid(user.getUuid())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .active(user.isActive())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .build();
    }
}