package com.mayak.ietms.infrastructure.config;

import com.mayak.ietms.infrastructure.notify.SlackAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@RequiredArgsConstructor
public class SchedulerConfig {

    private final SlackAlertService slackAlertService;

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setErrorHandler(slackAlertService::sendSchedulerError);
        return scheduler;
    }

}