package com.mayak.iet.config;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.Properties;

public class YamlPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource (@Nullable String name, EncodedResource resource) throws IOException {

        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource.getResource());

        Properties properties = factory.getObject();
        if (properties == null) {
            properties = new Properties();
        }

        String sourceName = name;
        if (sourceName == null) {
            sourceName = resource.getResource().getFilename();
        }
        if (sourceName == null) {
            sourceName = "yaml-property-source";
        }

        return new PropertiesPropertySource(sourceName, properties);
    }
}