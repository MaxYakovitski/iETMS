package com.mayak.ietms.integration.api;

import com.mayak.ietms.statistics.UserPersonalStats;

public interface UserStatisticsClient {
    UserPersonalStats getCurrentMonthStats(Long userId);
}