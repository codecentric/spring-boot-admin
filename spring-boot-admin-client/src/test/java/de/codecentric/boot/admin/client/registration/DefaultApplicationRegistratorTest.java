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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultApplicationRegistratorTest {

	private final Application application = Application.create("AppName")
		.managementUrl("http://localhost:8080/mgmt")
		.healthUrl("http://localhost:8080/health")
		.serviceUrl("http://localhost:8080")
		.build();

	private final RegistrationClient registrationClient = mock(RegistrationClient.class);

	@Test
	void register_should_return_true_when_successful() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		when(this.registrationClient.register(any(), eq(this.application))).thenReturn("-id-");
		assertThat(registrator.register()).isTrue();
		assertThat(registrator.getRegisteredId()).isEqualTo("-id-");
	}

	@Test
	void register_should_return_false_when_failed() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		when(this.registrationClient.register(any(), eq(this.application))).thenThrow(new RestClientException("Error"));

		assertThat(registrator.register()).isFalse();
		assertThat(registrator.register()).isFalse();
		assertThat(registrator.getRegisteredId()).isNull();
	}

	@Test
	void register_should_try_next_on_error() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		when(this.registrationClient.register("http://sba:8080/instances", this.application))
			.thenThrow(new RestClientException("Error"));
		when(this.registrationClient.register("http://sba2:8080/instances", this.application)).thenReturn("-id-");

		assertThat(registrator.register()).isTrue();
		assertThat(registrator.getRegisteredId()).isEqualTo("-id-");
	}

	@Test
	void deregister_should_deregister_at_server() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		when(this.registrationClient.register(any(), eq(this.application))).thenReturn("-id-");

		registrator.register();
		registrator.deregister();
		assertThat(registrator.getRegisteredId()).isNull();

		verify(this.registrationClient).deregister("http://sba:8080/instances", "-id-");
	}

	@Test
	void deregister_should_not_deregister_when_not_registered() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		registrator.deregister();

		verify(this.registrationClient, never()).deregister(any(), any());
	}

	@Test
	void deregister_should_try_next_on_error() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				true);

		when(this.registrationClient.register(any(), eq(this.application))).thenReturn("-id-");
		doThrow(new RestClientException("Error")).when(this.registrationClient)
			.deregister("http://sba:8080/instances", "-id-");

		registrator.register();
		registrator.deregister();
		assertThat(registrator.getRegisteredId()).isNull();

		verify(this.registrationClient).deregister("http://sba:8080/instances", "-id-");
		verify(this.registrationClient).deregister("http://sba2:8080/instances", "-id-");
	}

	@Test
	void register_should_register_on_multiple_servers() {
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { "http://sba:8080/instances", "http://sba2:8080/instances" },
				false);

		when(this.registrationClient.register(any(), eq(this.application))).thenReturn("-id-");

		assertThat(registrator.register()).isTrue();
		assertThat(registrator.getRegisteredId()).isEqualTo("-id-");

		verify(this.registrationClient).register("http://sba:8080/instances", this.application);
		verify(this.registrationClient).register("http://sba2:8080/instances", this.application);
	}

	@Test
	void deregister_should_use_the_id_returned_by_each_server() {
		String first = "http://sba:8080/instances";
		String second = "http://sba2:8080/instances";
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { first, second }, false);
		when(this.registrationClient.register(first, this.application)).thenReturn("first-id");
		when(this.registrationClient.register(second, this.application)).thenReturn("second-id", "new-second-id");

		registrator.register();
		registrator.register();
		registrator.deregister();

		verify(this.registrationClient).deregister(first, "first-id");
		verify(this.registrationClient).deregister(second, "new-second-id");
		assertThat(registrator.getRegisteredId()).isNull();
	}

	@Test
	void deregister_should_retry_only_the_server_that_failed() {
		String first = "http://sba:8080/instances";
		String second = "http://sba2:8080/instances";
		ApplicationRegistrator registrator = new DefaultApplicationRegistrator(() -> this.application,
				this.registrationClient, new String[] { first, second }, false);
		when(this.registrationClient.register(first, this.application)).thenReturn("first-id");
		when(this.registrationClient.register(second, this.application)).thenReturn("second-id");
		doThrow(new RestClientException("Error")).doNothing()
			.when(this.registrationClient)
			.deregister(second, "second-id");

		registrator.register();
		registrator.deregister();
		assertThat(registrator.getRegisteredId()).isEqualTo("second-id");
		registrator.deregister();

		verify(this.registrationClient).deregister(first, "first-id");
		verify(this.registrationClient, times(2)).deregister(second, "second-id");
		assertThat(registrator.getRegisteredId()).isNull();
	}

}
