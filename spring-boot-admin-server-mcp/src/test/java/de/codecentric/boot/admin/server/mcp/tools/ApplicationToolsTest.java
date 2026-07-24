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

package de.codecentric.boot.admin.server.mcp.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationToolsTest {

	private InstanceRepository instanceRepository;

	private ApplicationTools applicationTools;

	@BeforeEach
	void setUp() {
		this.instanceRepository = mock(InstanceRepository.class);
		this.applicationTools = new ApplicationTools(this.instanceRepository);
	}

	@Test
	void listApplications_withRegisteredApps_returnsFormattedList() {
		Instance instance1 = Instance.create(InstanceId.of("id-1"))
			.register(Registration.create("payment-service", "http://payment/actuator/health")
				.managementUrl("http://payment/actuator")
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		Instance instance2 = Instance.create(InstanceId.of("id-2"))
			.register(Registration.create("order-service", "http://order/actuator/health")
				.managementUrl("http://order/actuator")
				.build())
			.withStatusInfo(StatusInfo.ofDown());

		when(this.instanceRepository.findAll()).thenReturn(Flux.just(instance1, instance2));

		StepVerifier.create(this.applicationTools.listApplications()).assertNext((result) -> {
			assertThat(result).contains("Registered applications (2)");
			assertThat(result).contains("payment-service");
			assertThat(result).contains("id: id-1");
			assertThat(result).contains("UP");
			assertThat(result).contains("order-service");
			assertThat(result).contains("id: id-2");
			assertThat(result).contains("DOWN");
		}).verifyComplete();
	}

	@Test
	void listApplications_withNoApps_returnsNoAppsMessage() {
		when(this.instanceRepository.findAll()).thenReturn(Flux.empty());

		StepVerifier.create(this.applicationTools.listApplications())
			.assertNext((result) -> assertThat(result).isEqualTo("No applications are currently registered."))
			.verifyComplete();
	}

	@Test
	void listApplications_onRepositoryError_returnsErrorMessage() {
		when(this.instanceRepository.findAll()).thenReturn(Flux.error(new RuntimeException("connection failed")));

		StepVerifier.create(this.applicationTools.listApplications())
			.assertNext((result) -> assertThat(result).isEqualTo("Error retrieving registered applications."))
			.verifyComplete();
	}

}
