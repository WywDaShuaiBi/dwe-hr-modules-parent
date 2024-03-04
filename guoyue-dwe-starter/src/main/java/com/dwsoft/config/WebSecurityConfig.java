package com.dwsoft.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import com.dwsoft.core.annotation.SecurityDefinition;
import com.dwsoft.core.authority.integration.DefaultPermissionEvaluator;
import com.dwsoft.core.security.access.expression.GlobalWebSecurityExpressionHandler;
import com.dwsoft.core.security.access.expression.method.GlobalMethodSecurityExpressionHandler;
import com.dwsoft.core.security.access.intercept.FieldSecurityValidator;
import com.dwsoft.core.security.access.intercept.config.WebSecurityConfigurer;


/**
 * @author Sulta
 */
@Configuration
public class WebSecurityConfig {
	
	private final static Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);

	@Autowired
    FieldSecurityValidator fieldSecurityValidator;
    
    @Autowired
    DefaultPermissionEvaluator permissionEvaluator;
    
    @Bean
    GlobalWebSecurityExpressionHandler globalWebSecurityExpressionHandler() {
    	GlobalWebSecurityExpressionHandler expressionHandler = new GlobalWebSecurityExpressionHandler();
    	expressionHandler.setFieldSecurityValidator(fieldSecurityValidator);
		expressionHandler.setPermissionEvaluator(permissionEvaluator);
		return expressionHandler;
    }
        
    @Bean
    AffirmativeBased accessDecisionManager() throws Exception {
		List<AccessDecisionVoter<? extends Object>> voters = new ArrayList<AccessDecisionVoter<? extends Object>>();
		
		WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
		
		webExpressionVoter.setExpressionHandler(globalWebSecurityExpressionHandler());
		
		voters.add(webExpressionVoter);
		
		RoleVoter voter = new RoleVoter();
		voters.add(voter);
		
		voters.add(new AuthenticatedVoter());
		
		AffirmativeBased affirmativeBased = new AffirmativeBased(voters);
		affirmativeBased.setAllowIfAllAbstainDecisions(false);
		affirmativeBased.afterPropertiesSet();
		return affirmativeBased;
	}
        
    @Configuration
    @Order(-20)
    static class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    	private final AuthenticationManager authenticationManager;
    	
		private final List<WebSecurityConfigurer> webSecConfigs = new ArrayList<WebSecurityConfigurer>();
        
        @Autowired
        public WebSecurityConfiguration(
        		AuthenticationManager authenticationManager,
                WebSecurityConfigurer... webSecConfigs) {
        	
        	this.authenticationManager = authenticationManager;
        	
        	if(webSecConfigs != null) {
        		for (WebSecurityConfigurer webSecConfig : webSecConfigs) {
        			this.webSecConfigs.add(webSecConfig);
				}
        	}
			
			Reflections reflections = new Reflections("com.dwsoft.webapp");
			Set<Class<?>> annotatedSet = reflections.getTypesAnnotatedWith(SecurityDefinition.class);
			for (Class<?> clazz : annotatedSet) {
				//SecurityDefinition anno = AnnotationUtils.getAnnotation(clazz, SecurityDefinition.class);
				if (WebSecurityConfigurer.class.isAssignableFrom(clazz)) {
					try {
						WebSecurityConfigurer webSecConfig = (WebSecurityConfigurer) clazz.newInstance();
						this.webSecConfigs.add(webSecConfig);
					} catch (Exception e) {
						logger.error("",e);
					}
				}
			}
			this.webSecConfigs.sort(OrderComparator.INSTANCE);
        }

        
		@Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.parentAuthenticationManager(authenticationManager);
            
			if (webSecConfigs != null) {
				webSecConfigs.forEach(webSecConfig -> {
					try {
						webSecConfig.configure(auth);
					} catch (Exception e) {
						logger.error("配置AuthenticationManagerBuilder时发生错误", e);
					}
				});
			}
        }
        
        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers(
                    "/",
                    "/v2/api-docs/**",
                    "/webjars/**",
                    "/swagger-resources",
                    "/swagger-resources/**",
                    "/*.html",
                    "/*.js",
                    "/*.css",
                    "/*.svg",
                    "/*.ttf",
                    "/*.woff",
                    "/*.eot",
                    "/*.ttf",
                    "/*.png",
                    "/antd/**",
                    "/.~~spring-boot!~/**",
                    "/.~~spring-boot!~",
					"/user/login");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
	            .formLogin()
	            .disable()
	            .logout()
	            .disable();
            
            if(webSecConfigs != null) {
            	webSecConfigs.forEach( webSecConfig -> {
            		try {
						webSecConfig.configure(http);
					} catch (Exception e) {
						logger.error("配置HttpSecurity时发生错误",e);
					}
            	});
            }
        }
    }
    
    @Configuration
    @SecurityDefinition
    static class HttpSecurityConfigurer implements WebSecurityConfigurer {
    	private final AffirmativeBased accessDecisionManager;
    	
    	@Override
    	public int getOrder() {
    		return LOWEST_PRECEDENCE;
    	}
    	
    	public HttpSecurityConfigurer(AffirmativeBased accessDecisionManager) {
    		 this.accessDecisionManager = accessDecisionManager;
		}
    	
    	@Override
    	public void configure(HttpSecurity http) throws Exception {
    		http
            .authorizeRequests()
            .antMatchers(
            		"/druid/**",
            		"/dingtalk/config",
            		"/dingtalk/processLogin",
            		"/dingtalk/processAdminLogin",
            		"/dingtalk/getDefaultAppId",
            		"/dingtalk/ddScanCodeLogin", //钉钉扫码登录
					"/user/captchaImage",
                    "/checkVersion/**", //金蝶APP
                    "/api/cloud/getToken", //金蝶APP
                    "/login",
                    "/logout",
							"/dingtalk/calendar/**", "/openapi/auth/**")
					.permitAll()
            .anyRequest()
            .authenticated()
            .accessDecisionManager(accessDecisionManager);
            
			http.csrf().disable();
            
            //没登录的，返回401，而不是403：
            http.exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
    	}
    }
    

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true , securedEnabled = true)
	public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    	
		@Override
		protected MethodSecurityExpressionHandler createExpressionHandler() {
			GlobalMethodSecurityExpressionHandler expressionHandler = new GlobalMethodSecurityExpressionHandler();
			expressionHandler.setPermissionEvaluator(permissionEvaluator);
			expressionHandler.setFieldSecurityValidator(fieldSecurityValidator);
			return expressionHandler;
		}
	}
}
