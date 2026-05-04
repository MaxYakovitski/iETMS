package com.mayak.ietms.features.request.application.access;

public record RequestVisibilityScope(Long departmentId) {

    public static RequestVisibilityScope all() {
        return new RequestVisibilityScope(null);
    }

    public static RequestVisibilityScope forDepartment(Long departmentId) {
        return new RequestVisibilityScope(departmentId);
    }

    public boolean isRestricted() {
        return departmentId != null;
    }
}