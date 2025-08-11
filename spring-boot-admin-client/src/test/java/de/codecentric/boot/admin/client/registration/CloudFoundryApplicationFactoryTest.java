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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;

import de.codecentric.boot.admin.client.config.CloudFoundryApplicationProperties;
import de.codecentric.boot.admin.client.config.InstanceProperties;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CloudFoundryApplicationFactoryTest {

	private final InstanceProperties instanceProperties = new InstanceProperties();

	private final ServerProperties server = new ServerProperties();

	private final ManagementServerProperties management = new ManagementServerProperties();

	private final PathMappedEndpoints pathMappedEndpoints = mock(PathMappedEndpoints.class);

	private final WebEndpointProperties webEndpoint = new WebEndpointProperties();

	private final CloudFoundryApplicationProperties cfApplicationProperties = new CloudFoundryApplicationProperties();

	private final CloudFoundryApplicationFactory factory = new CloudFoundryApplicationFactory(this.instanceProperties,
			this.management, this.server, this.pathMappedEndpoints, this.webEndpoint,
			() -> singletonMap("contributor", "test"), this.cfApplicationProperties);

	@BeforeEach
	void setup() {
		this.instanceProperties.setName("test");
	}

	@Test
	void should_use_application_uri() {
		when(this.pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/actuator/health");
		this.cfApplicationProperties.setUris(singletonList("application/Uppercase"));

		final Application app = this.factory.createApplication();

		final SoftAssertions softly = new SoftAssertions();
		softly.assertThat(app.getManagementUrl()).isEqualTo("http://application/Uppercase/actuator");
		softly.assertThat(app.getHealthUrl()).isEqualTo("http://application/Uppercase/actuator/health");
		softly.assertThat(app.getServiceUrl()).isEqualTo("http://application/Uppercase/");
		softly.assertAll();
	}

	@Test
	void should_use_service_base_uri() {
		when(this.pathMappedEndpoints.getPath(EndpointId.of("health"))).thenReturn("/actuator/health");
		this.cfApplicationProperties.setUris(singletonList("application/Uppercase"));
		this.instanceProperties.setServiceBaseUrl("https://serviceBaseUrl");

		final Application app = this.factory.createApplication();

		final SoftAssertions softly = new SoftAssertions();
		softly.assertThat(app.getManagementUrl()).isEqualTo("https://serviceBaseUrl/actuator");
		softly.assertThat(app.getHealthUrl()).isEqualTo("https://serviceBaseUrl/actuator/health");
		softly.assertThat(app.getServiceUrl()).isEqualTo("https://serviceBaseUrl/");
		softly.assertAll();
	}

}
