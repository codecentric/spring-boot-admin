package de.codecentric.boot.admin.config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.actuate.autoconfigure.ManagementServerPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerPropertiesAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
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
		load("spring.boot.admin.url:http://localhost:8081",
				"management.contextPath=/admin");
		AdminClientProperties clientProperties = context
				.getBean(AdminClientProperties.class);

		publishEvent(8080, null);
		publishEvent(8081, "management");

		assertThat(clientProperties.getUrl(), is("http://" + getHostname()
				+ ":8081/admin"));
	}

	@Test
	public void test_mgmtPort() {
		load("spring.boot.admin.url:http://localhost:8081");
		AdminClientProperties clientProperties = context
				.getBean(AdminClientProperties.class);

		publishEvent(8080, null);
		publishEvent(8081, "management");

		assertThat(clientProperties.getUrl(), is("http://" + getHostname() + ":8081"));
	}

	@Test
	public void test_contextPath_mgmtPath() {
		load("spring.boot.admin.url:http://localhost:8081", "server.context-path=app",
				"management.context-path=/admin");
		AdminClientProperties clientProperties = context
				.getBean(AdminClientProperties.class);

		publishEvent(8080, null);

		assertThat(clientProperties.getUrl(), is("http://" + getHostname()
				+ ":8080/app/admin"));
	}


	@Test
	public void test_contextPath() {
		load("spring.boot.admin.url:http://localhost:8081", "server.context-path=app");
		AdminClientProperties clientProperties = context
				.getBean(AdminClientProperties.class);

		publishEvent(8080, null);

		assertThat(clientProperties.getUrl(), is("http://" + getHostname() + ":8080/app"));
	}


	@Test
	public void test_default() {
		load("spring.boot.admin.url:http://localhost:8081");
		AdminClientProperties clientProperties = context
				.getBean(AdminClientProperties.class);

		publishEvent(8080, null);

		assertThat(clientProperties.getUrl(), is("http://" + getHostname() + ":8080"));
	}

	private String getHostname() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		}
		catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}

	private void publishEvent(int port, String namespace) {
		EmbeddedServletContainer eventSource = mock(EmbeddedServletContainer.class);
		when(eventSource.getPort()).thenReturn(port);
		EmbeddedWebApplicationContext eventContext = mock(EmbeddedWebApplicationContext.class);
		when(eventContext.getNamespace()).thenReturn(namespace);
		context.publishEvent(new EmbeddedServletContainerInitializedEvent(eventContext,
				eventSource));
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
