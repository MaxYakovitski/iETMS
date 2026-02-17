package com.mayak.iet.domain.request.filter;

import com.mayak.iet.request.dto.filter.DatesFilterOption;

public class RequestFilterPolicy {

    public boolean areDatesEnabled(DatesFilterOption option) {
        return option != null;
    }
}