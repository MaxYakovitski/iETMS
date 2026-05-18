package com.mayak.ietms.features.user.api;

import com.mayak.ietms.user.dto.UserLookupDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.features.user.domain.enums.Role;
import com.mayak.ietms.infrastructure.security.current.CurrentUserId;
import com.mayak.ietms.features.user.application.UserProfileQueryService;
import com.mayak.ietms.features.user.application.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management and profile queries")
@RequiredArgsConstructor
public class UserQueryController {

    private final UserQueryService queryService;
    private final UserProfileQueryService profileQueryService;

    @GetMapping("/me")
    public UserResponseDto getMe(@CurrentUserId Long userId) {
        return queryService.findById(userId);
    }

    @GetMapping
    public List<UserResponseDto> findAll() {
        return queryService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponseDto findById(@PathVariable Long id) {
        return queryService.findById(id);
    }

    @GetMapping("/client-specialists")
    @Operation(summary = "Find client specialists",
               description = "Returns users with CLIENT_SPECIALIST role and manager.")
    public List<UserResponseDto> findClientSpecialists() {
        return profileQueryService.findAllByRoleWithManager(Role.CLIENT_SPECIALIST);
    }

    @GetMapping("/client-specialists/by-department/{id}")
    public List<UserResponseDto> findClientSpecialistsByDepartment(@PathVariable("id") Long id) {
        return profileQueryService.findByRoleWithManager(id, Role.CLIENT_SPECIALIST);
    }

    @GetMapping("/client-specialists/lookup/by-department/{id}")
    public List<UserLookupDto> findClientSpecialistsLookupByDepartment(@PathVariable("id") Long id) {
        return profileQueryService.findLookupByRoleWithManager(id, Role.CLIENT_SPECIALIST);
    }

    @GetMapping("/colleagues")
    @Operation(summary = "Find colleagues",
               description = "Returns users from the same department as the current user.")
    public List<UserResponseDto> findColleagues(@CurrentUserId Long userId) {
        return profileQueryService.findColleagues(userId);
    }

    @GetMapping("/colleagues/by-department/{id}")
    public List<UserResponseDto> findColleaguesByDepartment(@PathVariable("id") Long id) {
        return profileQueryService.findByDepartment(id);
    }

    @GetMapping("/colleagues/lookup/by-department/{id}")
    public List<UserLookupDto> findColleaguesLookupByDepartment(@PathVariable("id") Long id) {
        return profileQueryService.findLookupByDepartment(id);
    }
}