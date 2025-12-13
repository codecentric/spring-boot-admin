/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.client.registration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;
import org.springframework.mock.web.MockServletContext;

import de.codecentric.boot.admin.client.config.InstanceProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServletApplicationFactoryTest {

	private final InstanceProperties instance = new InstanceProperties();

	private final ServerProperties server = new ServerProperties();

	private final ManagementServerProperties management = new ManagementServerProperties();

	private final MockServletContext servletContext = new MockServletContext();

	private final PathMappedEndpoints pathMappedEndpoints = mock(PathMappedEndpoints.class);

	private final WebEndpointProperties webEndpoint = new WebEndpointProperties();

	private final DispatcherServletPath dispatcherServletPath = mock(DispatcherServletPath.class);

	private final ServletApplicationFactory factory = new ServletApplicationFactory(this.instance, this.management,
			this.server, this.servletContext, this.pathMappedEndpoints, this.webEndpoint, Collections::emptyMap,
			this.dispatcherServletPath);

	@BeforeEach
	void setup() {
		this.instance.setName("test");
		when(this.dispatcherServletPath.getPrefix()).thenReturn("");
	}

	@Test
	void test_contextPath_mgmtPath() {
		this.servletContext.setContextPath("app");
		this.webEndpoint.setBasePath("/admin");
		when(this.pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/admin/health");
		publishApplicationReadyEvent(this.factory, 8080, null);

		Application app = this.factory.createApplication();
		assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":8080/app/admin");
		assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":8080/app/admin/health");
		assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/app");
	}

	@Test
	void test_contextPath_mgmtPortPath() {
		this.servletContext.setContextPath("app");
		this.webEndpoint.setBasePath("/admin");
		when(this.pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/admin/health");
		publishApplicationReadyEvent(this.factory, 8080, 8081);

		Application app = this.factory.createApplication();
		assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":8081/admin");
		assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":8081/admin/health");
		assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/app");
	}

	@Test
	void test_contextPath() {
		this.servletContext.setContextPath("app");
		when(this.pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/actuator/health");
		publishApplicationReadyEvent(this.factory, 80, null);

		Application app = this.factory.createApplication();
		assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":80/app/actuator");
		assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":80/app/actuator/health");
		assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":80/app");
	}

	@Test
	void test_servletPath() {
		when(this.dispatcherServletPath.getPrefix()).thenReturn("app");
		this.servletContext.setContextPath("srv");
		when(this.pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/actuator/health");
		publishApplicationReadyEvent(this.factory, 80, null);

		Application app = this.factory.createApplication();
		assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":80/srv/app/actuator");
		assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":80/srv/app/actuator/health");
		assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":80/srv");
	}

	@Test
	void test_servicePath() {
		this.servletContext.setContextPath("app");
		when(this.pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/actuator/health");
		publishApplicationReadyEvent(this.factory, 80, null);
		this.instance.setServicePath("/servicePath/");

		Application app = this.factory.createApplication();
		assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":80/servicePath/app/actuator");
		assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":80/servicePath/app/actuator/health");
		assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":80/servicePath/app");
	}

	private String getHostname() {
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		}
		catch (UnknownHostException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private void publishApplicationReadyEvent(DefaultApplicationFactory factory, Integer serverport,
			Integer managementport) {
		factory.onWebServerInitialized(new TestWebServerInitializedEvent("server", serverport));
		factory.onWebServerInitialized(new TestWebServerInitializedEvent("management",
				(managementport != null) ? managementport : serverport));
	}

	private static final class TestWebServerInitializedEvent extends WebServerInitializedEvent {

		private final WebServer server = mock(WebServer.class);

		private final WebServerApplicationContext context = mock(WebServerApplicationContext.class);

		private TestWebServerInitializedEvent(String name, int port) {
			super(mock(WebServer.class));
			when(this.server.getPort()).thenReturn(port);
			when(this.context.getServerNamespace()).thenReturn(name);
		}

		@Override
		public WebServerApplicationContext getApplicationContext() {
			return this.context;
		}

		@Override
		public WebServer getWebServer() {
			return this.server;
		}

	}

}
