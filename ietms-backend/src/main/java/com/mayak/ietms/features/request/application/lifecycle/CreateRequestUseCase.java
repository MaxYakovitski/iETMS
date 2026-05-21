package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.company.application.CompanyService;
import com.mayak.ietms.features.company.domain.model.Company;
import com.mayak.ietms.features.lane.domain.model.Lane;
import com.mayak.ietms.features.lane.infra.persistence.LaneRepository;
import com.mayak.ietms.features.location.application.LocationCommandService;
import com.mayak.ietms.features.location.domain.model.Location;
import com.mayak.ietms.features.location.infra.mapping.LocationMapper;
import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.factory.RequestFactory;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.ContractRequest;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.location.util.LocationParser;
import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.request.dto.create.ContractRequestDto;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.request.validator.RequestContractValidator;
import com.mayak.ietms.shared.exception.business.LaneNotFoundException;
import com.mayak.ietms.shared.exception.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateRequestUseCase {

    private final RequestRepository requestRepository;
    private final LaneRepository laneRepository;
    private final LocationMapper locationMapper;
    private final RequestContractValidator requestContractValidator;
    private final RequestFactory requestFactory;
    private final UserQueryService userQueryService;
    private final RequestNotificationService requestNotificationService;
    private final RequestAccessService accessService;
    private final CompanyService companyService;
    private final RequestLifecycle lifecycle;
    private final LocationCommandService locationCommandService;

    public Request execute(BaseRequestDto dto, Long userId) {
        User actor = userQueryService.getEntityById(userId);
        accessService.requireAuthenticated(actor);

        var result = requestContractValidator.isValid(dto);
        if (!result.isValid()) throw new ValidationException(result);

        var request = requestFactory.createRequest(dto);
        request.setAuthorId(actor.getId());
        request.setStatus(RequestStatus.NEW);

        Company company = companyService.resolveCompany(dto.getCustomerName());
        request.setCustomer(company);

        List<Long> fromIds = resolveLocationIds(dto.getFromLocations());
        List<Long> toIds   = resolveLocationIds(dto.getToLocations());
        request.setFromLocationIds(fromIds);
        request.setToLocationIds(toIds);

        if (request instanceof ContractRequest contract && dto instanceof ContractRequestDto contractDto) {
            Lane lane = laneRepository.findById(contractDto.getLaneId())
                    .orElseThrow(() -> new LaneNotFoundException(contractDto.getLaneId()));
            lifecycle.assignLane(contract, lane);
        }

        Request saved = requestRepository.save(request);
        requestNotificationService.publishEvent(RequestEvent.EventType.CREATED, saved);
        return saved;
    }

    private List<Long> resolveLocationIds(List<String> rawLocations) {
        if (rawLocations == null || rawLocations.isEmpty()) return List.of();
        return rawLocations.stream()
                .map(LocationParser::parse)
                .filter(Objects::nonNull)
                .map(locationMapper::toEntity)
                .map(locationCommandService::resolve)
                .map(Location::getId)
                .toList();
    }
}