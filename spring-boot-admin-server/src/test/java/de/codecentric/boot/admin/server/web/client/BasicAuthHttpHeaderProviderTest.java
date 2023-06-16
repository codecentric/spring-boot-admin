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

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider.InstanceCredentials;

import static org.assertj.core.api.Assertions.assertThat;

public class BasicAuthHttpHeaderProviderTest {

	private final BasicAuthHttpHeaderProvider headersProvider = new BasicAuthHttpHeaderProvider();

	private final BasicAuthHttpHeaderProvider headersProviderEnableInstanceAuth = new BasicAuthHttpHeaderProvider(
			"client", "client", Collections.singletonMap("sb-admin-server", new InstanceCredentials("admin", "admin")));

	@Test
	public void test_auth_header() {
		Registration registration = Registration.create("foo", "http://health")
			.metadata("user.name", "test")
			.metadata("user.password", "drowssap")
			.build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProvider.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic dGVzdDpkcm93c3NhcA==");
	}

	@Test
	public void test_auth_header_with_dashes() {
		Registration registration = Registration.create("foo", "http://health")
			.metadata("user-name", "test")
			.metadata("user-password", "drowssap")
			.build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProvider.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic dGVzdDpkcm93c3NhcA==");
	}

	@Test
	public void test_auth_header_no_separator() {
		Registration registration = Registration.create("foo", "http://health")
			.metadata("username", "test")
			.metadata("userpassword", "drowssap")
			.build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProvider.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic dGVzdDpkcm93c3NhcA==");
	}

	@Test
	public void test_no_header() {
		Registration registration = Registration.create("foo", "http://health").build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProvider.getHeaders(instance)).isEmpty();
	}

	@Test
	public void test_auth_instance_enabled_use_default_creds() {
		Registration registration = Registration.create("foo", "http://health").name("xyz-server").build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProviderEnableInstanceAuth.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic Y2xpZW50OmNsaWVudA==");
	}

	@Test
	public void test_auth_instance_enabled_use_service_specific_creds() {
		Registration registration = Registration.create("foo", "http://health").name("sb-admin-server").build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProviderEnableInstanceAuth.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic YWRtaW46YWRtaW4=");
	}

	@Test
	public void test_auth_instance_enabled_use_metadata_over_props() {
		Registration registration = Registration.create("foo", "http://health")
			.metadata("username", "test")
			.metadata("userpassword", "drowssap")
			.name("xyz-server")
			.build();
		Instance instance = Instance.create(InstanceId.of("id")).register(registration);
		assertThat(this.headersProviderEnableInstanceAuth.getHeaders(instance).get(HttpHeaders.AUTHORIZATION))
			.containsOnly("Basic dGVzdDpkcm93c3NhcA==");
	}

}
