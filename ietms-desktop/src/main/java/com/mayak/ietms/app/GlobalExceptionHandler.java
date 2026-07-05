package com.mayak.ietms.app;

import com.mayak.ietms.infrastructure.common.SlackErrorReporter;
import com.mayak.ietms.integration.exception.SessionExpiredException;
import com.mayak.ietms.ui.core.SessionManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final SessionManager sessionManager;
    private final SlackErrorReporter slackErrorReporter;

    @PostConstruct
    public void install() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Throwable t = throwable;
            while (t.getCause() != null) t = t.getCause();
            if (t instanceof SessionExpiredException) {
                sessionManager.handleSessionExpired();
                return;
            }

            log.error("Unhandled exception", throwable);

            if (t instanceof Exception e) {
                slackErrorReporter.report(e, "Uncaught exception in thread " + thread.getName());
            } else {
                slackErrorReporter.report(new Exception(t), "Uncaught throwable in thread " + thread.getName());
            }
        });
    }
}
