package com.mayak.ietms.integration.api;

import com.mayak.ietms.common.dto.page.PageDto;
import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.filter.RequestFilterDto;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.request.dto.view.RequestListItemDto;

import java.math.BigDecimal;

public interface RequestClient {
    PageDto<RequestListItemDto> findPage(int page, int size, RequestTypeDto type);
    PageDto<RequestListItemDto> search(String query, int page, int size, RequestTypeDto type);
    PageDto<RequestListItemDto> filter(RequestFilterDto filter, int page, int size);

    RequestDetailsDto getDetails(long requestId);

    void join(long requestId);
    void leave(long requestId);

    void offer(long requestId);
    void accept(long requestId);
    void accept(long requestId, BigDecimal price);
    void refuse(long requestId, String reasonCode);

    String getExchangeString(long requestId);
    void updateTid(long requestId, String tid);

    RequestDetailsDto create(BaseRequestDto dto);
    void delete(long requestId);

    /**
     * Manually expires the request by refusing it with reason {@code BID_NOT_PROVIDED}.
     */
    void expire(long requestId);
}