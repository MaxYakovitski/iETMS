package com.mayak.ietms.features.user.domain.enums;

import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.user.application.UserPermissionService;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Getter
public enum Priority {
    LOW(new HashSet<>(Set.of(
            Permission.VIEW_CLIENT_REQUESTS,
            Permission.VIEW_ANALYTICS))),

    MEDIUM(new HashSet<>(Set.of(
            Permission.VIEW_CLIENT_REQUESTS,
            Permission.VIEW_ANALYTICS,
            Permission.VIEW_ADMINISTRATION))),

    HIGH(new HashSet<>(Set.of(
            Permission.VIEW_CLIENT_REQUESTS,
            Permission.VIEW_ANALYTICS,
            Permission.VIEW_ADMINISTRATION)));

    private final Set<Permission> requiredPermissions;

    public boolean canAccess(User user, UserPermissionService permissionService) {
        Set<Permission> userPermissions = permissionService.getPermissions(user);
        return userPermissions.containsAll(requiredPermissions);
    }
}
