package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.enums.SpotReasonCode;
import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.infra.mapping.RefuseReasonMapper;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
import com.mayak.ietms.shared.exception.business.RequestStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefuseRequestUseCase")
public class RefuseRequestUseCaseTest {

    @Mock RequestRepository requestRepository;
    @Mock RefuseReasonMapper refuseReasonMapper;
    @Mock UserQueryService userQueryService;
    @Mock RequestNotificationService requestNotificationService;
    @Mock RequestAccessService accessService;
    @Mock RequestLifecycle lifecycle;

    @InjectMocks RefuseRequestUseCase useCase;

    private User actor;
    private SpotRequest request;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(1L);
        request = new SpotRequest();
        request.setId(10L);
        request.setStatus(RequestStatus.NEW);

        given(userQueryService.getEntityById(1L)).willReturn(actor);
    }

    @Test
    @DisplayName("execute — карэктная прычына — адмаўляе і апублікоўвае падзею")
    void execute_validReason_refusesAndPublishesEvent() {
        given(requestRepository.findById(10L)).willReturn(Optional.of(request));
        given(refuseReasonMapper.fromCode("REFUSED")).willReturn(SpotReasonCode.BAD_TRANSIT_TIME);
        useCase.execute(10L, "REFUSED", 1L);
        then(accessService).should().requireAuthenticated(actor);
        then(lifecycle).should().refuse(request, SpotReasonCode.BAD_TRANSIT_TIME);
        then(requestRepository).should().save(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Test
    @DisplayName("execute — пусты код прычыны — RequestStateException")
    void execute_blankReasonCode_throwsRequestStateException() {
        assertThatThrownBy(() -> useCase.execute(10L, "  ", 1L))
                .isInstanceOf(RequestStateException.class);
        then(requestNotificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("execute — null код прычыны — RequestStateException")
    void execute_nullReasonCode_throwsRequestStateException() {
        assertThatThrownBy(() -> useCase.execute(10L, null, 1L))
                .isInstanceOf(RequestStateException.class);
        then(requestNotificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("execute — заяўка не знойдзена — RequestNotFoundException")
    void execute_requestNotFound_throwsRequestNotFoundException() {
        given(requestRepository.findById(10L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(10L, "REFUSED", 1L))
                .isInstanceOf(RequestNotFoundException.class);
    }

    @Test
    @DisplayName("execute — фінальны статус — RequestStateException")
    void execute_finalStatus_throwsRequestStateException() {
        request.setStatus(RequestStatus.ACCEPTED);
        given(requestRepository.findById(10L)).willReturn(Optional.of(request));
        assertThatThrownBy(() -> useCase.execute(10L, "REFUSED", 1L))
                .isInstanceOf(RequestStateException.class);
    }

    @Test
    @DisplayName("execute — невядомы код прычыны — RequestStateException")
    void execute_invalidReasonCode_throwsRequestStateException() {
        given(requestRepository.findById(10L)).willReturn(Optional.of(request));
        given(refuseReasonMapper.fromCode("UNKNOWN")).willReturn(null);
        assertThatThrownBy(() -> useCase.execute(10L, "UNKNOWN", 1L))
                .isInstanceOf(RequestStateException.class);
    }

}