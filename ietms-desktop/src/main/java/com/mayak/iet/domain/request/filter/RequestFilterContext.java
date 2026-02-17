package com.mayak.iet.domain.request.filter;

import com.mayak.iet.request.dto.filter.RequestFilterDto;
import lombok.AllArgsConstructor;

import java.util.Collection;

@AllArgsConstructor
public class RequestFilterContext {

    private final RequestFilterDto filter;

    public boolean isActive() {
        if (filter == null) return false;

        return hasCollection(filter.getStatuses())
                || hasCollection(filter.getRequestTypes())
                || hasCollection(filter.getShipmentTypes())
                || hasCollection(filter.getTransportTypes())
                || hasCollection(filter.getAuthorIds())
                || hasCollection(filter.getCompetitorIds())
                || hasCollection(filter.getDispatchersIds())
                || hasText(filter.getCompanyName())
                || hasFrom()
                || hasTo()
                || hasDates()
                || hasWeight()
                || hasLdm()
                || filter.getDangerous() != null;
    }

    private boolean hasFrom() {
        return hasText(filter.getFromCountry())
                || hasText(filter.getFromZipCode())
                || hasText(filter.getFromPlace());
    }

    private boolean hasTo() {
        return hasText(filter.getToCountry())
                || hasText(filter.getToZipCode())
                || hasText(filter.getToPlace());
    }

    private boolean hasDates() {
        return filter.getDatesFilterOption() != null &&
                (filter.getStartDate() != null || filter.getEndDate() != null);}

    private boolean hasWeight() {
        return filter.getMinWeight() != null || filter.getMaxWeight() != null;
    }

    private boolean hasLdm() {
        return filter.getMinLdm() != null || filter.getMaxLdm() != null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private boolean hasCollection(Collection<?> c) {
        return c != null && !c.isEmpty();
    }
}
