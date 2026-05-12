package com.mayak.ietms.ui.core;

import com.mayak.ietms.user.dto.UserResponseDto;

/**
 * Marks a controller that requires the current user's context.
 *
 * <p>Implemented by controllers that need to tailor their content or
 * permissions to the logged-in user. Called by {@link com.mayak.ietms.ui.home.HomeController#navigateTo}
 * before {@link ViewLifecycle#onShow()}.
 */
public interface SecuredView {

    /** Injects the authenticated user into this controller. */
    void setLoggedInUser(UserResponseDto user);
}