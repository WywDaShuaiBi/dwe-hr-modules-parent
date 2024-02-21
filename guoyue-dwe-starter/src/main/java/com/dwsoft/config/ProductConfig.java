package com.dwsoft.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author 
 */
@Configuration
@EnableConfigurationProperties(ProductProperties.class)
public class ProductConfig {

    private final ProductProperties properties;

    @Autowired
    public ProductConfig(ProductProperties properties) {
        this.properties = properties;
    }
}
