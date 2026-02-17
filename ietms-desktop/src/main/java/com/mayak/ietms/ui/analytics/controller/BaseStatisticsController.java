package com.mayak.ietms.ui.analytics.controller;

import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.ui.core.SecuredView;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.ui.core.UserPermissions;

public abstract class BaseStatisticsController implements SecuredView, ViewLifecycle {

    protected UserResponseDto loggedInUser;
    protected UserPermissions permissions;

    @Override
    public void setLoggedInUser(UserResponseDto user) {
        this.loggedInUser = user;
        this.permissions = new UserPermissions(user);
    }

    protected Long getUserDepartmentId() {
        return loggedInUser != null && loggedInUser.profile() != null
                ? loggedInUser.profile().departmentId()
                : null;
    }

    /**
     * Effective department for statistics.
     * Non-admin: user department
     * Admin: selected department from UI
     */
    protected Long getEffectiveDepartmentId() {
        Long userDept = getUserDepartmentId();
        return userDept != null ? userDept : getSelectedDepartmentId();
    }

    /**
     * Must be implemented by concrete controller
     * (usually returns value from ComboBox)
     */
    protected abstract Long getSelectedDepartmentId();
}