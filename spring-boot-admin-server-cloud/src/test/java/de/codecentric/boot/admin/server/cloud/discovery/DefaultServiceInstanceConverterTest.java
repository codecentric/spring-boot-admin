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
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import de.codecentric.boot.admin.server.domain.values.Registration;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultServiceInstanceConverterTest {

	@Test
	public void should_convert_with_defaults() {
		ServiceInstance service = new DefaultServiceInstance("test-1", "test", "localhost", 80, false);
		Registration registration = new DefaultServiceInstanceConverter().convert(service);

		assertThat(registration.getName()).isEqualTo("test");
		assertThat(registration.getServiceUrl()).isEqualTo("http://localhost:80");
		assertThat(registration.getManagementUrl()).isEqualTo("http://localhost:80/actuator");
		assertThat(registration.getHealthUrl()).isEqualTo("http://localhost:80/actuator/health");
	}

	@Test
	public void should_convert_with_custom_defaults() {
		DefaultServiceInstanceConverter converter = new DefaultServiceInstanceConverter();
		converter.setHealthEndpointPath("ping");
		converter.setManagementContextPath("mgmt");

		ServiceInstance service = new DefaultServiceInstance("test-1", "test", "localhost", 80, false);
		Registration registration = converter.convert(service);

		assertThat(registration.getName()).isEqualTo("test");
		assertThat(registration.getServiceUrl()).isEqualTo("http://localhost:80");
		assertThat(registration.getManagementUrl()).isEqualTo("http://localhost:80/mgmt");
		assertThat(registration.getHealthUrl()).isEqualTo("http://localhost:80/mgmt/ping");
	}

	@Test
	public void should_convert_with_metadata() {
		ServiceInstance service = new DefaultServiceInstance("test-1", "test", "localhost", 80, false);
		Map<String, String> metadata = new HashMap<>();
		metadata.put("health.path", "ping");
		metadata.put("management.scheme", "https");
		metadata.put("management.address", "127.0.0.1");
		metadata.put("management.port", "1234");
		metadata.put("management.context-path", "mgmt");
		service.getMetadata().putAll(metadata);

		Registration registration = new DefaultServiceInstanceConverter().convert(service);

		assertThat(registration.getName()).isEqualTo("test");
		assertThat(registration.getServiceUrl()).isEqualTo("http://localhost:80");
		assertThat(registration.getManagementUrl()).isEqualTo("https://127.0.0.1:1234/mgmt");
		assertThat(registration.getHealthUrl()).isEqualTo("https://127.0.0.1:1234/mgmt/ping");
		assertThat(registration.getMetadata()).isEqualTo(metadata);
	}

	// Fix for Issue #2076, #1737
	@Test
	public void should_convert_with_metadata_without_dots() {
		ServiceInstance service = new DefaultServiceInstance("test-1", "test", "localhost", 80, false);
		Map<String, String> metadata = new HashMap<>();
		metadata.put("health-path", "ping");
		metadata.put("management-scheme", "https");
		metadata.put("management-address", "127.0.0.1");
		metadata.put("management-port", "1234");
		metadata.put("management-context-path", "mgmt");
		service.getMetadata().putAll(metadata);

		Registration registration = new DefaultServiceInstanceConverter().convert(service);

		assertThat(registration.getName()).isEqualTo("test");
		assertThat(registration.getServiceUrl()).isEqualTo("http://localhost:80");
		assertThat(registration.getManagementUrl()).isEqualTo("https://127.0.0.1:1234/mgmt");
		assertThat(registration.getHealthUrl()).isEqualTo("https://127.0.0.1:1234/mgmt/ping");
		assertThat(registration.getMetadata()).isEqualTo(metadata);
	}

	@Test
	public void should_convert_with_metadata_having_null_value() {
		ServiceInstance service = new DefaultServiceInstance("test-1", "test", "localhost", 80, false);
		Map<String, String> metadata = new HashMap<>();
		metadata.put("health.path", "ping");
		metadata.put("management.scheme", "https");
		metadata.put("management.address", "127.0.0.1");
		metadata.put("management.port", "1234");
		metadata.put("null.value", null);
		metadata.put(null, "null.key");
		service.getMetadata().putAll(metadata);

		Registration registration = new DefaultServiceInstanceConverter().convert(service);

		assertThat(registration.getHealthUrl()).isEqualTo("https://127.0.0.1:1234/actuator/ping");
	}

	@Test
	public void should_convert_service_with_uri() {
		ServiceInstance service = new TestServiceInstance("test", URI.create("http://localhost/test"),
				Collections.emptyMap());
		Registration registration = new DefaultServiceInstanceConverter().convert(service);

		assertThat(registration.getName()).isEqualTo("test");
		assertThat(registration.getServiceUrl()).isEqualTo("http://localhost/test");
		assertThat(registration.getManagementUrl()).isEqualTo("http://localhost/test/actuator");
		assertThat(registration.getHealthUrl()).isEqualTo("http://localhost/test/actuator/health");
		assertThat(registration.getMetadata()).isEmpty();
	}

	@Test
	public void should_convert_service_with_uri_and_custom_defaults() {
		DefaultServiceInstanceConverter converter = new DefaultServiceInstanceConverter();
		converter.setHealthEndpointPath("ping");
		converter.setManagementContextPath("mgmt");

		ServiceInstance service = new TestServiceInstance("test", URI.create("http://localhost/test"),
				Collections.emptyMap());
		Registration registration = converter.convert(service);

		assertThat(registration.getName()).isEqualTo("test");
		assertThat(registration.getServiceUrl()).isEqualTo("http://localhost/test");
		assertThat(registration.getManagementUrl()).isEqualTo("http://localhost/test/mgmt");
		assertThat(registration.getHealthUrl()).isEqualTo("http://localhost/test/mgmt/ping");
	}

	@Test
	public void should_convert_service_with_uri_and_metadata_different_port() {
		Map<String, String> metadata = new HashMap<>();
		metadata.put("health.path", "ping");
		metadata.put("management.context-path", "mgmt");
		metadata.put("management.port", "1234");
		metadata.put("management.address", "127.0.0.1");
		ServiceInstance service = new TestServiceInstance("test", URI.create("http://localhost/test"), metadata);

		Registration registration = new DefaultServiceInstanceConverter().convert(service);
		assertThat(registration.getName()).isEqualTo("test");
		assertThat(registration.getServiceUrl()).isEqualTo("http://localhost/test");
		assertThat(registration.getManagementUrl()).isEqualTo("http://127.0.0.1:1234/mgmt");
		assertThat(registration.getHealthUrl()).isEqualTo("http://127.0.0.1:1234/mgmt/ping");
		assertThat(registration.getMetadata()).isEqualTo(metadata);
	}

	@Test
	public void should_convert_service_with_uri_and_metadata() {
		Map<String, String> metadata = new HashMap<>();
		metadata.put("health.path", "ping");
		metadata.put("management.context-path", "mgmt");
		ServiceInstance service = new TestServiceInstance("test", URI.create("http://localhost/test"), metadata);

		Registration registration = new DefaultServiceInstanceConverter().convert(service);
		assertThat(registration.getName()).isEqualTo("test");
		assertThat(registration.getServiceUrl()).isEqualTo("http://localhost/test");
		assertThat(registration.getManagementUrl()).isEqualTo("http://localhost/test/mgmt");
		assertThat(registration.getHealthUrl()).isEqualTo("http://localhost/test/mgmt/ping");
		assertThat(registration.getMetadata()).isEqualTo(metadata);
	}

	private static class TestServiceInstance implements ServiceInstance {

		private final String serviceId;

		private final URI uri;

		private final Map<String, String> metadata;

		TestServiceInstance(String serviceId, URI uri, Map<String, String> metadata) {
			this.uri = uri;
			this.serviceId = serviceId;
			this.metadata = metadata;
		}

		@Override
		public String getServiceId() {
			return this.serviceId;
		}

		@Override
		public String getHost() {
			return this.uri.getHost();
		}

		@Override
		public int getPort() {
			if (this.uri.getPort() != -1) {
				return this.uri.getPort();
			}
			return this.isSecure() ? 443 : 80;
		}

		@Override
		public boolean isSecure() {
			return this.uri.getScheme().equals("https");
		}

		@Override
		public URI getUri() {
			return this.uri;
		}

		@Override
		public Map<String, String> getMetadata() {
			return this.metadata;
		}

	}

}
