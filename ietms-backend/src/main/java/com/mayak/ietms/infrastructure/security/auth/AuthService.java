package com.mayak.ietms.infrastructure.security.auth;

import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.shared.exception.business.AuthenticationException;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.infrastructure.security.jwt.JwtService;
import com.mayak.ietms.features.user.application.UserPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserPermissionService userPermissionService;

    @Transactional
    public String login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
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