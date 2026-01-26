package com.mayak.iet.app;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(BackendProperties.class)
@Configuration
public class BackendConfig {}