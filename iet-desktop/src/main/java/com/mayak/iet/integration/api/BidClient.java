package com.mayak.iet.integration.api;

import com.mayak.iet.request.dto.bid.BidCreateDto;
import com.mayak.iet.request.dto.bid.BidViewDto;

import java.util.List;

public interface BidClient {

    List<BidViewDto> findByRequest(long requestId);
    BidViewDto create(BidCreateDto dto);
    void delete(long bidId);

}