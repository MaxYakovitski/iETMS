package com.mayak.ietms.features.request.application.bid;

import com.mayak.ietms.features.bid.domain.model.Bid;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.bid.infra.persistence.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RequestBidService {
    private final BidRepository bidRepository;

    public Set<Bid> getActiveBids(Request request) {
        if (request == null) return Set.of();
        return bidRepository.findByRequestAndDeletedFalse(request);
    }

    public boolean hasActiveBids(Request request) {
        return !getActiveBids(request).isEmpty();
    }
}