package com.dwsoft.webapp.sysinit;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.dwsoft.core.interfaces.AppInitializingBean;
import com.dwsoft.core.spring.SpringUtil;
import com.dwsoft.webapp.ApplicationTestRunner;
import com.dwsoft.webapp.Starter;

@RunWith(ApplicationTestRunner.class)
@SpringBootTest(classes = {Starter.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class Init {
	
	//如果不想通过前端去执行系统初始化，可以用这个
	@Test
	@Rollback(false)
	@Transactional
	public void Run() throws Exception {
		List<AppInitializingBean> initializeBeans = SpringUtil.getBeansForType(AppInitializingBean.class);
		Collections.sort(initializeBeans, new Comparator<AppInitializingBean>() {
			@Override
			public int compare(AppInitializingBean o1, AppInitializingBean o2) {
				return Integer.valueOf(o1.getInitializingOrder()).compareTo(Integer.valueOf(o2.getInitializingOrder()));
			}
		});
		
		for (AppInitializingBean appInitializingBean : initializeBeans) {
			appInitializingBean.onAppInitializing();
		}
	}
}
