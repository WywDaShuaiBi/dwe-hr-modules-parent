package com.dwsoft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.dwsoft.core.jpa.auditing.AutoAuditorAware;

/**
 * @author Sulta
 *
 */
@Configuration
@EnableJpaAuditing
public class AuditorConfig {
	
	@Bean
	public AutoAuditorAware auditorProvider() {
		return new AutoAuditorAware();
	}

}
