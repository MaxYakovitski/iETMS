package com.mayak.iet.ui.analytics.model;

import com.mayak.iet.user.dto.UserLookupDto;

import java.util.List;

public record UserReport(
        UserLookupDto user,
        List<UserReportItem> items) {

}