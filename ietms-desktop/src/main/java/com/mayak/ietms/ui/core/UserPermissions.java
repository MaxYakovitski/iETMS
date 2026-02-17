package com.mayak.ietms.ui.core;

import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.user.dto.enums.PriorityDto;
import com.mayak.ietms.user.dto.enums.RoleDto;
import com.mayak.ietms.user.dto.enums.UserTypeDto;

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
        return isAdmin() || user.profile().role() == RoleDto.TRANSPORT_SPECIALIST;
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

    public boolean canViewPlanner() {
        return isAdmin() || isManager()
                || hasRole(RoleDto.CLIENT_SPECIALIST)
                || hasRole(RoleDto.SALES_SPECIALIST)
                || hasRole(RoleDto.TRANSPORT_SPECIALIST);
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

    private boolean hasRole(RoleDto role) {
        return user.profile() != null && user.profile().role() == role;
    }

    private boolean hasPriority(PriorityDto priority) {
        return user.profile() != null && user.profile().priority() == priority;
    }
}
