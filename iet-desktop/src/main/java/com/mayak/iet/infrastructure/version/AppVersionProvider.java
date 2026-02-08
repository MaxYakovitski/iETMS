package com.mayak.iet.infrastructure.version;

import org.springframework.stereotype.Component;

@Component
public class AppVersionProvider {

    public String getAppVersion() {
        String version = AppVersionProvider.class
                .getPackage()
                .getImplementationVersion();

        return version != null ? version : "dev";
    }
}