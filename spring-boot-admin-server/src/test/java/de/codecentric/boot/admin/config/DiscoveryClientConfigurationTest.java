package de.codecentric.boot.admin.config;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration.RestTemplateConfiguration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.noop.NoopDiscoveryClientAutoConfiguration;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import de.codecentric.boot.admin.discovery.DefaultServiceInstanceConverter;
import de.codecentric.boot.admin.discovery.EurekaServiceInstanceConverter;
import de.codecentric.boot.admin.discovery.ServiceInstanceConverter;
import de.codecentric.boot.admin.model.Application;

public class DiscoveryClientConfigurationTest {

	private AnnotationConfigWebApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void defaultServiceInstanceConverter() {
		load(NoopDiscoveryClientAutoConfiguration.class);
		assertThat(context.getBean(ServiceInstanceConverter.class),
				is(instanceOf(DefaultServiceInstanceConverter.class)));
	}

	@Test
	public void eurekaServiceInstanceConverter() {
		load(UtilAutoConfiguration.class, EurekaClientAutoConfiguration.class);
		assertThat(context.getBean(ServiceInstanceConverter.class),
				is(instanceOf(EurekaServiceInstanceConverter.class)));
	}

	@Test
	public void customServiceInstanceConverter() {
		load(NoopDiscoveryClientAutoConfiguration.class,
				TestCustomServiceInstanceConverterConfig.class);
		assertThat(context.getBean(ServiceInstanceConverter.class),
				is(instanceOf(CustomServiceInstanceConverter.class)));
	}


	@Configuration
	static class TestCustomServiceInstanceConverterConfig {
		@Bean
		public CustomServiceInstanceConverter converter() {
			return new CustomServiceInstanceConverter();
		}
	}

	static class CustomServiceInstanceConverter implements ServiceInstanceConverter {
		@Override
		public Application convert(ServiceInstance instance) {
			return null;
		}
	}

	private void load(Class<?>... configs) {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		for (Class<?> config : configs) {
			applicationContext.register(config);
		}

		applicationContext.register(PropertyPlaceholderAutoConfiguration.class);
		applicationContext.register(RestTemplateConfiguration.class);
		applicationContext.register(ServerPropertiesAutoConfiguration.class);
		applicationContext.register(AdminServerWebConfiguration.class);
		applicationContext.register(DiscoveryClientConfiguration.class);

		applicationContext.refresh();
		this.context = applicationContext;
	}
}
