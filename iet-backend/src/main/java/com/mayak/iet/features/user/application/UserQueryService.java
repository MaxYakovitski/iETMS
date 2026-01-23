package com.mayak.iet.features.user.application;

import com.mayak.iet.user.dto.UserResponseDto;
import com.mayak.iet.features.user.domain.model.User;
import com.mayak.iet.shared.exception.business.UserNotFoundException;
import com.mayak.iet.features.user.infra.mapping.UserResponseMapper;
import com.mayak.iet.features.user.infra.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserResponseMapper userResponseMapper;

    public UserResponseDto findById(Long id) {
        return userRepository.findById(id)
                .map(userResponseMapper::toDto)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<UserResponseDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(userResponseMapper::toDto)
                .toList();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.trim());
    }

    public User getEntityById(Long id) {
        if (id == null) return null;

        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

}