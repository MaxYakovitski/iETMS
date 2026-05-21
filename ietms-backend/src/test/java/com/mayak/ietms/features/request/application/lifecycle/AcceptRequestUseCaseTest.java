package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.bid.domain.model.Bid;
import com.mayak.ietms.features.bid.infra.persistence.BidRepository;
import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.bid.RequestBidService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.shipment.application.notify.ShipmentNotificationService;
import com.mayak.ietms.features.shipment.domain.model.Shipment;
import com.mayak.ietms.features.shipment.infra.persistence.ShipmentRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.RequestStateException;
import com.mayak.ietms.shipment.event.ShipmentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("AcceptRequestUseCase")
public class AcceptRequestUseCaseTest {

    @Mock RequestRepository requestRepository;
    @Mock ShipmentRepository shipmentRepository;
    @Mock BidRepository bidRepository;
    @Mock UserQueryService userQueryService;
    @Mock RequestNotificationService requestNotificationService;
    @Mock ShipmentNotificationService shipmentNotificationService;
    @Mock RequestAccessService accessService;
    @Mock RequestBidService bidService;
    @Spy RequestLifecycle lifecycle = new RequestLifecycle();

    @InjectMocks AcceptRequestUseCase useCase;

    private User actor;
    private SpotRequest request;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(1L);
        request = new SpotRequest();
        request.setId(10L);
        request.setStatus(RequestStatus.OFFERED);
        request.setStartDate(LocalDateTime.now().plusDays(1));
        request.setEndDate(LocalDateTime.now().plusDays(3));

        given(userQueryService.getEntityById(1L)).willReturn(actor);
        given(requestRepository.findById(10L)).willReturn(Optional.of(request));
    }

    @Test
    @DisplayName("execute — няма актыўных ставак — RequestStateException")
    void execute_noActiveBids_throwsException() {
        given(bidService.hasActiveBids(request)).willReturn(false);
        assertThatThrownBy(() -> useCase.execute(10L, null, 1L)).isInstanceOf(RequestStateException.class);
        then(accessService).should().requireAuthenticated(actor);
        then(requestNotificationService).shouldHaveNoInteractions();
        then(shipmentNotificationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("execute — ёсць стаўкі — прымае і стварае адпраўку")
    void execute_withBids_acceptsAndCreatesShipment() {
        User dispatcher = new User();
        dispatcher.setId(5L);

        Bid bestBid = new Bid();
        bestBid.setUser(dispatcher);
        bestBid.setAmount(new BigDecimal("500.00"));

        given(bidService.hasActiveBids(request)).willReturn(true);
        given(bidRepository.findByRequestAndDeletedFalse(request)).willReturn(Set.of(bestBid));
        given(shipmentRepository.findById(10L)).willReturn(Optional.empty());
        given(shipmentRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        useCase.execute(10L, new BigDecimal("700.00"), 1L);

        then(accessService).should().requireAuthenticated(actor);
        then(lifecycle).should().accept(eq(request), eq(bestBid), eq(new BigDecimal("700.00")));
        then(requestRepository).should().save(request);
        then(shipmentRepository).should().save(any(Shipment.class));
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.UPDATED, request);
        then(requestNotificationService).should().publishToUser(eq(5L), eq(RequestEvent.EventType.UPDATED), eq(request));
        then(shipmentNotificationService).should().publishToParticipants(eq(ShipmentEvent.EventType.STATUS_CHANGED), any(Shipment.class));
    }
}