package com.mayak.iet.features.request.application.access;

import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.features.request.domain.enums.RequestStatus;
import com.mayak.iet.features.user.domain.model.User;
import com.mayak.iet.features.user.domain.enums.Role;
import com.mayak.iet.features.user.domain.enums.UserType;
import com.mayak.iet.shared.exception.business.AuthenticationException;
import com.mayak.iet.shared.exception.business.UnauthorizedException;
import org.springframework.stereotype.Service;

@Service
public class RequestAccessService {

    public boolean canJoin(User user, Request request) {
        return decideCanJoin(user, request).allowed();
    }

    public void requireCanJoin(User user, Request request) {
        AccessDecision decision = decideCanJoin(user, request);
        if (!decision.allowed()) {
            throw new UnauthorizedException(decision.reason());
        }
    }

    private AccessDecision decideCanJoin(User user, Request request) {
        if (user == null) {
            return AccessDecision.deny("User is not authenticated");
        }

        if (request == null) {
            return AccessDecision.deny("Request is null");
        }

        if (request.isAuthoredBy(user.getId())) {
            return AccessDecision.deny("Author cannot join own request");
        }

        var profile = user.getProfile();
        if (profile == null) {
            return AccessDecision.deny("User profile is missing");
        }

        var role = profile.getRole();
        var userType = user.getUserType();
        if (userType == null) {
            return AccessDecision.deny("User type is missing");
        }

        if (isJoined(user, request)) {
            return AccessDecision.deny("User already joined");
        }

        boolean isSpecialist =
                role == Role.TRANSPORT_SPECIALIST ||
                        role == Role.CLIENT_SPECIALIST;

        boolean isManager = userType == UserType.MANAGER;

        if (isSpecialist || isManager) {
            return AccessDecision.allow();
        }

        return AccessDecision.deny("User role is not allowed to join");
    }

    public boolean canBid(User user, Request request) {
        return decideCanBid(user, request).allowed();
    }

    public void requireCanBid(User user, Request request) {
        AccessDecision decision = decideCanBid(user, request);
        if (!decision.allowed()) {
            throw new UnauthorizedException(decision.reason());
        }
    }

    private AccessDecision decideCanBid(User user, Request request) {

        if (user == null) {
            return AccessDecision.deny("User is not authenticated");
        }

        if (request == null) {
            return AccessDecision.deny("Request is null");
        }

        RequestStatus status = request.getStatus();

        if (!status.allowsBidding()) {
            return AccessDecision.deny("Request status does not allow bidding");
        }

        if (request.isAuthoredBy(user.getId())) {
            return AccessDecision.allow();
        }

        if (!isJoined(user, request)) {
            return AccessDecision.deny("User is not joined to request");
        }

        return AccessDecision.allow();
    }


    public boolean isJoined(User user, Request request) {
        if (user == null || request == null) return false;

        Long userId = user.getId();
        if (userId == null) return false;
        return request.getCompetitorsId().contains(userId);
    }

    public void requireAuthenticated(User user) {
        if (user == null) {
            throw new AuthenticationException("User is not authenticated");
        }
    }
}