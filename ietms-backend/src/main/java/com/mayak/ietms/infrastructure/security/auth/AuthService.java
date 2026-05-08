package com.mayak.ietms.infrastructure.security.auth;

import com.mayak.ietms.features.license.application.LicenseQueryService;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.shared.exception.business.AuthenticationException;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.infrastructure.security.jwt.JwtService;
import com.mayak.ietms.features.user.application.UserPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user authentication and JWT token generation.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserPermissionService userPermissionService;
    private final LicenseQueryService licenseQueryService;

    /**
     * Authenticates a user by email and password and returns a signed JWT token.
     * Non-admin users are blocked from logging in if no active license is present.
     *
     * @param email    the user's email address
     * @param password the raw password to verify
     * @return signed JWT token
     * @throws AuthenticationException if credentials are invalid or no active license exists for non-admin users
     */
    @Transactional
    public String login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password!"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid email or password!");
        }

        if (user.getUserType() != UserType.ADMIN) {
            try {
                licenseQueryService.getActiveLicenseInfo();
            } catch (Exception e) {
                throw new AuthenticationException("No active license. Please contact your administrator.");
            }
        }

        user.incrementTokenVersion();
        userRepository.save(user);

        var perms = userPermissionService.getPermissions(user);

        return jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                perms,
                user.getTokenVersion()
        );
    }
}