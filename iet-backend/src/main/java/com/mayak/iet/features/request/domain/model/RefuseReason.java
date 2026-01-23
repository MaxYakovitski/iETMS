package com.mayak.iet.features.request.domain.model;

public interface RefuseReason {
    String getCode();
    String getLabel();

    default boolean isUserSelectable() {
        return true;
    }
}