package com.mayak.ietms;

import com.mayak.ietms.config.PropertyConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@ComponentScan("com.mayak.ietms")
@Import(PropertyConfig.class)
@EnableScheduling
public class DesktopApplication {}