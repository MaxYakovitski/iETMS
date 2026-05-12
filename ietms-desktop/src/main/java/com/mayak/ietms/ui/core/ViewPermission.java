package com.mayak.ietms.ui.core;

/**
 * Defines the set of UI sections that require an explicit permission check
 * before the view is loaded.
 *
 * <p>Each constant maps to a corresponding method in {@link UserPermissions}.
 * The mapping is centralised in {@link UserPermissions#hasPermission(ViewPermission)}.
 *
 * @see RequiresPermission
 * @see UserPermissions
 */
public enum ViewPermission {

    /** Access to the CRM module (companies, contracts). */
    CRM,

    /** Access to the analytics module. */
    ANALYTICS,

    /** Access to the administration panel (users, departments, license). */
    ADMINISTRATION,

    /** Access to the client requests workspace. */
    CLIENT_REQUESTS,

    /** Access to the transport requests workspace. */
    TRANSPORT_REQUESTS
}