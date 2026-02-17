package com.mayak.iet.request.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum TransportTypeDto {
    TILT ("TILT"),
    REF("REF"),
    JUMBO("JUMBO-120m3"),
    MEGA("MEGA"),
    BOX("box trailer"),
    SMALL ("small"),
    CONTAINER ("container"),;

    private final String label;

    public static TransportTypeDto fromLabel(String raw) {
        if (raw == null || raw.isBlank()) return null;

        String normalized = raw.trim();

        return Arrays.stream(values())
                .filter(t -> t.label.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(null);
    }
}