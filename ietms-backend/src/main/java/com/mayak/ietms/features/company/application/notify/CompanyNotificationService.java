package com.mayak.ietms.features.company.application.notify;

import com.mayak.ietms.company.dto.CompanyDto;
import com.mayak.ietms.company.event.CompanyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import static com.mayak.ietms.ws.WsDestinations.TOPIC_COMPANIES;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishCreated(CompanyDto dto) {
        publish(CompanyEvent.EventType.CREATED, dto);
    }

    public void publishUpdated(CompanyDto dto) {
        publish(CompanyEvent.EventType.UPDATED, dto);
    }

    public void publishDeleted(Long companyId) {
        publish(CompanyEvent.EventType.DELETED, new CompanyDto(companyId, null));
    }

    private void publish(CompanyEvent.EventType type, CompanyDto dto) {
        try {
            CompanyEvent<CompanyDto> event = new CompanyEvent<>(dto.id(), type, 0L, dto);
            messagingTemplate.convertAndSend(TOPIC_COMPANIES, event);
            log.info("WS company event {} sent for id={}", type, dto.id());
        } catch (Exception e) {
            log.warn("WS company publish failed: {}", e.toString());
        }
    }
}