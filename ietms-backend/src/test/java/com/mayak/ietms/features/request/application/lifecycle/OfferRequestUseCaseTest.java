package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.access.RequestAccessService;
import com.mayak.ietms.features.request.application.bid.RequestBidService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.enums.RequestStatus;
import com.mayak.ietms.features.request.domain.lifecycle.RequestLifecycle;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.features.user.application.UserQueryService;
import com.mayak.ietms.features.user.domain.model.User;
import com.mayak.ietms.request.event.RequestEvent;
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
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("OfferRequestUseCase")
public class OfferRequestUseCaseTest {

    @Mock RequestRepository requestRepository;
    @Mock UserQueryService userQueryService;
    @Mock RequestNotificationService requestNotificationService;
    @Mock RequestAccessService accessService;
    @Mock RequestBidService bidService;
    @Mock RequestLifecycle lifecycle;

    @InjectMocks OfferRequestUseCase useCase;

    private User actor;
    private SpotRequest request;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(1L);
        request = new SpotRequest();
        request.setId(10L);

        given(userQueryService.getEntityById(1L)).willReturn(actor);
        given(requestRepository.findById(10L)).willReturn(Optional.of(request));
    }

    @Test
    @DisplayName("execute — ёсць стаўкі — усталёўвае OFFERED і апублікоўвае падзею")
    void execute_withActiveBids_setsOfferedAndPublishesEvent() {
        request.setStatus(RequestStatus.BIDDING);
        given(bidService.hasActiveBids(request)).willReturn(true);

        useCase.execute(10L, 1L);

        then(accessService).should().requireAuthenticated(actor);
        then(lifecycle).should().offer(request);
        then(requestRepository).should().save(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Test
    @DisplayName("execute — статус ужо OFFERED — нічога не робіць")
    void execute_alreadyOffered_doesNothing() {
        request.setStatus(RequestStatus.OFFERED);
        given(bidService.hasActiveBids(request)).willReturn(true);

        useCase.execute(10L, 1L);

        then(lifecycle).should(never()).offer(request);
        then(requestRepository).should(never()).save(request);
    }

    @Test
    @DisplayName("execute — фінальны статус — RequestStateException")
    void execute_finalStatus_throwsException() {
        request.setStatus(RequestStatus.ACCEPTED);
        assertThatThrownBy(() -> useCase.execute(10L, 1L)).isInstanceOf(RequestStateException.class);
    }

    @Test
    @DisplayName("execute — няма актыўных ставак — RequestStateException")
    void execute_noActiveBids_throwsException() {
        request.setStatus(RequestStatus.IN_PROGRESS);
        given(bidService.hasActiveBids(request)).willReturn(false);
        assertThatThrownBy(() -> useCase.execute(10L, 1L)).isInstanceOf(RequestStateException.class);
    }
}