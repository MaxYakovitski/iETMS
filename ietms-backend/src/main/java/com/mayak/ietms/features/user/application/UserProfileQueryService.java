package com.mayak.ietms.features.user.application;

import com.mayak.ietms.user.dto.UserLookupDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.user.domain.enums.Role;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.features.user.infra.mapping.UserResponseMapper;
import com.mayak.ietms.features.user.infra.persistence.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileQueryService {

    private final ProfileRepository profileRepository;
    private final UserResponseMapper userResponseMapper;

    public List<UserResponseDto> findByDepartment(Long departmentId) {
        return loadUsersByDepartment(departmentId).stream()
                .map(userResponseMapper::toDto)
                .toList();
    }

    public List<UserResponseDto> findByRoleWithManager(Long departmentId, Role role) {
        return filterByRoleWithManager(loadUsersByDepartment(departmentId), role).stream()
                .map(userResponseMapper::toDto)
                .toList();
    }

    public List<UserResponseDto> findAllByRoleWithManager(Role role) {
        return profileRepository.findUsersByRoleOrManager(role).stream()
                .map(userResponseMapper::toDto)
                .toList();
    }

    public List<UserResponseDto> findColleagues(Long userId) {
        Long departmentId = profileRepository
                .findDepartmentIdByUserId(userId)
                .orElse(null);
        List<User> users = (departmentId == null)
                ? profileRepository.findAllUsers()
                : profileRepository.findUsersByDepartmentId(departmentId);
        return users.stream()
                .map(userResponseMapper::toDto)
                .toList();
    }

    public List<UserLookupDto> findLookupByDepartment(Long departmentId) {
        return loadUsersByDepartment(departmentId).stream()
                .map(userResponseMapper::toShortDto)
                .toList();
    }

    public List<UserLookupDto> findLookupByRoleWithManager(Long departmentId, Role role) {
        return filterByRoleWithManager(loadUsersByDepartment(departmentId), role).stream()
                .map(userResponseMapper::toShortDto)
                .toList();
    }

    private List<User> loadUsersByDepartment(Long departmentId) {
        return departmentId == null
                ? profileRepository.findAllUsers()
                : profileRepository.findUsersByDepartmentId(departmentId);
    }

    private List<User> filterByRoleWithManager(List<User> users, Role role) {
        return users.stream()
                .filter(u -> u.getProfile().getRole() == role || u.getUserType() == UserType.MANAGER)
                .toList();
    }
}