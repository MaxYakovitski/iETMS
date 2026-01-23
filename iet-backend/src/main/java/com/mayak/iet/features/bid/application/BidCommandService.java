package com.mayak.iet.features.bid.application;

import com.mayak.iet.request.dto.bid.BidViewDto;
import com.mayak.iet.features.bid.domain.model.Bid;
import com.mayak.iet.features.request.domain.model.Request;
import com.mayak.iet.features.user.domain.model.User;
import com.mayak.iet.shared.exception.business.BidNotFoundException;
import com.mayak.iet.shared.exception.business.RequestNotFoundException;
import com.mayak.iet.shared.exception.business.UnauthorizedException;
import com.mayak.iet.features.bid.infra.mapping.BidMapper;
import com.mayak.iet.features.bid.infra.persistence.BidRepository;
import com.mayak.iet.features.request.infra.persistence.RequestRepository;
import com.mayak.iet.features.request.application.lifecycle.RequestLifecycleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BidCommandService {

    @PersistenceContext
    private EntityManager entityManager;

    private final BidRepository bidRepository;
    private final RequestRepository requestRepository;
    private final RequestLifecycleService lifecycleService;
    private final BidMapper bidMapper;

    @Transactional
    public BidViewDto create(Long requestInternalId, Long userId, BigDecimal amount, String comment) {
        Request request = requestRepository.findById(requestInternalId)
                .orElseThrow(() -> new RequestNotFoundException(requestInternalId));

        Bid bid = new Bid();
        bid.setRequest(request);
        bid.setUser(entityManager.getReference(User.class, userId));
        bid.setAmount(amount);
        bid.setComment(comment);

        Bid saved = bidRepository.save(bid);
        lifecycleService.onBidsChanged(request.getId());

        return bidMapper.toViewDto(saved);
    }

    @Transactional
    public void delete(Long bidId, Long userId) {
        Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new BidNotFoundException(bidId));

        if (!bid.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Cannot delete this bid because user is not the owner");
        }

        bid.setDeleted(true);
        bidRepository.save(bid);
        lifecycleService.onBidsChanged(bid.getRequest().getId());
    }
}