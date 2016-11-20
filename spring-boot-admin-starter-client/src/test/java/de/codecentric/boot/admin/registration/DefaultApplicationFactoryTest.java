package de.codecentric.boot.admin.registration;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.Ssl;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import de.codecentric.boot.admin.client.config.AdminClientProperties;
import de.codecentric.boot.admin.client.registration.Application;
import de.codecentric.boot.admin.client.registration.DefaultApplicationFactory;

public class DefaultApplicationFactoryTest {

	private AdminClientProperties client = new AdminClientProperties();
	private ServerProperties server = new ServerProperties();
	private ManagementServerProperties management = new ManagementServerProperties();
	private DefaultApplicationFactory factory = new DefaultApplicationFactory(client, management,
			server, "/health");

	@Before
	public void setup() {
		client.setName("test");
	}

	@Test
	public void test_mgmtPortPath() {
		management.setContextPath("/admin");
		DefaultApplicationFactory factory = new DefaultApplicationFactory(client, management,
				server, "/alive");

		publishApplicationReadyEvent(factory, 8080, 8081);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":8081/admin"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":8081/admin/alive"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":8080"));
	}

	@Test
	public void test_contextPath_mgmtPath() {
		server.setContextPath("app");
		management.setContextPath("/admin");
		publishApplicationReadyEvent(factory, 8080, null);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":8080/app/admin"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":8080/app/admin/health"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":8080/app"));
	}

	@Test
	public void test_contextPath_mgmtPortPath() {
		server.setContextPath("app");
		management.setContextPath("/admin");
		publishApplicationReadyEvent(factory, 8080, 8081);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":8081/admin"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":8081/admin/health"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":8080/app"));
	}

	@Test
	public void test_contextPath() {
		server.setContextPath("app");
		publishApplicationReadyEvent(factory, 80, null);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":80/app"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":80/app/health"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":80/app"));
	}

	@Test
	public void test_servletPath() {
		server.setServletPath("app");
		server.setContextPath("srv");
		publishApplicationReadyEvent(factory, 80, null);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":80/srv/app"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":80/srv/app/health"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":80/srv"));
	}

	@Test
	public void test_default() {
		publishApplicationReadyEvent(factory, 8080, null);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://" + getHostname() + ":8080"));
		assertThat(app.getHealthUrl(), is("http://" + getHostname() + ":8080/health"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":8080"));
	}

	@Test
	public void test_ssl() {
		server.setSsl(new Ssl());
		server.getSsl().setEnabled(true);
		publishApplicationReadyEvent(factory, 8080, null);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("https://" + getHostname() + ":8080"));
		assertThat(app.getHealthUrl(), is("https://" + getHostname() + ":8080/health"));
		assertThat(app.getServiceUrl(), is("https://" + getHostname() + ":8080"));
	}

	@Test
	public void test_ssl_managment() {
		management.setSsl(new Ssl());
		management.getSsl().setEnabled(true);
		publishApplicationReadyEvent(factory, 8080, 9090);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("https://" + getHostname() + ":9090"));
		assertThat(app.getHealthUrl(), is("https://" + getHostname() + ":9090/health"));
		assertThat(app.getServiceUrl(), is("http://" + getHostname() + ":8080"));
	}

	@Test
	public void test_preferIpAddress_serveraddress_missing() {
		client.setPreferIp(true);
		publishApplicationReadyEvent(factory, 8080, null);

		Application app = factory.createApplication();
		assertTrue(app.getServiceUrl()
				.matches("http://\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}:8080"));
	}

	@Test
	public void test_preferIpAddress_managementaddress_missing() {
		client.setPreferIp(true);
		publishApplicationReadyEvent(factory, 8080, 8081);

		Application app = factory.createApplication();
		assertTrue(app.getManagementUrl()
				.matches("http://\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}:8081"));
	}

	@Test
	public void test_preferIpAddress() throws UnknownHostException {
		client.setPreferIp(true);
		server.setAddress(InetAddress.getByName("127.0.0.1"));
		management.setAddress(InetAddress.getByName("127.0.0.2"));
		publishApplicationReadyEvent(factory, 8080, 8081);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl(), is("http://127.0.0.2:8081"));
		assertThat(app.getHealthUrl(), is("http://127.0.0.2:8081/health"));
		assertThat(app.getServiceUrl(), is("http://127.0.0.1:8080"));
	}

	@Test
	public void test_allcustom() {
		client.setHealthUrl("http://health");
		client.setManagementUrl("http://management");
		client.setServiceUrl("http://service");

		Application app = factory.createApplication();
		assertThat(app.getServiceUrl(), is("http://service"));
		assertThat(app.getManagementUrl(), is("http://management"));
		assertThat(app.getHealthUrl(), is("http://health"));
	}

	@Test
	public void test_missingports() {
		try {
			factory.createApplication();
			fail("IllegalStateException expected");
		} catch (IllegalStateException ex) {
			assertThat(ex.getMessage(), containsString("serviceUrl"));
		}

	}

	private String getHostname() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			throw new IllegalStateException(e);
		}
	}

	private void publishApplicationReadyEvent(DefaultApplicationFactory factory, Integer serverport,
			Integer managementport) {

		MockEnvironment env = new MockEnvironment();
		if (serverport != null) {
			env.setProperty("local.server.port", serverport.toString());
		}
		if (managementport != null) {
			env.setProperty("local.management.port", managementport.toString());
		}

		ConfigurableWebApplicationContext context = mock(ConfigurableWebApplicationContext.class);
		when(context.getEnvironment()).thenReturn(env);
		factory.onApplicationReady(
				new ApplicationReadyEvent(mock(SpringApplication.class), new String[] {}, context));
	}

}
