package com.mayak.ietms.features.bid.api;

import com.mayak.ietms.request.dto.bid.BidCreateDto;
import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.infrastructure.security.current.CurrentUserId;
import com.mayak.ietms.features.bid.application.BidCommandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bids")
@Tag(name = "Bids", description = "Bid management for spot requests")
@RequiredArgsConstructor
public class BidCommandController {

    private final BidCommandService bidCommandService;

    @PostMapping
    public BidViewDto create(@RequestBody BidCreateDto dto, @CurrentUserId Long userId) {
        return bidCommandService.create(dto, userId);
    }

    @DeleteMapping("/{bidId}")
    @Operation(summary = "Delete bid", description = "Only the bid owner can delete their own bid.")
    public void delete(@PathVariable("bidId") Long bidId, @CurrentUserId Long userId) {
        bidCommandService.delete(bidId, userId);
    }

}