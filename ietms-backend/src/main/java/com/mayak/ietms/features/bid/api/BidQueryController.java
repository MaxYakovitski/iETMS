package com.mayak.ietms.features.bid.api;

import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.features.bid.application.BidQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bids")
@Tag(name = "Bids", description = "Bid management for requests")
@RequiredArgsConstructor
public class BidQueryController {

    private final BidQueryService bidQueryService;

    @GetMapping("/by-request/{requestId}")
    public List<BidViewDto> findByRequest(@PathVariable("requestId") Long requestId) {
        return bidQueryService.findByRequest(requestId);
    }
}