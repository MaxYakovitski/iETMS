package com.mayak.ietms.features.request.application.notify;

import com.mayak.ietms.request.dto.event.RequestEventDto;
import com.mayak.ietms.request.event.RequestEvent;
import com.mayak.ietms.features.request.domain.model.Request;
import com.mayak.ietms.features.request.infra.mapping.RequestEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static com.mayak.ietms.ws.WsDestinations.TOPIC_REQUESTS;
import static com.mayak.ietms.ws.WsDestinations.QUEUE_REQUESTS;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestNotificationService {


    private final SimpMessagingTemplate messagingTemplate;
    private final RequestEventMapper eventMapper;

    public void publishEvent(RequestEvent.EventType type, Request request) {
        if (request == null || type == null) return;

        try {
            var dto = eventMapper.toEventDto(request);

            RequestEvent<RequestEventDto> event = new RequestEvent <>(
                    request.getId(),
                    type,
                    request.getVersion(),
                    dto
            );
            messagingTemplate.convertAndSend(TOPIC_REQUESTS, event);
            log.info("WS request event {} sent for request {}", type, request.getId());
        } catch (Exception e) {
            log.warn("WS publish failed for request {}: {}",
                    request.getId(), e.toString());
        }
    }

    public void publishToUser(
            Long userId,
            RequestEvent.EventType type,
            Request request
    ) {
        if (userId == null || request == null) return;

        RequestEvent<RequestEventDto> event =
                new RequestEvent<>(request.getId(), type, request.getVersion(), eventMapper.toEventDto(request));

        messagingTemplate.convertAndSendToUser(String.valueOf(userId), QUEUE_REQUESTS, event);
        log.info("WS USER event {} sent to userId={}", type, userId);
    }
}