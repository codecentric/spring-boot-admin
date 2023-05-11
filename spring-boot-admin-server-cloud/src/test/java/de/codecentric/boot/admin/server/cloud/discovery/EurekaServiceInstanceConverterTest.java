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

import com.netflix.appinfo.InstanceInfo;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.netflix.eureka.EurekaServiceInstance;

import de.codecentric.boot.admin.server.domain.values.Registration;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EurekaServiceInstanceConverterTest {

	@Test
	public void convert_secure() {
		InstanceInfo instanceInfo = mock(InstanceInfo.class);
		when(instanceInfo.getSecureHealthCheckUrl()).thenReturn("");
		when(instanceInfo.getHealthCheckUrl()).thenReturn("http://localhost:80/mgmt/ping");
		EurekaServiceInstance service = mock(EurekaServiceInstance.class);
		when(service.getInstanceInfo()).thenReturn(instanceInfo);
		when(service.getUri()).thenReturn(URI.create("http://localhost:80"));
		when(service.getServiceId()).thenReturn("test");
		when(service.getMetadata()).thenReturn(singletonMap("management.context-path", "/mgmt"));

		Registration registration = new EurekaServiceInstanceConverter().convert(service);

		assertThat(registration.getName()).isEqualTo("test");
		assertThat(registration.getServiceUrl()).isEqualTo("http://localhost:80");
		assertThat(registration.getManagementUrl()).isEqualTo("http://localhost:80/mgmt");
		assertThat(registration.getHealthUrl()).isEqualTo("http://localhost:80/mgmt/ping");
	}

	@Test
	public void convert_missing_mgmtpath() {
		InstanceInfo instanceInfo = mock(InstanceInfo.class);
		when(instanceInfo.getHealthCheckUrl()).thenReturn("http://localhost:80/mgmt/ping");
		EurekaServiceInstance service = mock(EurekaServiceInstance.class);
		when(service.getInstanceInfo()).thenReturn(instanceInfo);
		when(service.getUri()).thenReturn(URI.create("http://localhost:80"));
		when(service.getServiceId()).thenReturn("test");

		Registration registration = new EurekaServiceInstanceConverter().convert(service);

		assertThat(registration.getManagementUrl()).isEqualTo("http://localhost:80/actuator");
	}

	@Test
	public void convert_secure_healthUrl() {
		InstanceInfo instanceInfo = mock(InstanceInfo.class);
		when(instanceInfo.getSecureHealthCheckUrl()).thenReturn("https://localhost:80/health");
		EurekaServiceInstance service = mock(EurekaServiceInstance.class);
		when(service.getInstanceInfo()).thenReturn(instanceInfo);
		when(service.getUri()).thenReturn(URI.create("http://localhost:80"));
		when(service.getServiceId()).thenReturn("test");

		Registration registration = new EurekaServiceInstanceConverter().convert(service);

		assertThat(registration.getHealthUrl()).isEqualTo("https://localhost:80/health");
	}

}
