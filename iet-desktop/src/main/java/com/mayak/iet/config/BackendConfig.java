package com.mayak.iet.config;

import com.mayak.iet.app.BackendProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(BackendProperties.class)
@Configuration
public class BackendConfig {}