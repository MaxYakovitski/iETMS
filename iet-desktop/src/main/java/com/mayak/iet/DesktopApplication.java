package com.mayak.iet;

import com.mayak.iet.config.PropertyConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan("com.mayak.iet")
@Import(PropertyConfig.class)
@EnableScheduling
public class DesktopApplication {}