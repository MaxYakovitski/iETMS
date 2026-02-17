package com.mayak.ietms.features.user.application;

import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.features.user.domain.enums.Permission;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.shared.exception.business.AuthenticationException;
import com.mayak.ietms.shared.exception.business.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class UserPermissionService {
    public Set<Permission> getPermissions(User user) {
        if (user == null) {
            throw new AuthenticationException("User is not authenticated");
        }

        if (user.getUserType() == UserType.ADMIN) {
            return Set.of(Permission.values());
        }

        Set<Permission> result = new HashSet<>();

        if (user.getUserType() != null) {
            result.addAll(user.getUserType().getPermissions());
        }

        var profile = user.getProfile();
        if (profile != null && profile.getRole() != null) {
            result.addAll(profile.getRole().getPermissions());
        }

        return Collections.unmodifiableSet(result);
    }

    public void requirePermission(User user, Permission permission) {
        if (!getPermissions(user).contains(permission)) {
            throw new UnauthorizedException(
                    "Permission required: " + permission
            );
        }
    }

    public boolean hasPermission(User user, Permission permission) {
        try {
            return getPermissions(user).contains(permission);
        } catch (AuthenticationException e) {
            return false;
        }
    }
}