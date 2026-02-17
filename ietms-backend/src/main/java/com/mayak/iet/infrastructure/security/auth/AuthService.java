package com.mayak.iet.infrastructure.security.auth;

import com.mayak.iet.features.user.domain.model.User;
import com.mayak.iet.shared.exception.business.AuthenticationException;
import com.mayak.iet.features.user.infra.persistence.UserRepository;
import com.mayak.iet.infrastructure.security.jwt.JwtService;
import com.mayak.iet.features.user.application.UserPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserPermissionService userPermissionService;

    public String login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AuthenticationException("Invalid email or password")
                );

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        var perms = userPermissionService.getPermissions(user);

        return jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                perms
        );
    }
}