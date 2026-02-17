package com.mayak.ietms.features.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Getter
public enum UserType {
    ADMIN(new HashSet<>(Set.of(Permission.values()))),

    MANAGER(new HashSet<>(Set.of(
            Permission.VIEW_CLIENT_REQUESTS,
            Permission.MANAGE_CLIENT_REQUESTS,

            Permission.VIEW_PLANNER,
            Permission.MANAGE_PLANNER,

            Permission.VIEW_CRM,
            Permission.MANAGE_CRM,

            Permission.VIEW_ANALYTICS,

            Permission.VIEW_ADMINISTRATION,
            Permission.MANAGE_USERS,
            Permission.MANAGE_LOCATIONS
    ))),

    EMPLOYEE(EnumSet.noneOf(Permission.class));

    private final Set<Permission> permissions;
}