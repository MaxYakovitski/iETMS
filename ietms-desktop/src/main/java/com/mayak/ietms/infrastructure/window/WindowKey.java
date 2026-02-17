package com.mayak.ietms.infrastructure.window;

public record WindowKey(
        String fxmlPath,
        Object discriminator) {
}