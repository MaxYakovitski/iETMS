package com.mayak.ietms.domain.request.filter;

import com.mayak.ietms.request.dto.filter.DatesFilterOption;

public class RequestFilterPolicy {

    public boolean areDatesEnabled(DatesFilterOption option) {
        return option != null;
    }
}