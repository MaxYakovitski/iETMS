package com.mayak.ietms.infrastructure.security.auth;

import com.mayak.ietms.features.license.application.LicenseQueryService;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.infrastructure.security.auth.domain.RefreshToken;
import com.mayak.ietms.infrastructure.security.auth.dto.LoginResponse;
import com.mayak.ietms.infrastructure.security.auth.persistence.RefreshTokenRepository;
import com.mayak.ietms.shared.exception.business.AuthenticationException;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.infrastructure.security.jwt.JwtService;
import com.mayak.ietms.features.user.application.UserPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Handles user authentication and JWT token generation.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserPermissionService userPermissionService;
    private final LicenseQueryService licenseQueryService;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    /**
     * Authenticates a user by email and password and returns a token pair.
     * Non-admin users are blocked from logging in if no active license is present.
     * All previous refresh tokens for the user are revoked to enforce a single active session.
     *
     * @param email    the user's email address
     * @param password the raw password to verify
     * @return access and refresh token pair
     * @throws AuthenticationException if credentials are invalid, no active license exists
     *                                 for non-admin users
     */
    @Transactional
    public LoginResponse login(String email, String password) {
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
        refreshTokenRepository.revokeAllByUserId(user.getId());
        var perms = userPermissionService.getPermissions(user);
        String accessToken  = jwtService.generateToken(user.getId(), user.getEmail(), perms);
        String refreshToken = createRefreshToken(user.getId());
        return new LoginResponse(accessToken, refreshToken);
    }

    /**
     * Issues a new access and refresh token pair in exchange for a valid refresh token.
     * The provided token is revoked immediately (rotation) to prevent reuse.
     *
     * @param rawToken the raw refresh token received from the client
     * @return new access and refresh token pair
     * @throws AuthenticationException if the token is invalid, expired, or revoked
     */
    @Transactional
    public LoginResponse refresh(String rawToken) {
        String hash = sha256(rawToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthenticationException("Refresh token expired or revoked");
        }
        stored.setRevoked(true);
        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        var perms = userPermissionService.getPermissions(user);
        String newAccess  = jwtService.generateToken(user.getId(), user.getEmail(), perms);
        String newRefresh = createRefreshToken(user.getId());
        return new LoginResponse(newAccess, newRefresh);
    }

    /**
     * Revokes the given refresh token, effectively ending the user's session.
     * If the token is not found, the call is silently ignored.
     *
     * @param rawToken the raw refresh token received from the client
     */
    @Transactional
    public void logout(String rawToken) {
        String hash = sha256(rawToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(t -> t.setRevoked(true));
    }

    private String createRefreshToken(Long userId) {
        String raw  = UUID.randomUUID().toString();
        String hash = sha256(raw);
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(hash);
        token.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        refreshTokenRepository.save(token);
        return raw;
    }

    private String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}