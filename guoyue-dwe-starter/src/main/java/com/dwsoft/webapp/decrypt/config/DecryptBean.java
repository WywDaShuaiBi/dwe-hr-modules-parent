package com.dwsoft.webapp.decrypt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import com.dwsoft.webapp.decrypt.processor.DecryptBeanPostProcessor;

@Configuration
public class DecryptBean {

	@Bean
	public static DecryptBeanPostProcessor enableEncryptionData(final ConfigurableEnvironment environment) {

		return new DecryptBeanPostProcessor(environment);
	}
}
