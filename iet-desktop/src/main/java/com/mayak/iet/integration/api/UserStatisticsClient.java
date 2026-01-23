package com.mayak.iet.integration.api;

import com.mayak.iet.statistics.UserPersonalStats;

public interface UserStatisticsClient {
    UserPersonalStats getCurrentMonthStats(Long userId);
}