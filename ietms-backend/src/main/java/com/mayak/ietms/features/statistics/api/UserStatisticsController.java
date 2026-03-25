package com.mayak.ietms.features.statistics.api;

import com.mayak.ietms.statistics.UserPersonalStats;
import com.mayak.ietms.features.statistics.application.UserPersonalStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics/users")
@RequiredArgsConstructor
public class UserStatisticsController {

    private final UserPersonalStatisticsService statisticsService;

    @GetMapping("/{id}/current-month")
    public UserPersonalStats getCurrentMonthStats(@PathVariable("id") Long userId) {
        return statisticsService.getCurrentMonthStats(userId);
    }
}