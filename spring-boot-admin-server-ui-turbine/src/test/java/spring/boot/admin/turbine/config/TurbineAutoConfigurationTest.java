package spring.boot.admin.turbine.config;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.WebClientAutoConfiguration.RestTemplateConfiguration;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import de.codecentric.boot.admin.config.AdminServerWebConfiguration;
import de.codecentric.boot.admin.config.RevereseZuulProxyConfiguration;
import spring.boot.admin.turbine.web.TurbineController;

public class TurbineAutoConfigurationTest {

	private AnnotationConfigWebApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void test_disabled() {
		load("spring.boot.admin.turbine.enabled:false",
				"spring.boot.admin.turbine.url:http://turbine.server:8989/turbine.stream");
		assertThat(context.getBeansOfType(TurbineController.class).values(), empty());
	}

	@Test
	public void test_missing_url() {
		load();
		assertThat(context.getBeansOfType(TurbineController.class).values(), empty());
	}

	@Test
	public void test_enabled() {
		load("spring.boot.admin.turbine.url:http://turbine.server:8989/turbine.stream");
		assertThat(context.getBean(TurbineController.class), instanceOf(TurbineController.class));
	}

	private void load(String... environment) {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(PropertyPlaceholderAutoConfiguration.class);
		applicationContext.register(RestTemplateConfiguration.class);
		applicationContext.register(ServerPropertiesAutoConfiguration.class);
		applicationContext.register(AdminServerWebConfiguration.class);
		applicationContext.register(RevereseZuulProxyConfiguration.class);
		applicationContext.register(TurbineAutoConfiguration.class);

		EnvironmentTestUtils.addEnvironment(applicationContext, environment);
		applicationContext.refresh();
		this.context = applicationContext;
	}
}
