package com.mayak.ietms.request.dto.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequestSortField {
    START_DATE("start_date"),
    END_DATE("end_date"),
    ISSUE_DATE("issue_date");

    private final String column;
}