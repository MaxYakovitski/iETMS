package com.mayak.ietms.features.bid.application;

import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.features.bid.domain.model.Bid;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
import com.mayak.ietms.features.bid.infra.mapping.BidMapper;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.request.application.bid.RequestBidService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BidQueryService {

    private final RequestRepository requestRepository;
    private final RequestBidService requestBidService;
    private final BidMapper bidMapper;

    @Transactional(readOnly = true)
    public List<BidViewDto> findByRequest(Long requestInternalId) {
        Request request = requestRepository.findById(requestInternalId)
                .orElseThrow(() -> new RequestNotFoundException(requestInternalId));

        return requestBidService.getActiveBids(request).stream()
                .sorted(Comparator.comparing(Bid::getTime).reversed())
                .map(bidMapper::toViewDto)
                .toList();
    }
}