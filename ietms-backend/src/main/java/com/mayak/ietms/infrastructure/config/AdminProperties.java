package com.mayak.ietms.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.admin")
@Getter
@Setter
public class AdminProperties {
    private String firstName, lastName, email, password;
}