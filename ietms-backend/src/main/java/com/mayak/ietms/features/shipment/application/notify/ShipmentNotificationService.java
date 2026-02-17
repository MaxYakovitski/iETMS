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

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ShipmentEventMapper eventMapper;

    public void publishEvent(ShipmentEvent.EventType type, Shipment shipment) {
        if (shipment == null || type == null) return;

        try {
            var dto = eventMapper.toEventDto(shipment);

            ShipmentEvent<ShipmentEventDto> event =
                    new ShipmentEvent<>(shipment.getId(), type, shipment.getVersion(), dto);

            messagingTemplate.convertAndSend(
                    WsDestinations.TOPIC_SHIPMENTS,
                    event
            );

            log.info("WS shipment event {} sent for shipment {}",
                    type, shipment.getId());

        } catch (Exception e) {
            log.warn("WS shipment publish failed for shipment {}: {}",
                    shipment.getId(), e.toString());
        }
    }

    public void publishToDispatcher(
            ShipmentEvent.EventType type,
            Shipment shipment
    ) {
        Long dispatcherId = shipment.getDispatcherId();
        if (dispatcherId == null) return;

        ShipmentEvent<ShipmentEventDto> event =
                new ShipmentEvent<>(
                        shipment.getId(),
                        type,
                        shipment.getVersion(),
                        eventMapper.toEventDto(shipment)
                );

        messagingTemplate.convertAndSendToUser(
                String.valueOf(dispatcherId),
                WsDestinations.QUEUE_SHIPMENTS,
                event
        );

        log.info("WS shipment USER event {} sent to dispatcherId={}",
                type, dispatcherId);
    }
}
