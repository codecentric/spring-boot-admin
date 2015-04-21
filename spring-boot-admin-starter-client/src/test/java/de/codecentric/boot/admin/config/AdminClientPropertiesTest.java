package de.codecentric.boot.admin.config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
import org.springframework.context.event.ContextRefreshedEvent;
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
	public void test_isServerStarted_false() {
		assertFalse(new AdminClientProperties().isServerInitialized());
	}

	@Test
	public void test_isServerStarted_true_embedded() {
		AdminClientProperties clientProperties = new AdminClientProperties();
		clientProperties.setUrl("http://localhost");
		publishServletContainerInitializedEvent(clientProperties, 8080, null);
		assertTrue(clientProperties.isServerInitialized());
	}

	@Test
	public void test_isServerStarted_true_war() {
		AdminClientProperties clientProperties = new AdminClientProperties();
		clientProperties.setUrl("http://localhost");
		publishContextRefreshedEvent(clientProperties);
		assertTrue(clientProperties.isServerInitialized());
	}

	@Test(expected = RuntimeException.class)
	public void test_isServerStarted_exception_war() {
		AdminClientProperties clientProperties = new AdminClientProperties();
		publishContextRefreshedEvent(clientProperties);
	}

	@Test
	public void test_mgmtPortPath() {
		load("management.contextPath=/admin");
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishServletContainerInitializedEvent(clientProperties, 8080, null);
		publishServletContainerInitializedEvent(clientProperties, 8081, "management");

		assertThat(clientProperties.getUrl(), is("http://" + getHostname()
				+ ":8081/admin"));
	}

	@Test
	public void test_mgmtPort() {
		load();
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishServletContainerInitializedEvent(clientProperties, 8080, null);
		publishServletContainerInitializedEvent(clientProperties, 8081, "management");

		assertThat(clientProperties.getUrl(), is("http://" + getHostname() + ":8081"));
	}

	@Test
	public void test_contextPath_mgmtPath() {
		load("server.context-path=app",
				"management.context-path=/admin");
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishServletContainerInitializedEvent(clientProperties, 8080, null);

		assertThat(clientProperties.getUrl(), is("http://" + getHostname()
				+ ":8080/app/admin"));
	}


	@Test
	public void test_contextPath() {
		load("server.context-path=app");
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishServletContainerInitializedEvent(clientProperties, 8080, null);

		assertThat(clientProperties.getUrl(), is("http://" + getHostname() + ":8080/app"));
	}


	@Test
	public void test_default() {
		load();
		AdminClientProperties clientProperties = new AdminClientProperties();
		context.getAutowireCapableBeanFactory().autowireBean(clientProperties);

		publishServletContainerInitializedEvent(clientProperties, 8080, null);

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

	private void publishServletContainerInitializedEvent(AdminClientProperties client,
			int port, String namespace) {
		EmbeddedServletContainer eventSource = mock(EmbeddedServletContainer.class);
		when(eventSource.getPort()).thenReturn(port);
		EmbeddedWebApplicationContext eventContext = mock(EmbeddedWebApplicationContext.class);
		when(eventContext.getNamespace()).thenReturn(namespace);
		when(eventContext.getEmbeddedServletContainer()).thenReturn(eventSource);
		client.onApplicationEvent(new EmbeddedServletContainerInitializedEvent(
				eventContext,
				eventSource));
	}

	private void publishContextRefreshedEvent(AdminClientProperties client) {
		client.onApplicationEvent(new ContextRefreshedEvent(
				mock(EmbeddedWebApplicationContext.class)));
	}

	private void load(String... environment) {
		AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
		applicationContext.register(ServerPropertiesAutoConfiguration.class);
		applicationContext.register(ManagementServerPropertiesAutoConfiguration.class);
		EnvironmentTestUtils.addEnvironment(applicationContext, environment);
		applicationContext.refresh();
		this.context = applicationContext;
	}

}
