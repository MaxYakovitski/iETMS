package com.mayak.ietms.infrastructure.web.test;

import com.mayak.ietms.infrastructure.notify.SlackNotifier;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SlackTestController {

    private final SlackNotifier slackNotifier;

    @GetMapping("/test/slack")
    public String testSlack() {
        slackNotifier.sendError("✅ Тэставы Slack webhook з backend!");
        return "Slack notification sent!";
    }
}