package com.mayak.ietms.features.user.domain.model;

/**
 * Lifecycle status of a system user.
 * Inactive users cannot authenticate.
 */
public enum UserStatus {
    /** User is active and can log in. */
    ACTIVE,
    /** User is deactivated (e.g. dismissed employee). Cannot log in. */
    INACTIVE
}