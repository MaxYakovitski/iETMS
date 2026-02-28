package com.mayak.ietms;

import com.mayak.ietms.infrastructure.config.SlackProperties;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
@EnableConfigurationProperties(SlackProperties.class)
public class IetmsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(IetmsBackendApplication.class, args);
    }
}