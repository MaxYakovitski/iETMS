package com.mayak.ietms.integration.bridge;

import com.mayak.ietms.integration.auth.AuthState;
import com.mayak.ietms.integration.bridge.server.DesktopBridgeServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DesktopBridgeConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public DesktopBridgeServer desktopBridgeServer(AuthState authState) {
        return new DesktopBridgeServer(authState);
    }
}