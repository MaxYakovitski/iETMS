package com.mayak.ietms.ui.core;

import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.user.dto.enums.PriorityDto;
import com.mayak.ietms.user.dto.enums.RoleDto;
import com.mayak.ietms.user.dto.enums.UserTypeDto;

/**
 * Encapsulates permission checks for a single authenticated user.
 *
 * <p>Wraps {@link com.mayak.ietms.user.dto.UserResponseDto} and exposes
 * named predicates instead of scattering role/type comparisons across
 * the UI layer. Use {@link #hasPermission(ViewPermission)} for navigation
 * guards driven by {@link RequiresPermission}.
 *
 * <p>Instances are created per session and should be treated as immutable
 * — the underlying {@code UserResponseDto} is never modified.
 */
public final class UserPermissions {

    private final UserResponseDto user;

    public UserPermissions(UserResponseDto user) {
        this.user = user;
    }

    // -------------------- BASE --------------------
    public boolean isAdmin() {
        return user.userType() == UserTypeDto.ADMIN;
    }

    public boolean isManager() {
        return user.userType() == UserTypeDto.MANAGER;
    }

    // -------------------- REQUESTS --------------------
    public boolean canViewTransportRequests() {
        return isAdmin() || hasRole(RoleDto.TRANSPORT_SPECIALIST);
    }

    public boolean canViewClientRequests() {
        return isAdmin() || isManager()
                || hasRole(RoleDto.SALES_SPECIALIST)
                || hasRole(RoleDto.CLIENT_SPECIALIST)
                || hasRole(RoleDto.ASSISTANT);
    }

    // -------------------- CRM --------------------
    public boolean canViewCrm() {
        return isAdmin() || isManager()
                || hasRole(RoleDto.SALES_SPECIALIST)
                || hasRole(RoleDto.CLIENT_SPECIALIST)
                || hasRole(RoleDto.ASSISTANT);
    }

    public boolean canViewMyShipments() {
        return isAdmin() || isManager()
                || hasRole(RoleDto.CLIENT_SPECIALIST)
                || hasRole(RoleDto.SALES_SPECIALIST);
    }

    // -------------------- ANALYTICS --------------------
    public boolean canViewAnalytics() {
        return isAdmin() || isManager();
    }

    // -------------------- ADMINISTRATION --------------------
    public boolean canViewAdministration() {
        return isAdmin();
    }

    // -------------------- NAVIGATION --------------------

    /**
     * Returns {@code true} if the user is allowed to access the view
     * identified by {@code permission}.
     *
     * <p>This is the single entry point used by navigation guards
     * ({@link RequiresPermission}); all other {@code canView*} methods
     * are implementation detail.
     */
    public boolean hasPermission(ViewPermission permission) {
        return switch (permission) {
            case CRM                -> canViewCrm();
            case ANALYTICS          -> canViewAnalytics();
            case ADMINISTRATION     -> canViewAdministration();
            case CLIENT_REQUESTS    -> canViewClientRequests();
            case TRANSPORT_REQUESTS -> canViewTransportRequests();
        };
    }

    // -------- weight / SLA --------
    public boolean hasHighPriority() {
        return user.profile().priority() == PriorityDto.HIGH;
    }

    public boolean hasMediumPriority() {
        return user.profile().priority() == PriorityDto.MEDIUM;
    }

    public boolean hasLowPriority() {
        return user.profile().priority() == PriorityDto.LOW;
    }

    // profile may be null for ADMIN users who have no assigned role
    private boolean hasRole(RoleDto role) {
        return user.profile() != null && user.profile().role() == role;
    }

}