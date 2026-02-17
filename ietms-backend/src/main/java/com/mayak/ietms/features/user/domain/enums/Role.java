package com.mayak.ietms.features.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Getter
public enum Role {
    CLIENT_SPECIALIST(new HashSet<>(Set.of(
            Permission.VIEW_CLIENT_REQUESTS,
            Permission.MANAGE_CLIENT_REQUESTS,
            Permission.VIEW_PLANNER,
            Permission.MANAGE_PLANNER,
            Permission.VIEW_CRM,
            Permission.MANAGE_CRM))),

    TRANSPORT_SPECIALIST(new HashSet<>(Set.of(
            Permission.VIEW_TRANSPORT_REQUESTS,
            Permission.MANAGE_TRANSPORT_REQUESTS,
            Permission.VIEW_PLANNER,
            Permission.MANAGE_PLANNER))),

    SALES_SPECIALIST(new HashSet<>(Set.of(
            Permission.VIEW_CLIENT_REQUESTS,
            Permission.MANAGE_CLIENT_REQUESTS,
            Permission.VIEW_PLANNER,
            Permission.MANAGE_PLANNER,
            Permission.VIEW_CRM))),

    ASSISTANT(new HashSet<>(Set.of(
            Permission.VIEW_CLIENT_REQUESTS,
            Permission.MANAGE_LOCATIONS
    )));

    private final Set<Permission> permissions;
}