package com.mayak.ietms.features.request.application.lifecycle;

import com.mayak.ietms.features.request.application.RequestStatusService;
import com.mayak.ietms.features.request.application.notify.RequestNotificationService;
import com.mayak.ietms.features.request.domain.model.SpotRequest;
import com.mayak.ietms.features.request.infra.persistence.RequestRepository;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.shared.exception.business.RequestNotFoundException;
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
@DisplayName("OnBidsChangedUseCase")
public class OnBidsChangedUseCaseTest {

    @Mock RequestRepository requestRepository;
    @Mock RequestNotificationService requestNotificationService;
    @Mock RequestStatusService statusService;

    @InjectMocks OnBidsChangedUseCase useCase;

    @Test
    @DisplayName("execute — абнаўляе статус і апублікоўвае падзею")
    void execute_refreshesStatusAndPublishesEvent() {
        SpotRequest request = new SpotRequest();
        request.setId(10L);
        given(requestRepository.findById(10L)).willReturn(Optional.of(request));

        useCase.execute(10L);

        then(statusService).should().refresh(request);
        then(requestNotificationService).should().publishEvent(RequestEvent.EventType.UPDATED, request);
    }

    @Test
    @DisplayName("execute — заяўка не знойдзена — RequestNotFoundException")
    void execute_requestNotFound_throwsException() {
        given(requestRepository.findById(10L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(10L)).isInstanceOf(RequestNotFoundException.class);
    }
}