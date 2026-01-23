package com.mayak.iet.integration.analytics;

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
import java.util.StringJoiner;

@Service
public class AnalyticsRestClient extends AbstractRestClient implements AnalyticsClient {

    private static final String BASE_URL = "http://localhost:8080/api/analytics";

    public AnalyticsRestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public AnalyticsReportDto getAnalytics(AnalyticsFilterDto filter) {
        return exchangeSafely(() ->
                restTemplate.postForObject(
                        BASE_URL + "/report",
                        filter,
                        AnalyticsReportDto.class
                )
        );
    }

    private String buildUrl(LocalDate start, LocalDate end, List<Long> companyIds, List<Long> userIds) {
        StringJoiner joiner = new StringJoiner("&", BASE_URL + "?", "");

        joiner.add("start=" + start);
        joiner.add("end=" + end);

        if (companyIds != null) {
            for (Long id : companyIds) {
                joiner.add("companyIds=" + id);
            }
        }

        if (userIds != null) {
            for (Long id : userIds) {
                joiner.add("userIds=" + id);
            }
        }

        return joiner.toString();
    }

    @Override
    public List<CompanyDto> findCompaniesForDepartmentAnalytics(
            Long departmentId,
            LocalDate from,
            LocalDate to
    ) {
        return exchangeSafely(() -> {

            String url = BASE_URL + "/companies-for-department"
                    + "?departmentId=" + departmentId
                    + "&from=" + from
                    + "&to=" + to;

            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CompanyDto>>() {}
            ).getBody();
        });
    }
}