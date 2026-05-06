package com.mayak.ietms.features.user.domain.enums;

/**
 * Defines fine-grained permissions used for access control across the application.
 * ADMIN users are granted all permissions automatically.
 */
public enum Permission {
    VIEW_CRM,
    MANAGE_CRM,

    VIEW_CLIENT_REQUESTS,
    MANAGE_CLIENT_REQUESTS,
    VIEW_TRANSPORT_REQUESTS,
    MANAGE_TRANSPORT_REQUESTS,

    VIEW_PLANNER,
    MANAGE_PLANNER,

    VIEW_ANALYTICS,

    VIEW_ADMINISTRATION,
    MANAGE_USERS,
    MANAGE_DEPARTMENTS,
    MANAGE_LOCATIONS,

    /** Allows viewing and managing the system license. */
    MANAGE_LICENSE,
}