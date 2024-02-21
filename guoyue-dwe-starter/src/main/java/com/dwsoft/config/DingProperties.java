package com.dwsoft.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author
 */
@ConfigurationProperties
public class DingProperties {
	
	@Value("#{ding4jConfig}")
	public Properties props;
	
}
