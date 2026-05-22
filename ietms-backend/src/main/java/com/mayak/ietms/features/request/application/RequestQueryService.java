package com.mayak.ietms.features.request.application;

import com.mayak.ietms.common.dto.page.PageDto;
import com.mayak.ietms.features.company.domain.model.Company;
import com.mayak.ietms.features.company.infra.persistence.CompanyRepository;
import com.mayak.ietms.features.request.application.access.RequestVisibilityScope;
import com.mayak.ietms.features.request.application.access.RequestVisibilityScopeResolver;
import com.mayak.ietms.features.user.application.UserLookupService;
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
import com.mayak.ietms.user.dto.UserNameDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final RequestRepository requestRepository;
    private final ShipmentRepository shipmentRepository;
    private final CompanyRepository companyRepository;
    private final UserLookupService userLookupService;
    private final ProfileRepository profileRepository;

    private final RequestDetailsAssembler detailsAssembler;
    private final RequestListItemAssembler listItemAssembler;

    private final LocationResolver locationResolver;
    private final RequestVisibilityScopeResolver scopeResolver;

    public PageDto<RequestListItemDto> findPage(Long userId, int page, int size, RequestTypeDto type) {
        RequestVisibilityScope scope = scopeResolver.resolve(userId);
        Pageable pageable = toPageable(page, size);
        Page<Request> result;
        if (scope.isRestricted()) {
            result = type == null
                    ? requestRepository.findAllActiveSortedByDepartment(scope.departmentId(), pageable)
                    : requestRepository.findAllByTypeAndDepartment(resolveType(type), scope.departmentId(), pageable);
        } else {
            result = type == null
                    ? requestRepository.findAllActiveSorted(pageable)
                    : requestRepository.findAllByType(resolveType(type), pageable);
        }
        List<RequestListItemDto> content = assembleListItems(result.getContent());
        return toPageDto(result, content);
    }

    public PageDto<RequestListItemDto> search(Long userId, String query, int page, int size,  RequestTypeDto type) {
        RequestVisibilityScope scope = scopeResolver.resolve(userId);
        Pageable pageable = toPageable(page, size);
        Page<Request> result = requestRepository.searchByQuery(query, type, scope.departmentId(), pageable);
        List<RequestListItemDto> content = assembleListItems(result.getContent());
        return toPageDto(result, content);
    }

    public PageDto<RequestListItemDto> filter(Long userId, RequestFilterDto filter, int page, int size) {
        RequestVisibilityScope scope = scopeResolver.resolve(userId);
        Pageable pageable = toPageable(page, size);
        Page<Request> result = requestRepository.filterByQuery(filter, scope.departmentId(), pageable);
        List<RequestListItemDto> content = assembleListItems(result.getContent());
        return toPageDto(result, content);
    }


    public RequestDetailsDto getDetails(long id, User actor) {
        Request request = requestRepository.findFullContractById(id)
                        .map(r -> (Request) r)
                        .orElseGet(() ->
                                requestRepository.findFullSpotById(id)
                                        .map(r -> (Request) r)
                                        .orElseThrow(() -> new RequestNotFoundException(id)));

        Set<Bid> activeBids = request.getBids().stream().filter(b -> !b.isDeleted()).collect(Collectors.toSet());
        return detailsAssembler.toDto(request, actor,  activeBids);
    }

    public String getExchangeString(long id) {
        Request request = requestRepository.findById(id).orElseThrow(() -> new RequestNotFoundException(id));
        List<LocationDto> from = locationResolver.resolve(request.getFromLocationIds());
        List<LocationDto> to = locationResolver.resolve(request.getToLocationIds());
        return ExchangeFormatter.format(from, to, request);
    }

    public List<Request> findRequestsForReport(LocalDate from, LocalDate to, Long userId) {
        var fromInstant = from.atStartOfDay().toInstant(ZoneOffset.UTC);
        var toInstant = to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<RequestStatus> statuses = List.of(RequestStatus.ACCEPTED, RequestStatus.REFUSED);
        Long departmentId = profileRepository.findDepartmentIdByUserId(userId).orElse(null);
        if (departmentId == null) {
            return requestRepository.findFullByIssueDateBetweenAndStatusIn(fromInstant, toInstant, statuses);
        }
        return requestRepository.findFullByDepartmentAndIssueDateBetweenAndStatusIn(departmentId, fromInstant, toInstant, statuses);
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

    private Pageable toPageable(int page, int size) {
        return PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE));
    }

    public boolean hasShipment(long requestId) {
        return shipmentRepository.existsByRequestId(requestId);
    }

    private List<RequestListItemDto> assembleListItems(List<Request> requests) {
        Set<Long> companyIds = requests.stream()
                .map(r -> r.getCustomer() != null ? r.getCustomer().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> userIds = requests.stream()
                .flatMap(r -> Stream.of(r.getAuthorId(), r.getDispatcherId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> companyNames = companyRepository.findAllById(companyIds).stream()
                .collect(Collectors.toMap(Company::getId, Company::getName));

        Map<Long, UserNameDto> userNames = userLookupService.getNames(userIds);
        return requests.stream()
                .map(r -> listItemAssembler.toDto(r, companyNames, userNames))
                .toList();
    }
}