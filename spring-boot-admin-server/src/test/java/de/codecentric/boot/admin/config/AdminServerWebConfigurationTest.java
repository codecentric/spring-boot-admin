package de.codecentric.boot.admin.config;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.cloud.client.discovery.noop.NoopDiscoveryClientAutoConfiguration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import de.codecentric.boot.admin.discovery.ApplicationDiscoveryListener;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.registry.store.HazelcastApplicationStore;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

public class AdminServerWebConfigurationTest {

	private AnnotationConfigWebApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void simpleConfig() {
		load("spring.boot.admin.hazelcast.enabled:false", "spring.boot.admin.discovery.enabled:false");
		assertTrue(context.getBean(ApplicationStore.class) instanceof SimpleApplicationStore);
		assertTrue(context.getBeansOfType(ApplicationDiscoveryListener.class).isEmpty());
	}

	@Test
	public void hazelcastConfig() {
		load("spring.boot.admin.hazelcast.enabled:true", "spring.boot.admin.discovery.enabled:false");
		assertTrue(context.getBean(ApplicationStore.class) instanceof HazelcastApplicationStore);
		assertTrue(context.getBeansOfType(ApplicationDiscoveryListener.class).isEmpty());
	}

	@Test
	public void discoveryConfig() {
		load("spring.boot.admin.hazelcast.enabled:false", "spring.boot.admin.discovery.enabled:true");
		assertTrue(context.getBean(ApplicationStore.class) instanceof SimpleApplicationStore);
		context.getBean(ApplicationDiscoveryListener.class);
	}


	private void load(String... environment) {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(ServerPropertiesAutoConfiguration.class);
		applicationContext.register(NoopDiscoveryClientAutoConfiguration.class);
		applicationContext.register(AdminServerWebConfiguration.class);
		EnvironmentTestUtils.addEnvironment(applicationContext, environment);
		applicationContext.refresh();
		this.context = applicationContext;
	}

}
