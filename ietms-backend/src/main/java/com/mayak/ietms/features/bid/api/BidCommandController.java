package com.mayak.ietms.features.bid.api;

import com.mayak.ietms.request.dto.bid.BidCreateDto;
import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.infrastructure.security.current.CurrentUserId;
import com.mayak.ietms.features.bid.application.BidCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidCommandController {

    private final BidCommandService bidCommandService;

    @PostMapping
    public BidViewDto create(@RequestBody BidCreateDto dto, @CurrentUserId Long userId) {
        return bidCommandService.create(dto, userId);
    }

    @DeleteMapping("/{bidId}")
    public void delete(@PathVariable("bidId") Long bidId, @CurrentUserId Long userId) {
        bidCommandService.delete(bidId, userId);
    }

}