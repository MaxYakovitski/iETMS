package com.mayak.ietms.features.user.application;

import com.mayak.ietms.common.validation.ValidationResult;
import com.mayak.ietms.common.validation.ValidationUtils;
import com.mayak.ietms.features.license.application.LicenseQueryService;
import com.mayak.ietms.features.user.domain.model.Profile;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.user.domain.model.UserStatus;
import com.mayak.ietms.shared.exception.business.DepartmentNotFoundException;
import com.mayak.ietms.shared.exception.business.LicenseLimitExceededException;
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

/**
 * Handles user lifecycle operations: creation, update, password change, status change, and deletion.
 */
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
    private final LicenseQueryService licenseQueryService;


    /**
     * Creates a new user and assigns their profile based on user type.
     *
     * @throws LicenseLimitExceededException if the active user limit has been reached
     * @throws ValidationException if the provided data fails validation
     */
    @Transactional
    public UserResponseDto create(UserCreateDto dto) {
        validateCreate(dto);
        int activeUsers = userRepository.countByStatusAndUserTypeNot(UserStatus.ACTIVE, UserType.ADMIN);
        int maxUsers = licenseQueryService.getMaxUsers();
        if (activeUsers >= maxUsers) {
            throw new LicenseLimitExceededException("User limit reached for current license: " + maxUsers);
        }
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

    /**
     * Creates a user internally without validation or profile assignment.
     * Intended for system-level initialization (e.g. seeding an admin account).
     */
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

    /**
     * Updates an existing user's data and profile.
     *
     * @throws UserNotFoundException if no user with the given id exists
     * @throws ValidationException if the provided data fails validation
     */
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

    /**
     * Changes the password for the given user.
     * Does nothing if {@code newPassword} is blank or null.
     *
     * @throws UserNotFoundException if no user with the given id exists
     * @throws ValidationException if the password does not meet the requirements
     */
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return;
        }
        if (!ValidationUtils.isValidPassword(newPassword)) {
            var result = new ValidationResult();
            result.add("password", "Password must be at least 8 characters and contain letters and numbers!");
            throw new ValidationException(result);
        }
        User user = getOrThrow(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.incrementTokenVersion();
        log.info("Password changed for user id={}", userId);
    }

    /**
     * Changes the lifecycle status of a user.
     * Increments token version to immediately invalidate active JWT tokens.
     *
     * @throws LicenseLimitExceededException if activating a user would exceed the license user limit
     */
    @Transactional
    public void changeStatus(Long id, UserStatusDto statusDto) {
        User user = getOrThrow(id);
        if (statusDto == UserStatusDto.ACTIVE) {
            int activeUsers = userRepository.countByStatusAndUserTypeNot(UserStatus.ACTIVE, UserType.ADMIN);
            int maxUsers = licenseQueryService.getMaxUsers();
            if (activeUsers >= maxUsers) {
                throw new LicenseLimitExceededException("User limit reached for current license: " + maxUsers);
            }
        }
        user.setStatus(UserStatus.valueOf(statusDto.name()));
        user.incrementTokenVersion();
        log.info("User status changed: id={}, status={}", id, statusDto);
    }

    /**
     * Deletes a user by id.
     * Deletion is blocked if the user is referenced by any request.
     *
     * @throws UserNotFoundException if no user with the given id exists
     * @throws UserInUseException if the user is referenced by existing requests
     */
    @Transactional
    public void delete(Long id) {
        User user = getOrThrow(id);
        boolean usedAsAuthor = requestRepository.existsByAuthorId(id);
        boolean usedAsAssigned = requestRepository.existsByDispatcherId(id);
        boolean usedAsCompetitor = requestRepository.existsByCompetitor(id);
        String fullName = user.getName() + " " + user.getSurname();
        if (usedAsAuthor) {
            throw new UserInUseException(
                    "User \"" + fullName + "\" cannot be deleted because they are an author of existing requests.");
        }
        if (usedAsAssigned) {
            throw new UserInUseException(
                    "User \"" + fullName + "\" cannot be deleted because they are assigned as a dispatcher.");
        }
        if (usedAsCompetitor) {
            throw new UserInUseException(
                    "User \"" + fullName + "\" cannot be deleted because they are a competitor in existing requests.");
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
            profile.setDepartment(departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new DepartmentNotFoundException(departmentId)));
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