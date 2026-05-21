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
import com.mayak.ietms.shared.exception.business.AlreadyJoinedException;
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
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("JoinRequestUseCase")
public class JoinRequestUseCaseTest {

    @Mock RequestRepository requestRepository;
    @Mock UserQueryService userQueryService;
    @Mock RequestNotificationService requestNotificationService;
    @Mock RequestAccessService accessService;
    @Mock RequestStatusService statusService;

    @InjectMocks JoinRequestUseCase useCase;

    private User actor;
    private SpotRequest request;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(1L);

        request = new SpotRequest();
        request.setId(10L);
        request.setStatus(RequestStatus.NEW);
        request.setAuthorId(99L);

        given(userQueryService.getEntityById(1L)).willReturn(actor);
        given(requestRepository.findById(10L)).willReturn(Optional.of(request));
    }

    @Test
    @DisplayName("execute — карыстальнік далучаецца — дадае ў канкурэнтаў і апублікоўвае падзею")
    void execute_happyPath_addsCompetitorAndPublishesEvent() {
        given(accessService.isJoined(actor, request)).willReturn(false);

        useCase.execute(10L, 1L);

        then(accessService).should().requireAuthenticated(actor);
        then(accessService).should().requireCanJoin(actor, request);
        then(requestRepository).should().save(request);
        then(statusService).should().refresh(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Test
    @DisplayName("execute — карыстальнік ужо далучыўся — AlreadyJoinedException")
    void execute_alreadyJoined_throwsAlreadyJoinedException() {
        given(accessService.isJoined(actor, request)).willReturn(true);
        assertThatThrownBy(() -> useCase.execute(10L, 1L)).isInstanceOf(AlreadyJoinedException.class);
        then(requestRepository).should(never()).save(any());
        then(statusService).should(never()).refresh(any());
        then(requestNotificationService).should(never()).publishEvent(any(), any());
    }

    @Test
    @DisplayName("execute — заяўка не знойдзена — RequestNotFoundException")
    void execute_requestNotFound_throwsException() {
        given(requestRepository.findById(10L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(10L, 1L)).isInstanceOf(RequestNotFoundException.class);
    }
}