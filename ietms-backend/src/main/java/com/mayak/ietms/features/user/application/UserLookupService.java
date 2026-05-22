package com.mayak.ietms.features.user.application;

import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.user.dto.UserLookupDto;
import com.mayak.ietms.user.dto.UserNameDto;
import com.mayak.ietms.features.user.infra.mapping.UserResponseMapper;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;
    private final UserResponseMapper userMapper;

    public UserLookupDto toShortDto(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId).map(userMapper::toShortDto).orElse(null);
    }

    public Set<UserLookupDto> toShortDtoSet(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Set.of();
        return userRepository.findAllById(userIds).stream().map(userMapper::toShortDto).collect(Collectors.toSet());
    }

    public Map<Long, UserNameDto> getNames(Set <Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> new UserNameDto(u.getName(), u.getSurname())));
    }

}