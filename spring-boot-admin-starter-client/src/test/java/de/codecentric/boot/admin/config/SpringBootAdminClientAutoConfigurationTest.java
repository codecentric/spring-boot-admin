package de.codecentric.boot.admin.config;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.actuate.autoconfigure.ManagementServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import de.codecentric.boot.admin.actuate.LogfileMvcEndpoint;
import de.codecentric.boot.admin.services.ApplicationRegistrator;

public class SpringBootAdminClientAutoConfigurationTest {

	private AnnotationConfigWebApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void not_active() {
		load();
		assertTrue(context.getBeansOfType(ApplicationRegistrator.class).isEmpty());
		assertTrue(context.getBeansOfType(LogfileMvcEndpoint.class).isEmpty());
	}

	public void not_active_logfile() {
		load();
		assertTrue(context.getBeansOfType(ApplicationRegistrator.class).isEmpty());
		context.getBean(LogfileMvcEndpoint.class);
	}

	@Test
	public void active_nologfile() {
		load("spring.boot.admin.url:http://localhost:8081");
		context.getBean(ApplicationRegistrator.class);
		assertTrue(context.getBeansOfType(LogfileMvcEndpoint.class).isEmpty());
	}

	@Test
	public void active_logfile() {
		load("spring.boot.admin.url:http://localhost:8081", "logging.file:spring.log");
		context.getBean(LogfileMvcEndpoint.class);
		context.getBean(ApplicationRegistrator.class);
	}

	@Test
	public void active_logfile_supressed() {
		load("spring.boot.admin.url:http://localhost:8081", "logging.file:spring.log",
				"endpoints.logfile.enabled:false");
		context.getBean(ApplicationRegistrator.class);
		assertTrue(context.getBeansOfType(LogfileMvcEndpoint.class).isEmpty());
	}

	private void load(String... environment) {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(ServerPropertiesAutoConfiguration.class);
		applicationContext.register(ManagementServerPropertiesAutoConfiguration.class);
		applicationContext.register(SpringBootAdminClientAutoConfiguration.class);
		EnvironmentTestUtils.addEnvironment(applicationContext, environment);
		applicationContext.refresh();
		this.context = applicationContext;
	}

}
