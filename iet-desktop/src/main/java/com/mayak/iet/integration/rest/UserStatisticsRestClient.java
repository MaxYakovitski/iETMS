package com.mayak.iet.integration.rest;

import com.mayak.iet.integration.api.UserStatisticsClient;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.mayak.iet.statistics.UserPersonalStats;

@Service
public class UserStatisticsRestClient extends AbstractRestClient implements UserStatisticsClient {

    private static final String BASE_URL = "http://localhost:8080/api/statistics/users";

    public UserStatisticsRestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public UserPersonalStats getCurrentMonthStats(Long userId) {
        return exchangeSafely(() ->
                restTemplate.exchange(
                        BASE_URL + "/{id}/current-month",
                        HttpMethod.GET,
                        null,
                        UserPersonalStats.class,
                        userId
                ).getBody()
        );
    }
}