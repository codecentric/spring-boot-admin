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
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.mock.web.MockServletContext;

import de.codecentric.boot.admin.client.config.InstanceProperties;
import de.codecentric.boot.admin.client.config.ServiceHostType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InetUtilsApplicationFactoryTest {

	private final InstanceProperties instance = new InstanceProperties();

	private final ServerProperties server = new ServerProperties();

	private final ManagementServerProperties management = new ManagementServerProperties();

	private final PathMappedEndpoints pathMappedEndpoints = mock(PathMappedEndpoints.class);

	private final WebEndpointProperties webEndpoint = new WebEndpointProperties();

	private final InetUtils inetUtils = mock(InetUtils.class);

	@BeforeEach
	void setup() {
		instance.setServiceHostType(ServiceHostType.IP);
		management.setBasePath("/management");
		when(pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/actuator/health");
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void uses_preferred_address(WebApplicationType type) throws UnknownHostException {
		InetAddress address = InetAddress.getByAddress("preferred-host", new byte[] { (byte) 192, 0, 2, 10 });
		when(inetUtils.findFirstNonLoopbackAddress()).thenReturn(address);
		DefaultApplicationFactory factory = createFactory(type);
		publishPort(factory, "server", 8080);
		publishPort(factory, "management", 9090);

		Application application = factory.createApplication();
		assertThat(application.getServiceUrl()).isEqualTo("http://192.0.2.10:8080/app");
		assertThat(application.getManagementUrl()).isEqualTo("http://192.0.2.10:9090/management/actuator");
		assertThat(application.getHealthUrl()).isEqualTo("http://192.0.2.10:9090/management/actuator/health");
	}

	@ParameterizedTest
	@MethodSource("hostTypes")
	void preserves_host_type(WebApplicationType type, ServiceHostType hostType, String host) {
		InetAddress address = mock(InetAddress.class);
		when(address.getHostAddress()).thenReturn("192.0.2.10");
		when(address.getHostName()).thenReturn("preferred-host");
		when(address.getCanonicalHostName()).thenReturn("preferred-host.example");
		when(inetUtils.findFirstNonLoopbackAddress()).thenReturn(address);
		instance.setServiceHostType(hostType);
		DefaultApplicationFactory factory = createFactory(type);
		publishPort(factory, "server", 8080);

		assertThat(factory.createApplication().getServiceUrl()).isEqualTo("http://" + host + ":8080/app");
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void falls_back_when_no_address_is_available(WebApplicationType type) throws UnknownHostException {
		assertThat(createFactory(type).getLocalHost()).isEqualTo(InetAddress.getLocalHost());
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void preserves_host_resolution_failure(WebApplicationType type) {
		DefaultApplicationFactory factory = createFactory(type);
		UnknownHostException failure = new UnknownHostException("Local host unavailable");
		try (MockedStatic<InetAddress> addresses = mockStatic(InetAddress.class)) {
			addresses.when(InetAddress::getLocalHost).thenThrow(failure);
			assertThatThrownBy(factory::getLocalHost).isInstanceOf(IllegalArgumentException.class)
				.hasMessage(failure.getMessage())
				.hasCause(failure);
		}
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void preserves_explicit_addresses(WebApplicationType type) throws UnknownHostException {
		server.setAddress(InetAddress.getByName("127.0.0.1"));
		management.setAddress(InetAddress.getByName("127.0.0.2"));
		DefaultApplicationFactory factory = createFactory(type);
		publishPort(factory, "server", 8080);
		publishPort(factory, "management", 9090);

		Application application = factory.createApplication();
		assertThat(application.getServiceUrl()).isEqualTo("http://127.0.0.1:8080/app");
		assertThat(application.getManagementUrl()).isEqualTo("http://127.0.0.2:9090/management/actuator");
		assertThat(application.getHealthUrl()).isEqualTo("http://127.0.0.2:9090/management/actuator/health");
		verifyNoInteractions(inetUtils);
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void preserves_explicit_management_address(WebApplicationType type) throws UnknownHostException {
		when(inetUtils.findFirstNonLoopbackAddress())
			.thenReturn(InetAddress.getByAddress(new byte[] { (byte) 192, 0, 2, 10 }));
		management.setAddress(InetAddress.getByName("127.0.0.2"));
		DefaultApplicationFactory factory = createFactory(type);
		publishPort(factory, "server", 8080);
		publishPort(factory, "management", 9090);

		Application application = factory.createApplication();
		assertThat(application.getServiceUrl()).isEqualTo("http://192.0.2.10:8080/app");
		assertThat(application.getManagementUrl()).isEqualTo("http://127.0.0.2:9090/management/actuator");
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void preserves_explicit_urls(WebApplicationType type) {
		instance.setServiceUrl("https://service.example");
		instance.setManagementUrl("https://management.example");
		instance.setHealthUrl("https://health.example");

		Application application = createFactory(type).createApplication();
		assertThat(application.getServiceUrl()).isEqualTo("https://service.example");
		assertThat(application.getManagementUrl()).isEqualTo("https://management.example");
		assertThat(application.getHealthUrl()).isEqualTo("https://health.example");
		verifyNoInteractions(inetUtils);
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void preserves_base_urls(WebApplicationType type) {
		instance.setServiceBaseUrl("https://service.example");
		instance.setManagementBaseUrl("https://management.example");

		Application application = createFactory(type).createApplication();
		assertThat(application.getServiceUrl()).isEqualTo("https://service.example/app");
		assertThat(application.getManagementUrl()).isEqualTo("https://management.example/actuator");
		assertThat(application.getHealthUrl()).isEqualTo("https://management.example/actuator/health");
		verifyNoInteractions(inetUtils);
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void preserves_paths_on_shared_port(WebApplicationType type) throws UnknownHostException {
		when(inetUtils.findFirstNonLoopbackAddress())
			.thenReturn(InetAddress.getByAddress(new byte[] { (byte) 192, 0, 2, 10 }));
		DefaultApplicationFactory factory = createFactory(type);
		publishPort(factory, "server", 8080);
		String managementBaseUrl = "http://192.0.2.10:8080/app"
				+ ((type == WebApplicationType.SERVLET) ? "/dispatcher/management" : "");

		Application application = factory.createApplication();
		assertThat(application.getServiceUrl()).isEqualTo("http://192.0.2.10:8080/app");
		assertThat(application.getManagementUrl()).isEqualTo(managementBaseUrl + "/actuator");
		assertThat(application.getHealthUrl()).isEqualTo(managementBaseUrl + "/actuator/health");
	}

	@ParameterizedTest
	@EnumSource(value = WebApplicationType.class, names = { "SERVLET", "REACTIVE" })
	void preserves_management_ssl(WebApplicationType type) throws UnknownHostException {
		when(inetUtils.findFirstNonLoopbackAddress())
			.thenReturn(InetAddress.getByAddress(new byte[] { (byte) 192, 0, 2, 10 }));
		management.setSsl(new Ssl());
		management.getSsl().setEnabled(true);
		DefaultApplicationFactory factory = createFactory(type);
		publishPort(factory, "server", 8080);
		publishPort(factory, "management", 9090);

		Application application = factory.createApplication();
		assertThat(application.getServiceUrl()).isEqualTo("http://192.0.2.10:8080/app");
		assertThat(application.getManagementUrl()).isEqualTo("https://192.0.2.10:9090/management/actuator");
		assertThat(application.getHealthUrl()).isEqualTo("https://192.0.2.10:9090/management/actuator/health");
	}

	private DefaultApplicationFactory createFactory(WebApplicationType type) {
		if (type == WebApplicationType.SERVLET) {
			MockServletContext servletContext = new MockServletContext();
			servletContext.setContextPath("/app");
			DispatcherServletPath dispatcherServletPath = mock(DispatcherServletPath.class);
			when(dispatcherServletPath.getPrefix()).thenReturn("/dispatcher");
			return new InetUtilsServletApplicationFactory(instance, management, server, servletContext,
					pathMappedEndpoints, webEndpoint, Collections::emptyMap, dispatcherServletPath, inetUtils);
		}
		WebFluxProperties webFluxProperties = new WebFluxProperties();
		webFluxProperties.setBasePath("/app");
		return new InetUtilsReactiveApplicationFactory(instance, management, server, pathMappedEndpoints, webEndpoint,
				Collections::emptyMap, webFluxProperties, inetUtils);
	}

	private void publishPort(DefaultApplicationFactory factory, String namespace, int port) {
		WebServerInitializedEvent event = mock(WebServerInitializedEvent.class);
		WebServerApplicationContext context = mock(WebServerApplicationContext.class);
		WebServer webServer = mock(WebServer.class);
		when(context.getServerNamespace()).thenReturn(namespace);
		when(webServer.getPort()).thenReturn(port);
		when(event.getApplicationContext()).thenReturn(context);
		when(event.getWebServer()).thenReturn(webServer);
		factory.onWebServerInitialized(event);
	}

	private static Stream<Arguments> hostTypes() {
		return Stream.of(WebApplicationType.SERVLET, WebApplicationType.REACTIVE)
			.flatMap((type) -> Stream.of(Arguments.of(type, ServiceHostType.IP, "192.0.2.10"),
					Arguments.of(type, ServiceHostType.HOST_NAME, "preferred-host"),
					Arguments.of(type, ServiceHostType.CANONICAL_HOST_NAME, "preferred-host.example")));
	}

}
