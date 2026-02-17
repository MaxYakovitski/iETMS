package com.mayak.ietms.features.user.api;

import com.mayak.ietms.user.dto.ChangePasswordDto;
import com.mayak.ietms.user.dto.UserCreateDto;
import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.user.dto.UserUpdateDto;
import com.mayak.ietms.features.user.application.UserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserCommandController {

    private final UserCommandService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public UserResponseDto create(@RequestBody UserCreateDto dto) {
        return userService.create(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public UserResponseDto update(
            @PathVariable("id") Long id,
            @RequestBody UserUpdateDto dto
    ) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public void delete(@PathVariable("id") Long id) {
        userService.delete(id);
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public void changePassword(
            @PathVariable("id") Long id,
            @RequestBody ChangePasswordDto dto
    ) {
        userService.changePassword(id, dto.newPassword());
    }
}