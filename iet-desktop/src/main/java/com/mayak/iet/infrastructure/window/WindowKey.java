package com.mayak.iet.infrastructure.window;

public record WindowKey(
        String fxmlPath,
        Object discriminator) {
}