package com.mayak.ietms.infrastructure.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PingController {

    @RequestMapping("/ping")
    public void ping() {
    }
}