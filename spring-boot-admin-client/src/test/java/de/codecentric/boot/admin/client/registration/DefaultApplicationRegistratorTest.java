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

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultApplicationRegistratorTest {

	private final Application application = Application.create("AppName")
		.managementUrl("http://localhost:8080/mgmt")
		.healthUrl("http://localhost:8080/health")
		.serviceUrl("http://localhost:8080")
		.build();

	private final RegistrationClient registrationClient = mock(RegistrationClient.class);

	@Test
	public void register_should_return_true_when_successful() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		when(this.registrationClient.register(any(), eq(this.application))).thenReturn(Optional.of("-id-"));
		assertThat(registrator.register()).isTrue();
		assertThat(registrator.getRegisteredId()).isEqualTo("-id-");
	}

	@Test
	public void register_should_return_false_when_failed() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		when(this.registrationClient.register(any(), eq(this.application))).thenThrow(new RestClientException("Error"));

		assertThat(registrator.register()).isFalse();
		assertThat(registrator.register()).isFalse();
		assertThat(registrator.getRegisteredId()).isNull();
	}

	@Test
	public void register_should_try_next_on_error() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		when(this.registrationClient.register("http://sba:8080/instances", this.application))
			.thenThrow(new RestClientException("Error"));
		when(this.registrationClient.register("http://sba2:8080/instances", this.application))
			.thenReturn(Optional.of("-id-"));

		assertThat(registrator.register()).isTrue();
		assertThat(registrator.getRegisteredId()).isEqualTo("-id-");
	}

	@Test
	public void deregister_should_deregister_at_server() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		when(this.registrationClient.register(any(), eq(this.application))).thenReturn(Optional.of("-id-"));

		registrator.register();
		registrator.deregister();
		assertThat(registrator.getRegisteredId()).isNull();

		verify(this.registrationClient).deregister("http://sba:8080/instances", "-id-");
	}

	@Test
	public void deregister_should_not_deregister_when_not_registered() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		registrator.deregister();

		verify(this.registrationClient, never()).deregister(any(), any());
	}

	@Test
	public void deregister_should_try_next_on_error() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		when(this.registrationClient.register(any(), eq(this.application))).thenReturn(Optional.of("-id-"));
		doThrow(new RestClientException("Error")).when(this.registrationClient)
			.deregister("http://sba:8080/instances", "-id-");

		registrator.register();
		registrator.deregister();
		assertThat(registrator.getRegisteredId()).isNull();

		verify(this.registrationClient).deregister("http://sba:8080/instances", "-id-");
		verify(this.registrationClient).deregister("http://sba2:8080/instances", "-id-");
	}

	@Test
	public void register_should_register_on_multiple_servers() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				false);

		when(this.registrationClient.register(any(), eq(this.application))).thenReturn(Optional.of("-id-"));

		assertThat(registrator.register()).isTrue();
		assertThat(registrator.getRegisteredId()).isEqualTo("-id-");

		verify(this.registrationClient).register("http://sba:8080/instances", this.application);
		verify(this.registrationClient).register("http://sba2:8080/instances", this.application);
	}

	@Test
	public void deregister_should_deregister_on_multiple_servers() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				false);

		when(this.registrationClient.register(any(), eq(this.application))).thenReturn(Optional.of("-id-"));

		registrator.register();
		registrator.deregister();
		assertThat(registrator.getRegisteredId()).isNull();

		verify(this.registrationClient).deregister("http://sba:8080/instances", "-id-");
		verify(this.registrationClient).deregister("http://sba2:8080/instances", "-id-");
	}

}
