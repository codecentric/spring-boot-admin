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

package de.codecentric.boot.admin.server.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.BuildVersion;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationRegistryTest {

	private InstanceRegistry instanceRegistry;

	private ApplicationRegistry applicationRegistry;

	@BeforeEach
	public void setUp() {
		this.instanceRegistry = mock(InstanceRegistry.class);
		InstanceEventPublisher instanceEventPublisher = mock(InstanceEventPublisher.class);
		this.applicationRegistry = new ApplicationRegistry(this.instanceRegistry, instanceEventPublisher);
	}

	@Test
	public void getApplications_noRegisteredApplications() {
		when(this.instanceRegistry.getInstances()).thenReturn(Flux.just());

		StepVerifier.create(this.applicationRegistry.getApplications()).verifyComplete();
	}

	@Test
	public void getApplications_oneRegisteredAndOneUnregisteredApplication() {
		Instance instance1 = getInstance("App1");
		Instance instance2 = getInstance("App2").deregister();

		when(this.instanceRegistry.getInstances()).thenReturn(Flux.just(instance1, instance2));

		StepVerifier.create(this.applicationRegistry.getApplications())
			.assertNext((app) -> assertThat(app.getName()).isEqualTo("App1"))
			.verifyComplete();
	}

	@Test
	public void getApplications_allRegisteredApplications() {
		Instance instance1 = getInstance("App1");
		Instance instance2 = getInstance("App2");

		when(this.instanceRegistry.getInstances()).thenReturn(Flux.just(instance1, instance2));

		StepVerifier.create(this.applicationRegistry.getApplications())
			.recordWith(ArrayList::new)
			.thenConsumeWhile((a) -> true)
			.consumeRecordedWith((applications) -> assertThat(applications.stream().map(Application::getName))
				.containsExactlyInAnyOrder("App1", "App2"))
			.verifyComplete();
	}

	@Test
	public void getApplication_noRegisteredApplications() {
		when(this.instanceRegistry.getInstances(any(String.class))).thenReturn(Flux.just());

		StepVerifier.create(this.applicationRegistry.getApplication("App1")).verifyComplete();
	}

	@Test
	public void getApplication_noMatchingRegisteredApplications() {
		when(this.instanceRegistry.getInstances("App2")).thenReturn(Flux.just(getInstance("App2")));
		when(this.instanceRegistry.getInstances(any(String.class))).thenReturn(Flux.just());

		StepVerifier.create(this.applicationRegistry.getApplication("App1")).verifyComplete();
	}

	@Test
	public void getApplication_matchingUnregisteredApplications() {
		Instance instance = getInstance("App1").deregister();
		when(this.instanceRegistry.getInstances("App1")).thenReturn(Flux.just(instance));

		StepVerifier.create(this.applicationRegistry.getApplication("App1")).verifyComplete();
	}

	@Test
	public void getApplication_matchingRegisteredApplications() {
		Instance instance = getInstance("App1");
		when(this.instanceRegistry.getInstances("App1")).thenReturn(Flux.just(instance));

		StepVerifier.create(this.applicationRegistry.getApplication("App1"))
			.assertNext((app) -> assertThat(app.getName()).isEqualTo("App1"))
			.verifyComplete();
	}

	@Test
	public void deregister() {
		Instance instance1 = getInstance("App1");
		InstanceId instance1Id = instance1.getId();

		when(this.instanceRegistry.getInstances("App1")).thenReturn(Flux.just(instance1));
		when(this.instanceRegistry.deregister(instance1Id)).thenReturn(Mono.just(instance1Id));

		StepVerifier.create(this.applicationRegistry.deregister("App1"))
			.assertNext((instanceId) -> assertThat(instanceId).isEqualTo(instance1Id))
			.verifyComplete();

		verify(this.instanceRegistry).deregister(instance1Id);
	}

	@Test
	public void getBuildVersion() {
		Instance instance1 = getInstance("App1", "0.1");
		Instance instance2 = getInstance("App2", "0.2");

		// Empty list should return null:
		assertThat(this.applicationRegistry.getBuildVersion(Collections.emptyList())).isNull();

		// Single instance should return the version number:
		assertThat(this.applicationRegistry.getBuildVersion(Collections.singletonList(instance1)))
			.isEqualTo(BuildVersion.valueOf("0.1"));

		// Multiple instances should return the version number range:
		assertThat(this.applicationRegistry.getBuildVersion(Arrays.asList(instance1, instance2)))
			.isEqualTo(BuildVersion.valueOf("0.1 ... 0.2"));
	}

	@ParameterizedTest
	@CsvSource({ "UP, UP, UP", "DOWN, DOWN, DOWN", "UNKNOWN, UNKNOWN, UNKNOWN", "UP, DOWN, RESTRICTED",
			"UP, UNKNOWN, RESTRICTED", "UP, OUT_OF_SERVICE, RESTRICTED", "UP, OFFLINE, RESTRICTED",
			"UP, RESTRICTED, RESTRICTED", "DOWN, UP, RESTRICTED" })
	public void getStatus(String instance1Status, String instance2Status, String expectedApplicationStatus) {
		Instance instance1 = getInstance("App1").withStatusInfo(StatusInfo.valueOf(instance1Status));
		Instance instance2 = getInstance("App1").withStatusInfo(StatusInfo.valueOf(instance2Status));

		when(this.instanceRegistry.getInstances()).thenReturn(Flux.just(instance1, instance2));

		StepVerifier.create(this.applicationRegistry.getApplications())
			.recordWith(ArrayList::new)
			.thenConsumeWhile((a) -> true)
			.consumeRecordedWith((applications) -> assertThat(applications.stream().map(Application::getStatus))
				.containsExactly(expectedApplicationStatus))
			.verifyComplete();
	}

	private Instance getInstance(String applicationName, String version) {
		Registration registration = Registration.create(applicationName, "http://localhost:8080/health")
			.metadata("version", version)
			.build();
		InstanceId id = InstanceId.of("TEST" + applicationName);
		return Instance.create(id).register(registration);
	}

	private Instance getInstance(String applicationName) {
		return getInstance(applicationName, "FooBarVersion");
	}

}
