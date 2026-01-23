package com.mayak.iet.scheduler;

import com.mayak.iet.features.request.application.lifecycle.RequestLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestScheduler {

    private final RequestLifecycleService lifecycleService;

    /**
     * Allows running scheduler in "dry-run" mode
     * (no state mutation, logging only).
     */
    @Value("${scheduler.request.dry-run:false}")
    private boolean dryRun;

    /**
     * Every day at 02:00
     */
    @Scheduled(cron = "0 0 2 * * *")
    @SchedulerLock(
            name = "RequestScheduler.processExpiredRequests",
            lockAtMostFor = "PT30M",
            lockAtLeastFor = "PT1M"
    )
    public void processExpiredRequests() {
        SchedulerMode mode = dryRun ? SchedulerMode.DRY_RUN : SchedulerMode.EXECUTE;
        log.info("RequestScheduler started [mode={}]", mode);

        lifecycleService.processExpiredRequests(mode);
        log.info("RequestScheduler finished");
    }
}