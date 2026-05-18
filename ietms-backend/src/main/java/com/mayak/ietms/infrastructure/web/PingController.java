package com.mayak.ietms.infrastructure.web;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Hidden
public class PingController {

    @RequestMapping("/ping")
    public void ping() {
    }
}