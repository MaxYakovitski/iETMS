package com.mayak.ietms.scheduler;

import com.mayak.ietms.features.shipment.application.scheduler.ShipmentSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ShipmentScheduler {

    private final ShipmentSchedulerService schedulerService;

    @Value("${scheduler.shipment.dry-run:false}")
    private boolean dryRun;

    /**
     * Every day at 00:05
     */
    @Scheduled(cron = "0 5 0 * * *")
    @SchedulerLock(
            name = "ShipmentScheduler.processShipmentTransitions",
            lockAtMostFor = "PT15M",
            lockAtLeastFor = "PT1M"
    )
    public void processShipmentTransitions() {
        SchedulerMode mode = dryRun ? SchedulerMode.DRY_RUN : SchedulerMode.EXECUTE;
        log.info("ShipmentScheduler started [mode={}]", mode);

        schedulerService.processShipmentTransitions(mode, LocalDate.now());
        log.info("ShipmentScheduler finished");
    }
}