package com.mayak.iet.infrastructure.version;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

@Component
public class AppVersionProvider {

    private final BuildProperties buildProperties;

    public AppVersionProvider(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public String getAppVersion() {
        return buildProperties.get("app.version");
    }
}