package com.mayak.ietms.features.request.application.access;

public record AccessDecision (boolean allowed, String reason) {

    public static AccessDecision allow() {
        return new AccessDecision(true, null);
    }

    public static AccessDecision deny(String reason) {
        return new AccessDecision(false, reason);
    }
}