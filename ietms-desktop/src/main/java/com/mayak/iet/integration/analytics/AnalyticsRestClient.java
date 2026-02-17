package com.mayak.iet.integration.analytics;

import com.mayak.iet.infrastructure.connection.BackendConnectionMonitor;
import com.mayak.iet.integration.rest.AbstractRestClient;
import com.mayak.iet.analytics.AnalyticsFilterDto;
import com.mayak.iet.analytics.AnalyticsReportDto;
import com.mayak.iet.company.dto.CompanyDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDate;
import java.util.List;

@Service
public class AnalyticsRestClient extends AbstractRestClient implements AnalyticsClient {

    private static final String API = "/api/analytics";

    public AnalyticsRestClient(RestTemplate restTemplate, BackendConnectionMonitor connectionMonitor) {
        super(restTemplate, connectionMonitor);
    }

    @Override
    public AnalyticsReportDto getAnalytics(AnalyticsFilterDto filter) {
        return exchangeSafely(() ->
                restTemplate.postForObject(
                        API + "/report",
                        filter,
                        AnalyticsReportDto.class
                )
        );
    }

    @Override
    public List<CompanyDto> findCompaniesForDepartmentAnalytics(Long departmentId, LocalDate from, LocalDate to) {
        return exchangeSafely(() -> {

            String url = API + "/companies-for-department" + "?departmentId=" + departmentId + "&from=" + from + "&to=" + to;

            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CompanyDto>>() {}
            ).getBody();
        });
    }
}