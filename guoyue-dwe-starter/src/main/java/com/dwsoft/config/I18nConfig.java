package com.dwsoft.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import com.dwsoft.core.i18n.interceptor.MessageResourceInterceptor;

@Configuration
public class I18nConfig {
	
	// 配置cookie语言解析器
	@Bean
	public LocaleResolver localeResolver() {
		CookieLocaleResolver resolver = new CookieLocaleResolver();
		resolver.setCookieMaxAge(3600); // cookie有效时长，单位秒
		resolver.setCookieName("Language"); // 设置存储的Cookie的name为Language
		return resolver;
	}

	// 配置一个拦截器，拦截国际化语言的变化
	@Bean
	public WebMvcConfigurer webMvcConfigurer() {
		return new WebMvcConfigurer() {
			// 拦截器
			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				registry.addInterceptor(new LocaleChangeInterceptor()).addPathPatterns("/**");
                registry.addInterceptor(new MessageResourceInterceptor()).addPathPatterns("/**");
			}
		};
	}
}
