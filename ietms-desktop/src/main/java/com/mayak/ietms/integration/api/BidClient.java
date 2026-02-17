package com.mayak.ietms.integration.api;

import com.mayak.ietms.request.dto.bid.BidCreateDto;
import com.mayak.ietms.request.dto.bid.BidViewDto;

import java.util.List;

public interface BidClient {

    List<BidViewDto> findByRequest(long requestId);
    BidViewDto create(BidCreateDto dto);
    void delete(long bidId);

}