package com.mayak.ietms.ui.analytics.model;

import com.mayak.ietms.user.dto.UserLookupDto;

import java.util.List;

public record UserReport(
        UserLookupDto user,
        List<UserReportItem> items) {

}