package com.mayak.ietms.support.state;

import com.mayak.ietms.request.dto.filter.RequestFilterDto;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RequestFilterState {

    private RequestFilterDto lastFilter;
    public Optional<RequestFilterDto> get() {
        return Optional.ofNullable(lastFilter);
    }
    public void set(RequestFilterDto filter) {
        this.lastFilter = filter;
    }
    public void clear() {
        this.lastFilter = null;
    }
}