/*
 * Copyright 2014-2026 the original author or authors.
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

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;
import org.springframework.mock.web.MockServletContext;

import de.codecentric.boot.admin.client.config.InstanceProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServletContainerApplicationFactoryTest {

	private final InstanceProperties instance = new InstanceProperties();

	private final MockServletContext servletContext = new MockServletContext();

	private final PathMappedEndpoints pathMappedEndpoints = mock(PathMappedEndpoints.class);

	private final WebEndpointProperties webEndpoint = new WebEndpointProperties();

	private final DispatcherServletPath dispatcherServletPath = mock(DispatcherServletPath.class);

	private final ServletContainerApplicationFactory factory = new ServletContainerApplicationFactory(instance,
			servletContext, pathMappedEndpoints, webEndpoint, () -> Collections.singletonMap("contributor", "1"),
			dispatcherServletPath);

	@BeforeEach
	void setup() {
		instance.setName("test");
		instance.setServiceBaseUrl("http://container:8080");
		servletContext.setContextPath("/app");
		when(dispatcherServletPath.getPrefix()).thenReturn("");
		when(pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/actuator/health");
	}

	@Test
	void test_serviceBaseUrl_contextPath() {
		instance.getMetadata().put("instance", "2");

		Application app = factory.createApplication();
		assertThat(app.getName()).isEqualTo("test");
		assertThat(app.getServiceUrl()).isEqualTo("http://container:8080/app");
		assertThat(app.getManagementUrl()).isEqualTo("http://container:8080/app/actuator");
		assertThat(app.getHealthUrl()).isEqualTo("http://container:8080/app/actuator/health");
		assertThat(app.getMetadata()).containsEntry("contributor", "1").containsEntry("instance", "2");
	}

	@Test
	void test_servicePath_servletPath() {
		instance.setServicePath("/servicePath/");
		when(dispatcherServletPath.getPrefix()).thenReturn("srv");

		Application app = factory.createApplication();
		assertThat(app.getServiceUrl()).isEqualTo("http://container:8080/servicePath/app");
		assertThat(app.getManagementUrl()).isEqualTo("http://container:8080/servicePath/app/srv/actuator");
		assertThat(app.getHealthUrl()).isEqualTo("http://container:8080/servicePath/app/srv/actuator/health");
	}

	@Test
	void test_managementBaseUrl() {
		instance.setManagementBaseUrl("http://container:9090/mgmt");

		Application app = factory.createApplication();
		assertThat(app.getServiceUrl()).isEqualTo("http://container:8080/app");
		assertThat(app.getManagementUrl()).isEqualTo("http://container:9090/mgmt/actuator");
		assertThat(app.getHealthUrl()).isEqualTo("http://container:9090/mgmt/actuator/health");
	}

	@Test
	void test_missing_serviceBaseUrl() {
		instance.setServiceBaseUrl(null);

		assertThatIllegalStateException().isThrownBy(factory::createApplication)
			.withMessageContaining("service-base-url");
	}

}
