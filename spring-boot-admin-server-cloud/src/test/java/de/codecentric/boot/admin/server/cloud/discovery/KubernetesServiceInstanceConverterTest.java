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

package de.codecentric.boot.admin.server.cloud.discovery;

import java.net.URI;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.kubernetes.commons.discovery.KubernetesDiscoveryProperties;

import de.codecentric.boot.admin.server.domain.values.Registration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KubernetesServiceInstanceConverterTest {

	@Test
	void convert_using_port_mgmt() {
		KubernetesDiscoveryProperties discoveryProperties = KubernetesDiscoveryProperties.DEFAULT;
		ServiceInstance service = mockServiceInstanceWithManagementPort(
				discoveryProperties.metadata().portsPrefix() + "management");

		Registration registration = new KubernetesServiceInstanceConverter(discoveryProperties).convert(service);

		assertThat(registration.getManagementUrl()).isEqualTo("http://localhost:9080/actuator");
		assertThat(registration.getHealthUrl()).isEqualTo("http://localhost:9080/actuator/health");
	}

	@Test
	void fallback_for_port_mgmt() {
		KubernetesDiscoveryProperties discoveryProperties = KubernetesDiscoveryProperties.DEFAULT;
		ServiceInstance service = mockServiceInstanceWithManagementPort("management");

		Registration registration = new KubernetesServiceInstanceConverter(discoveryProperties).convert(service);

		assertThat(registration.getManagementUrl()).isEqualTo("http://localhost:9080/actuator");
		assertThat(registration.getHealthUrl()).isEqualTo("http://localhost:9080/actuator/health");
	}

	private static ServiceInstance mockServiceInstanceWithManagementPort(String managementPortName) {
		ServiceInstance service = mock(ServiceInstance.class);
		when(service.getUri()).thenReturn(URI.create("http://localhost:80"));
		when(service.getServiceId()).thenReturn("test");
		when(service.getMetadata()).thenReturn(Collections.singletonMap(managementPortName, "9080"));
		return service;
	}

}
