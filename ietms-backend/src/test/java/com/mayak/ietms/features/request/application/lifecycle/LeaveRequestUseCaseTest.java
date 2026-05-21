package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.RequestStatusService;
import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
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
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("LeaveRequestUseCase")
public class LeaveRequestUseCaseTest {

    @Mock RequestRepository requestRepository;
    @Mock UserQueryService userQueryService;
    @Mock RequestNotificationService requestNotificationService;
    @Mock RequestAccessService accessService;
    @Mock RequestStatusService statusService;

    @InjectMocks LeaveRequestUseCase useCase;

    private User actor;
    private SpotRequest request;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(1L);
        request = new SpotRequest();
        request.setId(10L);
        request.setStatus(RequestStatus.IN_PROGRESS);

        given(userQueryService.getEntityById(1L)).willReturn(actor);
        given(requestRepository.findById(10L)).willReturn(Optional.of(request));
    }

    @Test
    @DisplayName("execute — карыстальнік пакідае — выдаляе з канкурэнтаў і апублікоўвае падзею")
    void execute_happyPath_removesCompetitorAndPublishesEvent() {
        given(accessService.isJoined(actor, request)).willReturn(true);

        useCase.execute(10L, 1L);

        then(accessService).should().requireAuthenticated(actor);
        then(requestRepository).should().save(request);
        then(statusService).should().refresh(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Test
    @DisplayName("execute — карыстальнік не далучаўся — нічога не робіць")
    void execute_notJoined_doesNothing() {
        given(accessService.isJoined(actor, request)).willReturn(false);

        useCase.execute(10L, 1L);

        then(accessService).should().requireAuthenticated(actor);
        then(requestRepository).should(never()).save(request);
        then(statusService).should(never()).refresh(request);
    }

    @Test
    @DisplayName("execute — заяўка не знойдзена — RequestNotFoundException")
    void execute_requestNotFound_throwsException() {
        given(requestRepository.findById(10L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(10L, 1L)).isInstanceOf(RequestNotFoundException.class);
    }
}