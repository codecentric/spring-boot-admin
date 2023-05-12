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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;

import de.codecentric.boot.admin.client.config.InstanceProperties;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReactiveApplicationFactoryTest {

	private InstanceProperties instanceProperties = new InstanceProperties();

	private ServerProperties server = new ServerProperties();

	private ManagementServerProperties management = new ManagementServerProperties();

	private PathMappedEndpoints pathMappedEndpoints = mock(PathMappedEndpoints.class);

	private WebEndpointProperties webEndpoint = new WebEndpointProperties();

	private WebFluxProperties webflux = new WebFluxProperties();

	private ReactiveApplicationFactory factory = new ReactiveApplicationFactory(instanceProperties, management, server,
			pathMappedEndpoints, webEndpoint, () -> singletonMap("contributor", "test"), webflux);

	@BeforeEach
	public void setup() {
		instanceProperties.setName("test");
	}

	@Test
	public void test_contextPath_mgmtPath() {
		webflux.setBasePath("/app");
		webEndpoint.setBasePath("/admin");
		when(pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/admin/health");
		publishApplicationReadyEvent(factory, 8080, null);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":8080/app/admin");
		assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":8080/app/admin/health");
		assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/app");
	}

	@Test
	public void test_contextPath_mgmtPortPath() {
		webflux.setBasePath("/app");
		webEndpoint.setBasePath("/admin");
		when(pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/admin/health");
		publishApplicationReadyEvent(factory, 8080, 8081);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":8081/admin");
		assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":8081/admin/health");
		assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/app");
	}

	@Test
	public void test_basePath() {
		webflux.setBasePath("/app");
		when(pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/actuator/health");
		publishApplicationReadyEvent(factory, 80, null);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":80/app/actuator");
		assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":80/app/actuator/health");
		assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":80/app");
	}

	@Test
	public void test_noBasePath() {
		when(pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/actuator/health");
		publishApplicationReadyEvent(factory, 80, null);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":80/actuator");
		assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":80/actuator/health");
		assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":80/");
	}

	@Test
	public void test_mgmtBasePath_mgmtPortPath() {
		webflux.setBasePath("/app");
		management.setBasePath("/mgnt");
		when(pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/actuator/health");
		publishApplicationReadyEvent(factory, 8080, 8081);

		Application app = factory.createApplication();
		assertThat(app.getManagementUrl()).isEqualTo("http://" + getHostname() + ":8081/mgnt/actuator");
		assertThat(app.getHealthUrl()).isEqualTo("http://" + getHostname() + ":8081/mgnt/actuator/health");
		assertThat(app.getServiceUrl()).isEqualTo("http://" + getHostname() + ":8080/app");
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
			when(server.getPort()).thenReturn(port);
			when(context.getServerNamespace()).thenReturn(name);
		}

		@Override
		public WebServerApplicationContext getApplicationContext() {
			return context;
		}

		@Override
		public WebServer getWebServer() {
			return this.server;
		}

	}

}
