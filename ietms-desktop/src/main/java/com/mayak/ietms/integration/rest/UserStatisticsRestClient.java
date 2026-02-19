package com.mayak.ietms.integration.rest;

import com.mayak.ietms.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.ietms.integration.api.UserStatisticsClient;
import com.mayak.ietms.ui.core.SessionManager;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.mayak.ietms.statistics.UserPersonalStats;

@Service
public class UserStatisticsRestClient extends AbstractRestClient implements UserStatisticsClient {

    private static final String API = "/api/statistics/users";

    public UserStatisticsRestClient(RestTemplate restTemplate, BackendConnectionMonitor connectionMonitor, SessionManager sessionManager) {
        super(restTemplate, connectionMonitor, sessionManager);
    }

    public UserPersonalStats getCurrentMonthStats(Long userId) {
        return exchangeSafely(() ->
                restTemplate.exchange(
                        API + "/{id}/current-month",
                        HttpMethod.GET,
                        null,
                        UserPersonalStats.class,
                        userId
                ).getBody()
        );
    }
}