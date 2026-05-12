package com.mayak.ietms.ui.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a JavaFX controller as requiring a specific {@link ViewPermission}
 * to be accessible by the current user.
 *
 * <p>Applied by {@link com.mayak.ietms.ui.navigation.NavigationService}
 * (or equivalent guard) before loading the view. Controllers without this
 * annotation are considered unrestricted.
 *
 * @see ViewPermission
 * @see com.mayak.ietms.ui.core.UserPermissions#hasPermission(ViewPermission)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /** The permission required to open this view. */
    ViewPermission value();
}