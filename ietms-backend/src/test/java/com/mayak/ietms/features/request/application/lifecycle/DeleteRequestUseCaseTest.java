package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.shipment.infra.persistence.ShipmentRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.enums.UserType;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.RequestDeletionNotAllowedException;
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
@DisplayName("DeleteRequestUseCase")
public class DeleteRequestUseCaseTest {

    @Mock RequestRepository requestRepository;
    @Mock ShipmentRepository shipmentRepository;
    @Mock UserQueryService userQueryService;
    @Mock RequestNotificationService requestNotificationService;
    @Mock RequestAccessService accessService;

    @InjectMocks DeleteRequestUseCase useCase;

    private User actor;
    private SpotRequest request;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(1L);
        actor.setUserType(UserType.EMPLOYEE);

        request = new SpotRequest();
        request.setId(10L);
        request.setAuthorId(1L);

        given(userQueryService.getEntityById(1L)).willReturn(actor);
        given(requestRepository.findById(10L)).willReturn(Optional.of(request));
    }

    @Test
    @DisplayName("execute — аўтар выдаляе — выдаляецца і апублікоўваецца падзея DELETED")
    void execute_authorDeletes_success() {
        given(shipmentRepository.existsByRequestId(10L)).willReturn(false);
        useCase.execute(10L, 1L);
        then(accessService).should().requireAuthenticated(actor);
        then(requestRepository).should().delete(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.DELETED, request);
    }

    @Test
    @DisplayName("execute — адмін выдаляе чужую заяўку — дазволена")
    void execute_adminDeletesOtherRequest_success() {
        given(shipmentRepository.existsByRequestId(10L)).willReturn(false);
        actor.setUserType(UserType.ADMIN);
        request.setAuthorId(99L);  // іншы аўтар
        useCase.execute(10L, 1L);
        then(accessService).should().requireAuthenticated(actor);
        then(requestRepository).should().delete(request);
    }

    @Test
    @DisplayName("execute — не аўтар і не адмін — UnauthorizedException")
    void execute_nonAuthorNonAdmin_throwsException() {
        request.setAuthorId(99L);
        assertThatThrownBy(() -> useCase.execute(10L, 1L)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("execute — адпраўка існуе — RequestDeletionNotAllowedException")
    void execute_shipmentExists_throwsException() {
        given(shipmentRepository.existsByRequestId(10L)).willReturn(true);
        assertThatThrownBy(() -> useCase.execute(10L, 1L)).isInstanceOf(RequestDeletionNotAllowedException.class);
    }

}