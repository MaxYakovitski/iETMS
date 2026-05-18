package com.mayak.ietms.features.bid.application;

import com.mayak.ietms.features.user.infra.persistence.UserRepository;
import com.mayak.ietms.request.dto.bid.BidCreateDto;
import com.mayak.ietms.request.dto.bid.BidViewDto;
import com.mayak.ietms.features.bid.domain.model.Bid;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.request.validator.BidContractValidator;
import com.mayak.ietms.shared.exception.business.BidNotFoundException;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
import com.mayak.ietms.shared.exception.business.UnauthorizedException;
import com.mayak.ietms.features.bid.infra.mapping.BidMapper;
import com.mayak.ietms.features.bid.infra.persistence.BidRepository;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.request.application.lifecycle.RequestLifecycleService;
import com.mayak.ietms.shared.exception.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BidCommandService {

    private final BidRepository bidRepository;
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    private final RequestLifecycleService lifecycleService;

    private final BidContractValidator bidContractValidator;

    private final BidMapper bidMapper;

    @Transactional
    public BidViewDto create(BidCreateDto dto, Long userId) {
        validate(dto);

        Request request = requestRepository.findById(dto.requestId())
                .orElseThrow(() -> new RequestNotFoundException(dto.requestId()));

        Bid bid = new Bid();
        bid.setRequest(request);
        bid.setUser(userRepository.getReferenceById(userId));
        bid.setAmount(dto.amount());
        bid.setComment(dto.comment());

        Bid saved = bidRepository.save(bid);
        lifecycleService.onBidsChanged(request.getId());
        return bidMapper.toViewDto(saved);
    }

    private void validate(BidCreateDto dto) {
        var result = bidContractValidator.isValid(dto);
        if (!result.isValid()) throw new ValidationException(result);
    }

    @Transactional
    public void delete(Long bidId, Long userId) {
        Bid bid = bidRepository.findById(bidId).orElseThrow(() -> new BidNotFoundException(bidId));

        if (!bid.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Cannot delete this bid because user is not the owner.");
        }

        bid.setDeleted(true);
        bidRepository.save(bid);
        lifecycleService.onBidsChanged(bid.getRequest().getId());
    }
}