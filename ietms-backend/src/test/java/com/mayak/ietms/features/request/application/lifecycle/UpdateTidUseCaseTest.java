package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.shipment.application.notify.ShipmentNotificationService;
import com.mayak.ietms.features.shipment.domain.model.Shipment;
import com.mayak.ietms.features.shipment.infra.persistence.ShipmentRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.UnauthorizedException;
import com.mayak.ietms.shipment.event.ShipmentEvent;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateTidUseCase")
public class UpdateTidUseCaseTest {

    @Mock RequestRepository requestRepository;
    @Mock ShipmentRepository shipmentRepository;
    @Mock UserQueryService userQueryService;
    @Mock RequestNotificationService requestNotificationService;
    @Mock ShipmentNotificationService shipmentNotificationService;

    @InjectMocks UpdateTidUseCase useCase;

    private SpotRequest request;

    @BeforeEach
    void setUp() {
        User actor = new User();
        actor.setId(1L);
        request = new SpotRequest();
        request.setId(10L);
        request.setAuthorId(1L);

        given(userQueryService.getEntityById(1L)).willReturn(actor);
        given(requestRepository.findById(10L)).willReturn(Optional.of(request));
    }

    @Test
    @DisplayName("execute — новы TID — захоўвае і апублікоўвае падзею")
    void execute_newTid_savesAndPublishesEvent() {
        given(shipmentRepository.findById(10L)).willReturn(Optional.empty());
        useCase.execute(10L, "NEW-TID-001", 1L);
        then(requestRepository).should().save(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.UPDATED, request);
        then(shipmentNotificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("execute — TID не змяніўся — нічога не робіць")
    void execute_sameTid_doesNothing() {
        request.setTid("SAME-TID");
        useCase.execute(10L, "SAME-TID", 1L);
        then(requestRepository).should(never()).save(request);
        then(requestNotificationService).should(never()).publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Test
    @DisplayName("execute — пусты TID без адпраўкі — захоўвае як null, апавяшчае толькі request")
    void execute_blankTid_noShipment_storesNull() {
        request.setTid("OLD-TID");
        given(shipmentRepository.findById(10L)).willReturn(Optional.empty());
        useCase.execute(10L, "   ", 1L);
        then(requestRepository).should().save(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.UPDATED, request);
        then(shipmentNotificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("execute — пусты TID з адпраўкай — захоўвае як null, апавяшчае request, shipment")
    void execute_blankTid_withShipment_storesNull() {
        request.setTid("OLD-TID");
        Shipment shipment = mock(Shipment.class);
        given(shipmentRepository.findById(10L)).willReturn(Optional.of(shipment));
        useCase.execute(10L, "   ", 1L);
        then(requestRepository).should().save(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.UPDATED, request);
        then(shipmentNotificationService).should().publishToParticipants(ShipmentEvent.EventType.UPDATED, shipment);
    }

    @Test
    @DisplayName("execute — не аўтар — UnauthorizedException")
    void execute_nonAuthor_throwsException() {
        request.setAuthorId(99L);
        assertThatThrownBy(() -> useCase.execute(10L, "NEW-TID", 1L)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("execute — адпраўка існуе — апавяшчае ўдзельнікаў адпраўкі")
    void execute_newTidWithExistingShipment_notifiesShipmentParticipants() {
        Shipment shipment = mock(Shipment.class);
        given(shipmentRepository.findById(10L)).willReturn(Optional.of(shipment));
        useCase.execute(10L, "NEW-TID", 1L);
        then(requestRepository).should().save(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.UPDATED, request);
        then(shipmentNotificationService).should().publishToParticipants(ShipmentEvent.EventType.UPDATED, shipment);
    }
}