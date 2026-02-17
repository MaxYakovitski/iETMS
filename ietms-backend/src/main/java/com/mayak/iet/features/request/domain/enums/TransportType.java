package com.mayak.iet.features.request.domain.enums;

import lombok.Getter;

@Getter
public enum TransportType {
    TILT ("TILT"),
    REF("REF"),
    JUMBO("JUMBO-120m3"),
    MEGA("MEGA"),
    BOX("box trailer"),
    SMALL ("small"),
    CONTAINER ("container"),;

    private final String label;

    TransportType(String string) {
        this.label = string;
    }
}