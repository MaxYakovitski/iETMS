package com.mayak.ietms.ui.analytics.controller;

import com.mayak.ietms.user.dto.UserResponseDto;
import com.mayak.ietms.ui.core.SecuredView;
import com.mayak.ietms.ui.core.ViewLifecycle;
import com.mayak.ietms.ui.core.UserPermissions;

/**
 * Base class for statistics screens.
 *
 * <p>Provides access to the logged-in user's department and
 * resolves the effective department for filtering:
 * non-admin users are scoped to their own department;
 * admins can select any department via the UI.
 */
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
     * Returns the department id to use when loading statistics.
     * For non-admin users returns their own department; for admins
     * delegates to {@link #getSelectedDepartmentId()}.
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