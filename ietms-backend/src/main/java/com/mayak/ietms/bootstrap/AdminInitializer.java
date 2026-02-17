package com.mayak.ietms.bootstrap;

import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.infrastructure.config.AdminProperties;
import com.mayak.ietms.features.user.application.UserCommandService;
import com.mayak.ietms.features.user.application.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserCommandService commandService;
    private final UserQueryService queryService;
    private final AdminProperties adminProperties;

    @Override
    public void run(String... args) {
        validateAdminProperties();
        log.info("🔍 Checking if default admin exists...");

        queryService.findByEmail(adminProperties.getEmail())
                .ifPresentOrElse(
                        user -> log.info("Admin already exists: {}", user.getEmail()),
                        this::createAdmin
                );
    }

    private void validateAdminProperties() {
        if (adminProperties.getEmail() == null || adminProperties.getEmail().isBlank()) {
            throw new IllegalStateException(
                    "ADMIN_EMAIL is not configured. Backend cannot start without admin user."
            );
        }

        if (adminProperties.getPassword() == null || adminProperties.getPassword().isBlank()) {
            throw new IllegalStateException(
                    "ADMIN_PASSWORD is not configured. Backend cannot start without admin user."
            );
        }
    }

    private void createAdmin() {
        User admin = commandService.createInternal(
                adminProperties.getFirstName(),
                adminProperties.getLastName(),
                adminProperties.getEmail(),
                adminProperties.getPassword(),
                UserType.ADMIN
        );

        log.info("Default admin placed: id={}, email={}", admin.getId(), admin.getEmail());
    }
}