package com.mayak.ietms.features.shipment.application.notify;

import com.mayak.ietms.request.dto.event.ShipmentEventDto;
import com.mayak.ietms.features.shipment.domain.model.Shipment;
import com.mayak.ietms.shipment.event.ShipmentEvent;
import com.mayak.ietms.features.shipment.infra.mapping.ShipmentEventMapper;
import com.mayak.ietms.ws.WsDestinations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ShipmentEventMapper eventMapper;

    public void publishToParticipants(ShipmentEvent.EventType type, Shipment shipment) {
        if (shipment == null || type == null) return;

        try {
            ShipmentEvent<ShipmentEventDto> event = new ShipmentEvent<>(
                    shipment.getId(), type, shipment.getVersion(),
                    eventMapper.toEventDto(shipment)
            );

            Set<Long> recipients = shipment.collectParticipantIds();

            for (Long userId : recipients) {
                messagingTemplate.convertAndSendToUser(
                        String.valueOf(userId),
                        WsDestinations.QUEUE_SHIPMENTS,
                        event
                );
            }

            log.info("WS shipment event {} sent to {} participants for shipment {}",
                    type, recipients.size(), shipment.getId());

        } catch (Exception e) {
            log.warn("WS shipment publish failed for shipment {}: {}",
                    shipment.getId(), e.toString());
        }
    }
}