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

package de.codecentric.boot.admin.server.web.client;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider.InstanceCredentials;

import static org.assertj.core.api.Assertions.assertThat;

class BasicAuthHttpHeaderProviderTest {

	private final BasicAuthHttpHeaderProvider headersProvider = new BasicAuthHttpHeaderProvider();

	private final BasicAuthHttpHeaderProvider headersProviderEnableInstanceAuth = new BasicAuthHttpHeaderProvider(
			"client", "client", Collections.singletonMap("sb-admin-server", new InstanceCredentials("admin", "admin")));

	@Test
	void test_auth_header() {
		Registration registration = Registration.create("foo", "https://health")
			.metadata("user.name", "test")
			.metadata("user.password", "drowssap")
			.build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProvider.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic dGVzdDpkcm93c3NhcA==");
	}

	@Test
	void test_auth_header_with_dashes() {
		Registration registration = Registration.create("foo", "https://health")
			.metadata("user-name", "test")
			.metadata("user-password", "drowssap")
			.build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProvider.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic dGVzdDpkcm93c3NhcA==");
	}

	@Test
	void test_auth_header_no_separator() {
		Registration registration = Registration.create("foo", "https://health")
			.metadata("username", "test")
			.metadata("userpassword", "drowssap")
			.build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProvider.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic dGVzdDpkcm93c3NhcA==");
	}

	@Test
	void test_no_header() {
		Registration registration = Registration.create("foo", "https://health").build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProvider.getHeaders(instance).toSingleValueMap()).isEmpty();
	}

	@Test
	void test_auth_instance_enabled_use_default_creds() {
		Registration registration = Registration.create("foo", "https://health").name("xyz-server").build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProviderEnableInstanceAuth.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic Y2xpZW50OmNsaWVudA==");
	}

	@Test
	void test_auth_instance_enabled_use_service_specific_creds() {
		Registration registration = Registration.create("foo", "https://health").name("sb-admin-server").build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProviderEnableInstanceAuth.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic YWRtaW46YWRtaW4=");
	}

	@Test
	void test_auth_instance_enabled_use_metadata_over_props() {
		Registration registration = Registration.create("foo", "https://health")
			.metadata("username", "test")
			.metadata("userpassword", "drowssap")
			.name("xyz-server")
			.build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProviderEnableInstanceAuth.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic dGVzdDpkcm93c3NhcA==");
	}

	@Test
	void isAllowedFor_respects_allowed_hosts() {
		BasicAuthHttpHeaderProvider provider = new BasicAuthHttpHeaderProvider("client", "client",
				Map.of("sb-admin-server", new InstanceCredentials("admin", "admin", List.of("*.svc.cluster.local"))),
				List.of("default.example.com"));

		Instance service = Instance.create(InstanceId.of("id"))
			.register(Registration.create("sb-admin-server", "https://health").build());
		assertThat(provider.isAllowedFor(service, URI.create("http://a.svc.cluster.local/health"))).isTrue();
		assertThat(provider.isAllowedFor(service, URI.create("https://attacker.example/health"))).isFalse();

		Instance other = Instance.create(InstanceId.of("id"))
			.register(Registration.create("xyz-server", "https://health").build());
		assertThat(provider.isAllowedFor(other, URI.create("https://default.example.com/health"))).isTrue();
		assertThat(provider.isAllowedFor(other, URI.create("https://attacker.example/health"))).isFalse();

		Instance metadata = Instance.create(InstanceId.of("id"))
			.register(Registration.create("sb-admin-server", "https://health")
				.metadata("username", "test")
				.metadata("userpassword", "drowssap")
				.build());
		assertThat(provider.isAllowedFor(metadata, URI.create("https://attacker.example/health"))).isTrue();
	}

	@Test
	void reports_credentials_without_allowed_hosts() {
		BasicAuthHttpHeaderProvider provider = new BasicAuthHttpHeaderProvider("client", "client",
				Map.of("bound", new InstanceCredentials("a", "a", List.of("bound.example.com")), "unbound",
						new InstanceCredentials("b", "b")));
		assertThat(provider.getCredentialsWithoutAllowedHosts()).containsExactly("default", "unbound");
		assertThat(this.headersProvider.getCredentialsWithoutAllowedHosts()).isEmpty();
	}

}
