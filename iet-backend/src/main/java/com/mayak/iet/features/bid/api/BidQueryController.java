package com.mayak.iet.features.bid.api;

import com.mayak.iet.request.dto.bid.BidViewDto;
import com.mayak.iet.features.bid.application.BidQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidQueryController {

    private final BidQueryService bidQueryService;

    @GetMapping("/by-request/{requestId}")
    public List<BidViewDto> findByRequest(@PathVariable Long requestId) {
        return bidQueryService.findByRequest(requestId);
    }
}
