package com.dwsoft.webapp;

import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class ApplicationTestRunner extends SpringJUnit4ClassRunner{

	public ApplicationTestRunner(Class<?> clazz) throws InitializationError {
		super(init(clazz));
	}

	private static Class<?> init(Class<?> clazz) {
		    return clazz;
	}
}
