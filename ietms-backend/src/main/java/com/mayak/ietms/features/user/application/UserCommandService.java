package com.mayak.ietms.features.user.application;

import com.mayak.ietms.features.user.domain.model.Profile;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.user.domain.model.UserStatus;
import com.mayak.ietms.user.dto.UserCreateDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.user.dto.UserUpdateDto;
import com.mayak.ietms.user.dto.enums.PriorityDto;
import com.mayak.ietms.user.dto.enums.RoleDto;
import com.mayak.ietms.user.dto.enums.UserStatusDto;
import com.mayak.ietms.user.dto.enums.UserTypeDto;
import com.mayak.ietms.features.user.domain.enums.Priority;
import com.mayak.ietms.features.user.domain.enums.Role;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.shared.exception.business.UserInUseException;
import com.mayak.ietms.shared.exception.business.UserNotFoundException;
import com.mayak.ietms.shared.exception.validation.ValidationException;
import com.mayak.ietms.features.user.infra.mapping.UserCreateMapper;
import com.mayak.ietms.features.user.infra.mapping.UserResponseMapper;
import com.mayak.ietms.features.user.infra.mapping.UserUpdateMapper;
import com.mayak.ietms.features.department.infra.persistence.DepartmentRepository;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.features.user.application.validation.UserCreateBackendValidator;
import com.mayak.ietms.features.user.application.validation.UserUpdateBackendValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCommandService {

    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserCreateBackendValidator userCreateBackendValidator;
    private final UserUpdateBackendValidator userUpdateBackendValidator;

    private final UserCreateMapper userCreateMapper;
    private final UserUpdateMapper userUpdateMapper;
    private final UserResponseMapper userResponseMapper;


    // --- CREATE ---
    @Transactional
    public UserResponseDto create(UserCreateDto dto) {
        validateCreate(dto);

        User user = userCreateMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        applyProfile(
                user,
                dto.getUserType(),
                dto.getDepartmentId(),
                dto.getRole(),
                dto.getPriority()
        );

        User saved = userRepository.save(user);
        log.info("User placed: id={}, email={}", saved.getId(), saved.getEmail());
        return userResponseMapper.toDto(saved);
    }

    @Transactional
    public User createInternal(String name, String surname, String email, String rawPassword, UserType userType) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);
        user.setUserType(userType);
        user.setPassword(passwordEncoder.encode(rawPassword));

        return userRepository.save(user);
    }

    // --- UPDATE ---
    @Transactional
    public UserResponseDto update(Long id, UserUpdateDto dto) {
        validateUpdate(id, dto);

        User user = getOrThrow(id);
        userUpdateMapper.update(user, dto);


        applyProfile(
                user,
                dto.getUserType(),
                dto.getDepartmentId(),
                dto.getRole(),
                dto.getPriority()
        );

        User saved = userRepository.save(user);

        log.info("User updated: id={}", saved.getId());
        return userResponseMapper.toDto(saved);
    }

    @Transactional
    public void changePassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return;
        }

        User user = getOrThrow(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        log.info("Password changed for user id={}", userId);
    }

    /**
     * Changes the lifecycle status of a user.
     * Increments token version to immediately invalidate active JWT tokens.
     */
    @Transactional
    public void changeStatus(Long id, UserStatusDto statusDto) {
        User user = getOrThrow(id);
        user.setStatus(UserStatus.valueOf(statusDto.name()));
        user.incrementTokenVersion();
        log.info("User status changed: id={}, status={}", id, statusDto);
    }

    // --- DELETE ---
    @Transactional
    public void delete(Long id) {
        User user = getOrThrow(id);

        boolean usedAsAuthor = requestRepository.existsByAuthorId(id);
        boolean usedAsAssigned = requestRepository.existsByDispatcherId(id);
        boolean usedAsCompetitor = requestRepository.existsByCompetitor(id);

        if (usedAsAuthor || usedAsAssigned || usedAsCompetitor) {
            throw new UserInUseException(id);
        }

        userRepository.delete(user);
        log.info("User deleted: id={}", id);
    }

    // ---------- helpers ----------
    private void validateCreate(UserCreateDto dto) {
        var result = userCreateBackendValidator.isValid(dto);
        if (!result.isValid()) {
            throw new ValidationException(result);
        }
    }

    private void validateUpdate(Long userId, UserUpdateDto dto) {
        var result = userUpdateBackendValidator.isValid(userId, dto);
        if (!result.isValid()) {
            throw new ValidationException(result);
        }
    }

    private User getOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void applyProfile(User user, UserTypeDto userType, Long departmentId, RoleDto roleDto, PriorityDto priorityDto) {
        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            profile.setUser(user);
            user.setProfile(profile);
        }

        if (departmentId != null) {
            profile.setDepartment(departmentRepository.getReferenceById(departmentId));
        }

        switch (userType) {
            case MANAGER -> {
                profile.setPriority(toPriority(priorityDto));
                profile.setRole(null);
            }

            case EMPLOYEE -> {
                profile.setRole(toRole(roleDto));
                profile.setPriority(null);
            }

            default -> {
                profile.setRole(null);
                profile.setPriority(null);
            }
        }
    }

    private Priority toPriority(PriorityDto dto) {
        return dto != null ? Priority.valueOf(dto.name()) : null;
    }

    private Role toRole(RoleDto dto) {
        return dto != null ? Role.valueOf(dto.name()) : null;
    }
}