package com.mayak.iet.features.request.domain.model;

public interface RefuseReason {
    String getCode();
    default boolean isUserSelectable() {
        return true;
    }
}