package com.dwsoft.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;

@Configuration
public class DruidConfig {
	
	@ConfigurationProperties(prefix = "spring.datasource")
	@Bean
	public DataSource druidDataSource() {
		return new DruidDataSource();
	}

	// 配置 Druid 监控管理后台的Servlet；
	@Bean
	public ServletRegistrationBean statViewServlet() {
		ServletRegistrationBean bean = new ServletRegistrationBean(new StatViewServlet(), "/druid/*");

		// 这些参数可以在 com.alibaba.druid.support.http.StatViewServlet
		// 的父类 com.alibaba.druid.support.http.ResourceServlet 中找到
		Map<String, String> initParams = new HashMap<>();
		initParams.put("loginUsername", "admin"); // 后台管理界面的登录账号
		initParams.put("loginPassword", "Abcabc@1234"); // 后台管理界面的登录密码

		// 后台允许谁可以访问
		//initParams.put("allow", "localhost"); //只有本机可以访问
		initParams.put("allow", "");

		// 设置初始化参数
		bean.setInitParameters(initParams);
		return bean;
	}
}
