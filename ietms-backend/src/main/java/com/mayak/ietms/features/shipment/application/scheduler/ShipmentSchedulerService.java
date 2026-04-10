package com.mayak.ietms.features.shipment.application.scheduler;

import com.mayak.ietms.features.shipment.domain.enums.ShipmentStatus;
import com.mayak.ietms.features.shipment.domain.model.Shipment;
import com.mayak.ietms.features.shipment.infra.persistence.ShipmentRepository;
import com.mayak.ietms.scheduler.SchedulerMode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentSchedulerService {
    private final ShipmentRepository shipmentRepository;

    @Transactional
    public void processShipmentTransitions(SchedulerMode mode, LocalDate today) {
        promoteToLoad(mode, today);
        promoteToDrop(mode, today);
    }

    private void promoteToLoad(SchedulerMode mode, LocalDate today) {
        List<Shipment> shipments = shipmentRepository
                .findByStatusAndPlannedLoadDateLessThanEqual(ShipmentStatus.PLANNED, today);

        for (Shipment s : shipments) {
            if (mode == SchedulerMode.DRY_RUN) {
                log.info("[DRY-RUN] Would promote shipment {} to TO_LOAD", s.getId());
            } else {
                s.markToLoadBySystem();
                shipmentRepository.save(s);
            }
        }

        log.info("promoteToLoad: processed {} shipments (mode={})", shipments.size(), mode);
    }

    private void promoteToDrop(SchedulerMode mode, LocalDate today) {
        List<Shipment> shipments = shipmentRepository
                .findByStatusAndPlannedDropDateLessThanEqual(ShipmentStatus.LOADED, today);

        for (Shipment s : shipments) {
            if (mode == SchedulerMode.DRY_RUN) {
                log.info("[DRY-RUN] Would promote shipment {} to TO_DROP", s.getId());
            } else {
                s.markToDropBySystem();
                shipmentRepository.save(s);
            }
        }

        log.info("promoteToDrop: processed {} shipments (mode={})", shipments.size(), mode);
    }
}
