package com.mayak.ietms.infrastructure.notify;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlackAlertService {

    private final SlackNotifier notifier;
    private final Environment environment;

    public void sendHttpError(Exception ex, HttpServletRequest request) {
        notifier.sendError("""
                🚨 *Backend error (%s)*

                Host: %s
                URI: %s %s

                Type: %s
                Message: %s

                Stack:
                %s
                """.formatted(
                getProfile(),
                getHostname(),
                request.getMethod(),
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                formatStack(ex)
        ));
    }

    public void sendSchedulerError(Throwable ex) {
        notifier.sendError("""
                🕐 *Scheduler error (%s)*

                Host: %s

                Type: %s
                Message: %s

                Stack:
                %s
                """.formatted(
                getProfile(),
                getHostname(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                formatStack(ex)
        ));
    }

    private String getProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .findFirst()
                .orElse("unknown")
                .toUpperCase();
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    private String formatStack(Throwable ex) {
        return Arrays.stream(ex.getStackTrace())
                .limit(5)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }
}