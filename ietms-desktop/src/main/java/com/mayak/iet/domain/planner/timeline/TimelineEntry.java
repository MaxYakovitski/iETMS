package com.mayak.iet.domain.planner.timeline;

import java.time.LocalDateTime;

public record TimelineEntry(
        String label,
        LocalDateTime time,
        TimelineColor color) {
}