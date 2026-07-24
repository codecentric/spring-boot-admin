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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
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

class ApplicationRegistryTest {

	private InstanceRegistry instanceRegistry;

	private ApplicationRegistry applicationRegistry;

	private TestInstanceEventPublisher eventPublisher;

	@BeforeEach
	void setUp() {
		this.instanceRegistry = mock(InstanceRegistry.class);
		this.eventPublisher = new TestInstanceEventPublisher();
		this.applicationRegistry = new ApplicationRegistry(this.instanceRegistry, this.eventPublisher);
	}

	@Test
	void getApplications_noRegisteredApplications() {
		when(this.instanceRegistry.getInstances()).thenReturn(Flux.just());

		StepVerifier.create(this.applicationRegistry.getApplications()).verifyComplete();
	}

	@Test
	void getApplications_oneRegisteredAndOneUnregisteredApplication() {
		Instance instance1 = getInstance("App1");
		Instance instance2 = getInstance("App2").deregister();

		when(this.instanceRegistry.getInstances()).thenReturn(Flux.just(instance1, instance2));

		StepVerifier.create(this.applicationRegistry.getApplications())
			.assertNext((app) -> assertThat(app.getName()).isEqualTo("App1"))
			.verifyComplete();
	}

	@Test
	void getApplications_allRegisteredApplications() {
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
	void getApplication_noRegisteredApplications() {
		when(this.instanceRegistry.getInstances(any(String.class))).thenReturn(Flux.just());

		StepVerifier.create(this.applicationRegistry.getApplication("App1")).verifyComplete();
	}

	@Test
	void getApplication_noMatchingRegisteredApplications() {
		when(this.instanceRegistry.getInstances("App2")).thenReturn(Flux.just(getInstance("App2")));
		when(this.instanceRegistry.getInstances(any(String.class))).thenReturn(Flux.just());

		StepVerifier.create(this.applicationRegistry.getApplication("App1")).verifyComplete();
	}

	@Test
	void getApplication_matchingUnregisteredApplications() {
		Instance instance = getInstance("App1").deregister();
		when(this.instanceRegistry.getInstances("App1")).thenReturn(Flux.just(instance));

		StepVerifier.create(this.applicationRegistry.getApplication("App1")).verifyComplete();
	}

	@Test
	void getApplication_matchingRegisteredApplications() {
		Instance instance = getInstance("App1");
		when(this.instanceRegistry.getInstances("App1")).thenReturn(Flux.just(instance));

		StepVerifier.create(this.applicationRegistry.getApplication("App1"))
			.assertNext((app) -> assertThat(app.getName()).isEqualTo("App1"))
			.verifyComplete();
	}

	@Test
	void deregister() {
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
	void getBuildVersion() {
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
	void getStatus(String instance1Status, String instance2Status, String expectedApplicationStatus) {
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

	@Test
	void getApplicationStream_emitsNewApplicationAndRemovesOldOneWhenInstanceRenamed() {
		// The instance keeps a stable id (based on healthUrl) but changes its name from
		// old to new.
		Registration oldRegistration = Registration.create("old-service", "http://localhost:8080/health").build();
		Registration newRegistration = Registration.create("new-service", "http://localhost:8080/health").build();
		InstanceId id = InstanceId.of("renamed-instance");
		Instance renamedInstance = Instance.create(id).register(oldRegistration).register(newRegistration);

		InstanceRegistrationUpdatedEvent event = new InstanceRegistrationUpdatedEvent(id, renamedInstance.getVersion(),
				newRegistration, oldRegistration);

		when(this.instanceRegistry.getInstance(id)).thenReturn(Mono.just(renamedInstance));
		when(this.instanceRegistry.getInstances("new-service"))
			.thenReturn(Flux.just(renamedInstance).filter(Instance::isRegistered));
		when(this.instanceRegistry.getInstances("old-service")).thenReturn(Flux.empty());

		List<Application> applications = Flux.from(this.applicationRegistry.getApplicationStream())
			.doOnSubscribe((sub) -> this.eventPublisher.emit(event))
			.filter((application) -> application.getName().equals("new-service")
					|| application.getName().equals("old-service"))
			.take(2)
			.collectList()
			.block(Duration.ofSeconds(5));

		assertThat(applications).extracting(Application::getName)
			.containsExactlyInAnyOrder("new-service", "old-service");
		Application renamed = applications.stream()
			.filter((a) -> a.getName().equals("new-service"))
			.findFirst()
			.orElseThrow();
		assertThat(renamed.getInstances()).extracting(Instance::getId).containsExactly(id);

		Application previous = applications.stream()
			.filter((a) -> a.getName().equals("old-service"))
			.findFirst()
			.orElseThrow();
		assertThat(previous.getInstances()).isEmpty();
	}

	@Test
	void getApplicationStream_recomputesOldApplicationWhenSiblingInstanceRemainsAfterRename() {
		// old-service still has instance-2; instance-1 moves to new-service.
		Registration oldRegistration1 = Registration.create("old-service", "http://localhost:8080/health").build();
		Registration newRegistration1 = Registration.create("new-service", "http://localhost:8080/health").build();
		InstanceId id1 = InstanceId.of("instance-1");
		Instance renamedInstance = Instance.create(id1).register(oldRegistration1).register(newRegistration1);

		Registration oldRegistration2 = Registration.create("old-service", "http://localhost:8081/health").build();
		Instance remainingInstance = Instance.create(InstanceId.of("instance-2")).register(oldRegistration2);

		InstanceRegistrationUpdatedEvent event = new InstanceRegistrationUpdatedEvent(id1, renamedInstance.getVersion(),
				newRegistration1, oldRegistration1);

		when(this.instanceRegistry.getInstance(id1)).thenReturn(Mono.just(renamedInstance));
		when(this.instanceRegistry.getInstances("new-service"))
			.thenReturn(Flux.just(renamedInstance).filter(Instance::isRegistered));
		when(this.instanceRegistry.getInstances("old-service"))
			.thenReturn(Flux.just(remainingInstance).filter(Instance::isRegistered));

		List<Application> applications = Flux.from(this.applicationRegistry.getApplicationStream())
			.doOnSubscribe((sub) -> this.eventPublisher.emit(event))
			.filter((application) -> application.getName().equals("new-service")
					|| application.getName().equals("old-service"))
			.take(2)
			.collectList()
			.block(Duration.ofSeconds(5));

		assertThat(applications).extracting(Application::getName)
			.containsExactlyInAnyOrder("new-service", "old-service");
		Application renamed = applications.stream()
			.filter((a) -> a.getName().equals("new-service"))
			.findFirst()
			.orElseThrow();
		assertThat(renamed.getInstances()).extracting(Instance::getId).containsExactly(id1);

		Application previous = applications.stream()
			.filter((a) -> a.getName().equals("old-service"))
			.findFirst()
			.orElseThrow();
		assertThat(previous.getInstances()).extracting(Instance::getId).containsExactly(remainingInstance.getId());
	}

	@Test
	void getApplicationStream_doesNotEmitExtraApplicationWhenOnlyNonNameFieldsChange() {
		// Same name, only the managementUrl differs - the event should result in a single
		// update for the same application and not a spurious delete/empty event.
		Registration oldRegistration = Registration.create("service", "http://localhost:8080/health")
			.managementUrl("http://localhost:8080/actuator")
			.build();
		Registration newRegistration = Registration.create("service", "http://localhost:8080/health")
			.managementUrl("http://localhost:9090/actuator")
			.build();
		InstanceId id = InstanceId.of("stable-instance");
		Instance updatedInstance = Instance.create(id).register(oldRegistration).register(newRegistration);

		InstanceRegistrationUpdatedEvent event = new InstanceRegistrationUpdatedEvent(id, updatedInstance.getVersion(),
				newRegistration, oldRegistration);

		when(this.instanceRegistry.getInstance(id)).thenReturn(Mono.just(updatedInstance));
		when(this.instanceRegistry.getInstances("service"))
			.thenReturn(Flux.just(updatedInstance).filter(Instance::isRegistered));

		// Emit the event right after the stream is subscribed; expect exactly one
		// application named "service" to be re-published (no empty/delete event).
		List<Application> applications = Flux.from(this.applicationRegistry.getApplicationStream())
			.doOnSubscribe((sub) -> this.eventPublisher.emit(event))
			.filter((application) -> application.getName().equals("service"))
			.take(1)
			.collectList()
			.block(Duration.ofSeconds(5));

		assertThat(applications).hasSize(1);
		Application application = applications.iterator().next();
		assertThat(application.getName()).isEqualTo("service");
		assertThat(application.getInstances()).extracting(Instance::getId).containsExactly(id);
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

	/**
	 * Test helper that exposes the protected publish method so tests can push events.
	 */
	static class TestInstanceEventPublisher extends InstanceEventPublisher {

		private final reactor.core.publisher.Sinks.Many<InstanceEvent> sink = reactor.core.publisher.Sinks.many()
			.multicast()
			.onBackpressureBuffer();

		@Override
		public void subscribe(org.reactivestreams.Subscriber<? super InstanceEvent> subscriber) {
			this.sink.asFlux().subscribe(subscriber);
		}

		void emit(InstanceEvent... events) {
			for (InstanceEvent event : events) {
				this.sink.emitNext(event, reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST);
			}
		}

	}

}
