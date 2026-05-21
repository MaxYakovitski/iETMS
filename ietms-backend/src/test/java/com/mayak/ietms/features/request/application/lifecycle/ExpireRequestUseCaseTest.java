package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.enums.ReasonCode;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.RequestStateException;
import com.mayak.ietms.shared.exception.business.UnauthorizedException;
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
@DisplayName("ExpireRequestUseCase")
public class ExpireRequestUseCaseTest {

    @Mock RequestRepository requestRepository;
    @Mock UserQueryService userQueryService;
    @Mock RequestNotificationService requestNotificationService;
    @Mock RequestAccessService accessService;
    @Mock RequestLifecycle lifecycle;

    @InjectMocks ExpireRequestUseCase useCase;

    private User actor;
    private SpotRequest request;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(1L);
        actor.setUserType(UserType.EMPLOYEE);

        request = new SpotRequest();
        request.setId(10L);
        request.setStatus(RequestStatus.NEW);
        request.setAuthorId(1L);

        given(userQueryService.getEntityById(1L)).willReturn(actor);
        given(requestRepository.findById(10L)).willReturn(Optional.of(request));
    }

    @Test
    @DisplayName("execute — аўтар анулюе — адмаўляецца з BID_NOT_PROVIDED")
    void execute_authorExpires_refusesWithBidNotProvided() {
        useCase.execute(10L, 1L);

        then(accessService).should().requireAuthenticated(actor);
        then(lifecycle).should().refuse(request, ReasonCode.BID_NOT_PROVIDED);
        then(requestRepository).should().save(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Test
    @DisplayName("execute — адмін анулюе чужую заяўку — дазволена")
    void execute_adminExpiresOtherRequest_success() {
        actor.setUserType(UserType.ADMIN);
        request.setAuthorId(99L);

        useCase.execute(10L, 1L);
        then(accessService).should().requireAuthenticated(actor);
        then(lifecycle).should().refuse(request, ReasonCode.BID_NOT_PROVIDED);
    }

    @Test
    @DisplayName("execute — не аўтар і не адмін — UnauthorizedException")
    void execute_nonAuthorNonAdmin_throwsException() {
        request.setAuthorId(99L);
        assertThatThrownBy(() -> useCase.execute(10L, 1L)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("execute — фінальны статус — RequestStateException")
    void execute_finalStatus_throwsException() {
        request.setStatus(RequestStatus.ACCEPTED);
        assertThatThrownBy(() -> useCase.execute(10L, 1L)).isInstanceOf(RequestStateException.class);
    }
}