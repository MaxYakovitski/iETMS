package com.mayak.iet.features.extension.notify;

import com.mayak.iet.extension.event.ExtensionDraftInvalidEvent;
import com.mayak.iet.ws.WsDestinations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtensionNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishDraftInvalid(ExtensionDraftInvalidEvent event) {
        if (event == null || event.userId() == null) return;

        messagingTemplate.convertAndSendToUser(
                event.userId().toString(),
                WsDestinations.QUEUE_EXTENSION,
                event
        );

        log.info("WS EXTENSION DraftInvalid sent to userId={}, errors={}",
                event.userId(),
                event.payload().errors().keySet());
    }
}
