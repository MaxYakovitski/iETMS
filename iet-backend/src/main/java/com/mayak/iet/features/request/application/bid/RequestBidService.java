package com.mayak.iet.features.request.application.bid;

import com.mayak.iet.features.bid.domain.model.Bid;
import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.shared.exception.business.NoActiveBidsException;
import com.mayak.iet.features.bid.infra.persistence.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Optional;
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

    public Optional<Bid> findBestBid(Request request) {
        if (request == null) return Optional.empty();

        return getActiveBids(request).stream()
                .min(Comparator.comparing(Bid::getAmount).thenComparing(Bid::getTime));
    }

    public Bid requireBestBid(Request request) {
        return findBestBid(request).orElseThrow(() ->
                        new NoActiveBidsException(request != null ? request.getId() : null));
    }
}