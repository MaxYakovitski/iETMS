package com.mayak.ietms.features.user.api;

import com.mayak.ietms.user.dto.*;
import com.mayak.ietms.features.user.application.UserCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management and profile queries")
@RequiredArgsConstructor
public class UserCommandController {

    private final UserCommandService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Create user",
               description = "Password must be at least 8 characters with letters and numbers. " +
                       "Role and priority are required only for EMPLOYEE type.")
    public UserResponseDto create(@RequestBody UserCreateDto dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public UserResponseDto update(@PathVariable("id") Long id, @RequestBody UserUpdateDto dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public void delete(@PathVariable("id") Long id) {
        userService.delete(id);
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    @Operation(summary = "Change user password",
               description = "Password must be at least 8 characters with letters and numbers.")
    public void changePassword(@PathVariable("id") Long id, @RequestBody ChangePasswordDto dto) {
        userService.changePassword(id, dto.newPassword());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public void changeStatus(@PathVariable("id") Long id, @RequestBody ChangeUserStatusDto dto) {
        userService.changeStatus(id, dto.status());
    }
}