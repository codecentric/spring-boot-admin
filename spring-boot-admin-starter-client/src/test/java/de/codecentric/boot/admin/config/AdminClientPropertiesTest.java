package de.codecentric.boot.admin.config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class AdminClientPropertiesTest {

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
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);

		assertThat(clientProperties.getManagementUrl(),
				is("http://" + getHostname() + ":8081/admin"));
		assertThat(clientProperties.getHealthUrl(),
				is("http://" + getHostname() + ":8081/admin/alive"));
		assertThat(clientProperties.getServiceUrl(), is("http://" + getHostname() + ":8080"));
	}

	@Test
	public void test_contextPath_mgmtPath() {
		load("server.context-path=app", "management.context-path=/admin", "local.server.port=8080");
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);

		assertThat(clientProperties.getManagementUrl(),
				is("http://" + getHostname() + ":8080/app/admin"));
		assertThat(clientProperties.getHealthUrl(),
				is("http://" + getHostname() + ":8080/app/admin/health"));
		assertThat(clientProperties.getServiceUrl(), is("http://" + getHostname() + ":8080/app"));
	}

	@Test
	public void test_contextPatht_mgmtPortPath() {
		load("server.context-path=app", "management.context-path=/admin", "local.server.port=8080",
				"local.management.port=8081");
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);

		assertThat(clientProperties.getManagementUrl(),
				is("http://" + getHostname() + ":8081/admin"));
		assertThat(clientProperties.getHealthUrl(),
				is("http://" + getHostname() + ":8081/admin/health"));
		assertThat(clientProperties.getServiceUrl(), is("http://" + getHostname() + ":8080/app"));
	}

	@Test
	public void test_contextPath() {
		load("server.context-path=app", "local.server.port=80");
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);

		assertThat(clientProperties.getManagementUrl(), is("http://" + getHostname() + ":80/app"));
		assertThat(clientProperties.getHealthUrl(),
				is("http://" + getHostname() + ":80/app/health"));
		assertThat(clientProperties.getServiceUrl(), is("http://" + getHostname() + ":80/app"));
	}

	@Test
	public void test_servletPath() {
		load("server.servlet-path=app", "server.context-path=srv", "local.server.port=80");
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);

		assertThat(clientProperties.getManagementUrl(),
				is("http://" + getHostname() + ":80/srv/app"));
		assertThat(clientProperties.getHealthUrl(),
				is("http://" + getHostname() + ":80/srv/app/health"));
		assertThat(clientProperties.getServiceUrl(), is("http://" + getHostname() + ":80/srv"));
	}

	@Test
	public void test_default() {
		load("local.server.port=8080");
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);

		assertThat(clientProperties.getManagementUrl(), is("http://" + getHostname() + ":8080"));
		assertThat(clientProperties.getHealthUrl(), is("http://" + getHostname() + ":8080/health"));
		assertThat(clientProperties.getServiceUrl(), is("http://" + getHostname() + ":8080"));
	}

	@Test
	public void testSsl() {
		load("server.ssl.key-store=somefile.jks", "server.ssl.key-store-password=password",
				"local.server.port=8080");
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);

		assertThat(clientProperties.getManagementUrl(), is("https://" + getHostname() + ":8080"));
		assertThat(clientProperties.getHealthUrl(),
				is("https://" + getHostname() + ":8080/health"));
		assertThat(clientProperties.getServiceUrl(), is("https://" + getHostname() + ":8080"));
	}

	@Test
	public void test_preferIpAddress_serveraddress_missing() {
		load("local.server.port=8080");
		AdminClientProperties clientProperties = new AdminClientProperties();
		clientProperties.setPreferIp(true);
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);

		assertTrue(clientProperties.getServiceUrl()
				.matches("http://\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}:8080"));
	}

	@Test
	public void test_preferIpAddress_managementaddress_missing() {
		load("local.server.port=8080", "local.management.port=8081");
		AdminClientProperties clientProperties = new AdminClientProperties();
		clientProperties.setPreferIp(true);
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);
		assertTrue(clientProperties.getManagementUrl()
				.matches("http://\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}:8081"));
	}

	@Test
	public void test_preferIpAddress() {
		load("server.address=127.0.0.1", "management.address=127.0.0.2", "local.server.port=8080",
				"local.management.port=8081");
		AdminClientProperties clientProperties = new AdminClientProperties();
		clientProperties.setPreferIp(true);
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);

		assertThat(clientProperties.getManagementUrl(), is("http://127.0.0.2:8081"));
		assertThat(clientProperties.getHealthUrl(), is("http://127.0.0.2:8081/health"));
		assertThat(clientProperties.getServiceUrl(), is("http://127.0.0.1:8080"));
	}

	@Test
	public void test_serveraddress() {
		load("server.address=127.0.0.2", "local.server.port=8080");
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);

		assertThat(clientProperties.getServiceUrl(), is("http://127.0.0.2:8080"));
		assertThat(clientProperties.getManagementUrl(), is("http://127.0.0.2:8080"));
		assertThat(clientProperties.getHealthUrl(), is("http://127.0.0.2:8080/health"));
	}

	@Test
	public void test_managementaddress() {
		load("server.address=127.0.0.2", "management.address=127.0.0.3", "local.server.port=8080",
				"local.management.port=8081");
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishApplicationReadyEvent(clientProperties);

		assertThat(clientProperties.getServiceUrl(), is("http://127.0.0.2:8080"));
		assertThat(clientProperties.getManagementUrl(), is("http://127.0.0.3:8081"));
		assertThat(clientProperties.getHealthUrl(), is("http://127.0.0.3:8081/health"));
	}

	private String getHostname() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	private void publishApplicationReadyEvent(AdminClientProperties client) {
		client.onApplicationReady(
				new ApplicationReadyEvent(mock(SpringApplication.class), new String[] {}, context));
	}

	private void load(String... environment) {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(PropertyPlaceholderAutoConfiguration.class);
		applicationContext.register(ServerPropertiesAutoConfiguration.class);
		applicationContext.register(ManagementServerPropertiesAutoConfiguration.class);
		EnvironmentTestUtils.addEnvironment(applicationContext, environment);
		applicationContext.refresh();
		this.context = applicationContext;
	}

}
