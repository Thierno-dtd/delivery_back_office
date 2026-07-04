package com.delivery.delivery_api.config;

import com.delivery.delivery_api.admin.entity.Admin;
import com.delivery.delivery_api.admin.enums.AdminRole;
import com.delivery.delivery_api.admin.repository.AdminRepository;
import com.delivery.delivery_api.shared.utils.KeyGeneratorUtil;
import com.delivery.delivery_api.user.entity.User;
import com.delivery.delivery_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    private static final String SUPER_ADMIN_EMAIL = "";
    private static final String SUPER_ADMIN_FIRST_NAME = "";
    private static final String SUPER_ADMIN_LAST_NAME = "";

    private static final String SUPER_ADMIN_PASSWORD_HASH = "";

    @Bean
    public CommandLineRunner initSuperAdmin() {
        return args -> {
            if (userRepository.existsByEmail(SUPER_ADMIN_EMAIL)) {
                log.info("Super admin déjà présent : {}", SUPER_ADMIN_EMAIL);
                return;
            }

            User user = User.builder()
                    .uuid(KeyGeneratorUtil.generateRandomToken(16))
                    .email(SUPER_ADMIN_EMAIL)
                    .password(SUPER_ADMIN_PASSWORD_HASH)
                    .active(true)
                    .emailVerified(true)
                    .mustChangePassword(false)
                    .role("SUPER_ADMIN")
                    .build();
            user = userRepository.save(user);

            Admin admin = Admin.builder()
                    .uuid(KeyGeneratorUtil.generateRandomToken(16))
                    .user(user)
                    .firstName(SUPER_ADMIN_FIRST_NAME)
                    .lastName(SUPER_ADMIN_LAST_NAME)
                    .role(AdminRole.SUPER_ADMIN)
                    .agencyId(null)
                    .build();
            adminRepository.save(admin);

            log.info("✅ Super admin créé avec succès : {}", SUPER_ADMIN_EMAIL);
        };
    }
}