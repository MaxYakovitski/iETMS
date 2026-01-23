package com.mayak.iet.integration.bridge;

import com.mayak.iet.integration.auth.AuthState;
import com.mayak.iet.integration.bridge.server.DesktopBridgeServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DesktopBridgeConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public DesktopBridgeServer desktopBridgeServer(AuthState authState) {
        return new DesktopBridgeServer(authState);
    }
}