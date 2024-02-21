package com.dwsoft.webapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.annotation.EnableAsync;

import com.dwsoft.core.launch.Application;
import com.dwsoft.core.tx.EnableGlobalTxManager;

@ImportResource({ "classpath*:spring-autoconfig-*.xml" })
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = {
		"com.dwsoft.config",
        "com.dwsoft.core",
        "com.dwsoft.webapp",
        "com.dwsoft.integration"
})
@EntityScan("com.dwsoft.webapp")
@ServletComponentScan({"com.dwsoft.core","com.dwsoft.webapp"})
@EnableAsync
@EnableGlobalTxManager(txMethodTimeOut = 10)
@SpringBootApplication
public class Starter {
    public static void main(String[] args) {
    	Application.run("hr-server" , Starter.class, args);
    }
}