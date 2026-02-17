package com.mayak.ietms.features.request.application;

import com.mayak.ietms.common.dto.page.PageDto;
import com.mayak.ietms.features.user.infra.persistence.ProfileRepository;
import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.request.dto.enums.RequestTypeDto;
import com.mayak.ietms.request.dto.filter.RequestFilterDto;
import com.mayak.ietms.request.dto.view.RequestDetailsDto;
import com.mayak.ietms.request.dto.view.RequestListItemDto;
import com.mayak.ietms.features.bid.domain.model.Bid;
import com.mayak.ietms.features.request.domain.model.ContractRequest;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
import com.mayak.ietms.features.request.application.assembly.RequestDetailsAssembler;
import com.mayak.ietms.features.request.application.assembly.RequestListItemAssembler;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.shipment.infra.persistence.ShipmentRepository;
import com.mayak.ietms.features.location.application.LocationResolver;
import com.mayak.ietms.features.request.application.format.ExchangeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestQueryService {

    private final RequestRepository requestRepository;
    private final ShipmentRepository shipmentRepository;
    private final ProfileRepository profileRepository;
    private final RequestDetailsAssembler detailsAssembler;
    private final RequestListItemAssembler listItemAssembler;
    private final LocationResolver locationResolver;

    public PageDto<RequestListItemDto> findPage(int page, int size, RequestTypeDto type) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Request> entityPage =
                type == null
                        ? requestRepository.findAllActiveSorted(pageable)
                        : requestRepository.findAllByType(resolveType(type), pageable);

        List<RequestListItemDto> content =
                entityPage.getContent().stream()
                        .map(listItemAssembler::toDto)
                        .toList();

        return toPageDto(entityPage, content);
    }

    public PageDto<RequestListItemDto> search(String query, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Request> result = requestRepository.searchByQuery(query, pageable);

        List<RequestListItemDto> content =
                result.getContent().stream()
                        .map(listItemAssembler::toDto)
                        .toList();

        return toPageDto(result, content);
    }

    public PageDto<RequestListItemDto> filter(RequestFilterDto filter, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Request> result = requestRepository.filterByQuery(filter, pageable);

        List<RequestListItemDto> content =
                result.getContent().stream()
                        .map(listItemAssembler::toDto)
                        .toList();

        return toPageDto(result, content);
    }


    public RequestDetailsDto getDetails(long id, User actor) {

        Request request = requestRepository.findFullContractById(id)
                        .map(r -> (Request) r)
                        .orElseGet(() ->
                                requestRepository.findFullSpotById(id)
                                        .map(r -> (Request) r)
                                        .orElseThrow(() -> new RequestNotFoundException(id)));

        Set<Bid> activeBids =
                request.getBids().stream()
                        .filter(b -> !b.isDeleted())
                        .collect(Collectors.toSet());

        return detailsAssembler.toDto(request, actor,  activeBids);
    }

    public String getExchangeString(long id) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RequestNotFoundException(id));

        List<LocationDto> from = locationResolver.resolve(request.getFromLocationIds());
        List<LocationDto> to = locationResolver.resolve(request.getToLocationIds());

        return ExchangeFormatter.format(from, to, request);
    }

    public List<Request> findRequestsForReport(LocalDate from, LocalDate to, Long userId) {
        var fromDt = from.atStartOfDay();
        var toDt = to.atTime(LocalTime.MAX);

        List<RequestStatus> statuses = List.of(
                RequestStatus.ACCEPTED,
                RequestStatus.REFUSED
        );

        Long departmentId = profileRepository
                .findDepartmentIdByUserId(userId)
                .orElse(null);

        if (departmentId == null) {
            return requestRepository.findFullByIssueDateBetweenAndStatusIn(fromDt, toDt, statuses);
        }

        return requestRepository.findFullByDepartmentAndIssueDateBetweenAndStatusIn(departmentId, fromDt, toDt, statuses);

    }

    private Class<? extends Request> resolveType(RequestTypeDto dto) {
        return switch (dto) {
            case SPOT -> SpotRequest.class;
            case CONTRACT -> ContractRequest.class;
        };
    }

    private <D> PageDto<D> toPageDto(Page<?> page, List<D> content) {
        PageDto<D> dto = new PageDto<>();
        dto.setContent(content);
        dto.setPage(page.getNumber());
        dto.setSize(page.getSize());
        dto.setTotalElements(page.getTotalElements());
        dto.setTotalPages(page.getTotalPages());
        dto.setLast(page.isLast());
        return dto;
    }

    public boolean hasShipment(long requestId) {
        return shipmentRepository.existsByRequestId(requestId);
    }
}