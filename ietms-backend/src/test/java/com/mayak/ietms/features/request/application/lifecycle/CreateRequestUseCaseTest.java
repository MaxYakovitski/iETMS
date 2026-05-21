package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.common.validation.ValidationResult;
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
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.location.dto.LocationDto;
import com.mayak.ietms.request.dto.create.BaseRequestDto;
import com.mayak.ietms.request.dto.create.ContractRequestDto;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.request.validator.RequestContractValidator;
import com.mayak.ietms.shared.exception.business.LaneNotFoundException;
import com.mayak.ietms.shared.exception.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateRequestUseCase")
public class CreateRequestUseCaseTest {

    @Mock RequestRepository requestRepository;
    @Mock LaneRepository laneRepository;
    @Mock LocationMapper locationMapper;
    @Mock RequestContractValidator requestContractValidator;
    @Mock RequestFactory requestFactory;
    @Mock UserQueryService userQueryService;
    @Mock RequestNotificationService requestNotificationService;
    @Mock RequestAccessService accessService;
    @Mock CompanyService companyService;
    @Mock RequestLifecycle lifecycle;
    @Mock LocationCommandService locationCommandService;

    @InjectMocks CreateRequestUseCase useCase;

    private User actor;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(1L);
        given(userQueryService.getEntityById(1L)).willReturn(actor);
    }

    @Test
    @DisplayName("execute — карэктная SpotRequest — захоўвае і апублікоўвае падзею CREATED")
    void execute_validSpotRequest_savesAndPublishesEvent() {
        BaseRequestDto dto = new BaseRequestDto();   // fromLocations/toLocations = null → resolveLocationIds = []
        SpotRequest request = new SpotRequest();
        Company company = mock(Company.class);

        given(requestContractValidator.isValid(dto)).willReturn(new ValidationResult());
        given(requestFactory.createRequest(dto)).willReturn(request);
        given(companyService.resolveCompany(null)).willReturn(company);
        given(requestRepository.save(request)).willReturn(request);

        Request result = useCase.execute(dto, 1L);

        then(accessService).should().requireAuthenticated(actor);
        assertThat(result).isEqualTo(request);
        assertThat(request.getAuthorId()).isEqualTo(1L);
        assertThat(request.getStatus()).isEqualTo(RequestStatus.NEW);
        then(requestRepository).should().save(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.CREATED, request);
        then(lifecycle).shouldHaveNoInteractions();  // няма прызначэння lane для SpotRequest
    }

    @Test
    @DisplayName("execute — невалідны DTO — ValidationException, нічога не захоўваецца")
    void execute_invalidDto_throwsValidationException() {
        BaseRequestDto dto = new BaseRequestDto();
        ValidationResult invalid = new ValidationResult();
        invalid.add("startDate", "Start date is required");

        given(requestContractValidator.isValid(dto)).willReturn(invalid);
        assertThatThrownBy(() -> useCase.execute(dto, 1L)).isInstanceOf(ValidationException.class);

        then(requestRepository).shouldHaveNoInteractions();
        then(requestNotificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("execute — ContractRequest — прызначае lane і захоўвае")
    void execute_validContractRequest_assignsLaneAndSaves() {
        ContractRequestDto dto = new ContractRequestDto();
        dto.setLaneId(5L);
        ContractRequest request = new ContractRequest();
        Lane lane = mock(Lane.class);
        Company company = mock(Company.class);

        given(requestContractValidator.isValid(dto)).willReturn(new ValidationResult());
        given(requestFactory.createRequest(dto)).willReturn(request);
        given(companyService.resolveCompany(null)).willReturn(company);
        given(laneRepository.findById(5L)).willReturn(Optional.of(lane));
        given(requestRepository.save(request)).willReturn(request);

        useCase.execute(dto, 1L);

        then(lifecycle).should().assignLane(request, lane);
        then(requestRepository).should().save(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.CREATED, request);
    }

    @Test
    @DisplayName("execute — ContractRequest, lane не знойдзены — LaneNotFoundException")
    void execute_contractRequestLaneNotFound_throwsLaneNotFoundException() {
        ContractRequestDto dto = new ContractRequestDto();
        dto.setLaneId(5L);
        ContractRequest request = new ContractRequest();
        Company company = mock(Company.class);

        given(requestContractValidator.isValid(dto)).willReturn(new ValidationResult());
        given(requestFactory.createRequest(dto)).willReturn(request);
        given(companyService.resolveCompany(null)).willReturn(company);
        given(laneRepository.findById(5L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(dto, 1L)).isInstanceOf(LaneNotFoundException.class);
        then(requestRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("execute — з лакацыямі — резолвіць ID і ўсталёўвае ў request")
    void execute_withLocations_resolvesLocationIds() {
        BaseRequestDto dto = new BaseRequestDto();
        dto.setFromLocations(List.of("DE-10115"));
        dto.setToLocations(List.of("PL-00001"));
        SpotRequest request = new SpotRequest();
        Company company = mock(Company.class);

        Location fromLocation = mock(Location.class);
        Location toLocation = mock(Location.class);
        given(fromLocation.getId()).willReturn(100L);
        given(toLocation.getId()).willReturn(200L);

        given(requestContractValidator.isValid(dto)).willReturn(new ValidationResult());
        given(requestFactory.createRequest(dto)).willReturn(request);
        given(companyService.resolveCompany(null)).willReturn(company);
        given(locationMapper.toEntity(new LocationDto(null, "DE", "10115", null))).willReturn(fromLocation);
        given(locationMapper.toEntity(new LocationDto(null, "PL", "00001", null))).willReturn(toLocation);
        given(locationCommandService.resolve(fromLocation)).willReturn(fromLocation);
        given(locationCommandService.resolve(toLocation)).willReturn(toLocation);
        given(requestRepository.save(request)).willReturn(request);

        useCase.execute(dto, 1L);

        assertThat(request.getFromLocationIds()).containsExactly(100L);
        assertThat(request.getToLocationIds()).containsExactly(200L);
    }

}