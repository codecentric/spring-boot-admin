package de.codecentric.boot.admin.services;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.codecentric.boot.admin.config.AdminClientProperties;
import de.codecentric.boot.admin.model.Application;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.EndpointWebMvcManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.ManagementServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class ApplicationFactoryTest {

	private AnnotationConfigWebApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void test_mgmtPortPath() {
		load("management.contextPath=/admin", "endpoints.health.id=alive", "local.server.port=8080",
				"local.management.port=8081");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":8081/admin"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":8081/admin/alive"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":8080"));
	}

	@Test
	public void test_contextPath_mgmtPath() {
		load("server.context-path=app", "management.context-path=/admin", "local.server.port=8080");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":8080/app/admin"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":8080/app/admin/health"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":8080/app"));
	}

	@Test
	public void test_contextPatht_mgmtPortPath() {
		load("server.context-path=app", "management.context-path=/admin", "local.server.port=8080",
				"local.management.port=8081", "endpoints.health.path=/foo/bar");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":8081/admin"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":8081/admin/foo/bar"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":8080/app"));
	}

	@Test
	public void test_contextPath() {
		load("server.context-path=app", "local.server.port=80");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":80/app"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":80/app/health"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":80/app"));
	}

	@Test
	public void test_servletPath() {
		load("server.servlet-path=app", "server.context-path=srv", "local.server.port=80");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":80/srv/app"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":80/srv/app/health"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":80/srv"));
	}

	@Test
	public void test_default() {
		load("local.server.port=8080");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":8080"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":8080/health"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":8080"));
	}

	@Test
	public void test_ssl() {
		load("server.ssl.key-store=somefile.jks", "server.ssl.key-store-password=password",
				"local.server.port=8080");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("https://" + getHostname() + ":8080"));
		assertThat(app.getHealthUrl(), is("https://" + getHostname() + ":8080/health"));
		assertThat(app.getServiceUrl(), is("https://" + getHostname() + ":8080"));
	}

	@Test
	public void test_ssl_managment() {
		load("management.ssl.key-store=somefile.jks", "management.ssl.key-store-password=password",
				"local.server.port=8080", "local.management.port=9090");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("https://" + getHostname() + ":9090"));
		assertThat(app.getHealthUrl(), is("https://" + getHostname() + ":9090/health"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":8080"));
	}

	@Test
	public void test_preferIpAddress_serveraddress_missing() {
		load("spring.boot.admin.client.prefer-ip=true", "local.server.port=8080");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertTrue(app.getServiceUrl()
				.matches("http://\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}:8080"));
	}

	@Test
	public void test_preferIpAddress_managementaddress_missing() {
		load("spring.boot.admin.client.prefer-ip=true", "local.server.port=8080",
				"local.management.port=8081");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertTrue(app.getManagementUrl()
				.matches("http://\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}:8081"));
	}

	@Test
	public void test_preferIpAddress() {
		load("spring.boot.admin.client.prefer-ip=true", "server.address=127.0.0.1",
				"management.address=127.0.0.2", "local.server.port=8080",
				"local.management.port=8081");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://127.0.0.2:8081"));
		assertThat(app.getHealthUrl(), is("http://127.0.0.2:8081/health"));
		assertThat(app.getServiceUrl(), is("http://127.0.0.1:8080"));
	}

	@Test
	public void test_serveraddress() {
		load("server.address=127.0.0.2", "local.server.port=8080");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getServiceUrl(), is("http://127.0.0.2:8080"));
		assertThat(app.getManagementUrl(), is("http://127.0.0.2:8080"));
		assertThat(app.getHealthUrl(), is("http://127.0.0.2:8080/health"));
	}

	@Test
	public void test_managementaddress() {
		load("server.address=127.0.0.2", "management.address=127.0.0.3", "local.server.port=8080",
				"local.management.port=8081");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getServiceUrl(), is("http://127.0.0.2:8080"));
		assertThat(app.getManagementUrl(), is("http://127.0.0.3:8081"));
		assertThat(app.getHealthUrl(), is("http://127.0.0.3:8081/health"));
	}

	@Test
	public void test_allcustom() {
		load("spring.boot.admin.client.service-url=http://service",
				"spring.boot.admin.client.management-url=http://management",
				"spring.boot.admin.client.health-url=http://health");
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);
		publishApplicationReadyEvent(factory);

		Application app = factory.createApplication();
		assertThat(app.getServiceUrl(), is("http://service"));
		assertThat(app.getManagementUrl(), is("http://management"));
		assertThat(app.getHealthUrl(), is("http://health"));
	}

	@Test
	public void test_missingports() {
		load();
		ApplicationFactory factory = context.getBean(ApplicationFactory.class);

		try {
			factory.doServiceUrl();
			fail("IllegalStateException expected");
		} catch (IllegalStateException ex) {
			assertThat(ex.getMessage(), containsString("serviceUrl"));
		}

		try {
			factory.doManagementUrl();
			fail("IllegalStateException expected");
		} catch (IllegalStateException ex) {
			assertThat(ex.getMessage(), containsString("serviceUrl"));
		}

		try {
			factory.doHealthUrl();
			fail("IllegalStateException expected");
		} catch (IllegalStateException ex) {
			assertThat(ex.getMessage(), containsString("serviceUrl"));
		}

	}

	private String getHostname() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	private void publishApplicationReadyEvent(ApplicationFactory factory) {
		factory.onApplicationReady(
				new ApplicationReadyEvent(mock(SpringApplication.class), new String[] {}, context));
	}

	private void load(String... environment) {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(PropertyPlaceholderAutoConfiguration.class);
		applicationContext.register(ServerPropertiesAutoConfiguration.class);
		applicationContext.register(ManagementServerPropertiesAutoConfiguration.class);
		applicationContext.register(EndpointAutoConfiguration.class);
		applicationContext.register(EndpointWebMvcManagementContextConfiguration.class);
		applicationContext.register(TestConfig.class);
		applicationContext.register(ApplicationFactory.class);
		EnvironmentTestUtils.addEnvironment(applicationContext, environment);
		applicationContext.refresh();
		this.context = applicationContext;
	}

	@Configuration
	@EnableConfigurationProperties({ AdminClientProperties.class })
	public static class TestConfig {
	}

}
