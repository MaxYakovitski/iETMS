package com.mayak.iet.features.user.application;

import com.mayak.iet.user.dto.UserLookupDto;
import com.mayak.iet.user.dto.UserNameDto;
import com.mayak.iet.features.user.infra.mapping.UserResponseMapper;
import com.mayak.iet.features.user.infra.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;
    private final UserResponseMapper userMapper;

    public UserLookupDto toShortDto(Long userId) {
        if (userId == null) return null;

        return userRepository.findById(userId)
                .map(userMapper::toShortDto)
                .orElse(null);
    }

    public Set<UserLookupDto> toShortDtoSet(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Set.of();

        return userRepository.findAllById(userIds).stream()
                .map(userMapper::toShortDto)
                .collect(Collectors.toSet());
    }

    public UserNameDto getName(Long userId) {
        if (userId == null) return null;

        return userRepository.findById(userId)
                .map(u -> new UserNameDto(u.getName(), u.getSurname()))
                .orElse(null);
    }

}
