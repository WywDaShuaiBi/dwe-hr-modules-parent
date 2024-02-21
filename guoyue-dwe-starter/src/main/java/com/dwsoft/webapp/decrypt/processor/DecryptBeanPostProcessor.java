package com.dwsoft.webapp.decrypt.processor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import com.dwsoft.core.tool.utils.StringUtil;
import com.dwsoft.webapp.decrypt.utils.ProfileSecureUtil;

public class DecryptBeanPostProcessor implements BeanFactoryPostProcessor, Ordered {

	private final static String ENCRYPTION_PREFIX = "ENC(";
	private final static String ENCRYPTION_SUFFIX = ")";

	private final ConfigurableEnvironment environment;

	public DecryptBeanPostProcessor(ConfigurableEnvironment environment) {
		this.environment = environment;
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 100;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// 获取是否加密配置
		String isDncrypt = environment.getProperty("dwsoft.encryptor.enable");
		if (!"true".equals(isDncrypt)) {
			return;
		}
		MutablePropertySources propertySources = environment.getPropertySources();
		for (PropertySource<?> propertySource : propertySources) {

			if (propertySource.getSource() instanceof Map) {
				Map<String, Object> source = (Map) propertySource.getSource();
				Set<String> keySet = source.keySet();
				Iterator<String> iterator = keySet.iterator();
				Map<String, Object> decryptMap = new HashMap<String, Object>();
				while (iterator.hasNext()) {
					String key = iterator.next();
					String value = environment.getProperty(key);
					if (checkIsEncryptd(value)) {
						decryptMap.put(key,
								ProfileSecureUtil.decrypt(value.substring(ENCRYPTION_PREFIX.length(), value.length())));
					}

				}
				if (decryptMap.size() > 0) {
					propertySources.addBefore(propertySource.getName(),
							new MapPropertySource(propertySource.getName() + "-decrypt", decryptMap));
				}

			}
		}
	}

	private boolean checkIsEncryptd(String value) {
		if (StringUtil.hasText(value) && value.startsWith(ENCRYPTION_PREFIX) && value.endsWith(ENCRYPTION_SUFFIX)) {
			return true;
		}
		return false;
	}
}
