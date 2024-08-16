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
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InstanceRegistryTest {

	private InstanceRepository repository;

	private InstanceIdGenerator idGenerator;

	private InstanceRegistry registry;

	@BeforeEach
	public void setUp() {
		repository = new EventsourcingInstanceRepository(new InMemoryEventStore());
		idGenerator = new HashingInstanceUrlIdGenerator();
		registry = new InstanceRegistry(repository, idGenerator, new TestInstanceFilter());
	}

	@Test
	public void registerFailed_null() {
		assertThatThrownBy(() -> registry.register(null)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void register() {
		Registration registration = Registration.create("abc", "http://localhost:8080/health").build();
		InstanceId id = registry.register(registration).block();

		StepVerifier.create(registry.getInstance(id)).assertNext((app) -> {
			assertThat(app.getRegistration()).isEqualTo(registration);
			assertThat(app.getId()).isNotNull();
		}).verifyComplete();

		StepVerifier.create(registry.getInstances()).assertNext((app) -> {
			assertThat(app.getRegistration()).isEqualTo(registration);
			assertThat(app.getId()).isNotNull();
		}).verifyComplete();
	}

	@Test
	public void deregister() {
		InstanceId id = registry.register(Registration.create("abc", "http://localhost:8080/health").build()).block();
		registry.deregister(id).block();

		StepVerifier.create(registry.getInstance(id))
			.assertNext((app) -> assertThat(app.isRegistered()).isFalse())
			.verifyComplete();
	}

	@Test
	public void refresh() {
		// Given instance is already reegistered and has status and info.
		StatusInfo status = StatusInfo.ofUp();
		Info info = Info.from(singletonMap("foo", "bar"));
		Registration registration = Registration.create("abc", "http://localhost:8080/health").build();
		InstanceId id = idGenerator.generateId(registration);
		Instance app = Instance.create(id).register(registration).withStatusInfo(status).withInfo(info);
		StepVerifier.create(repository.save(app)).expectNextCount(1).verifyComplete();

		// When instance registers second time
		InstanceId refreshId = registry.register(Registration.create("abc", "http://localhost:8080/health").build())
			.block();

		assertThat(refreshId).isEqualTo(id);
		StepVerifier.create(registry.getInstance(id)).assertNext((registered) -> {
			// Then info and status are retained
			assertThat(registered.getInfo()).isEqualTo(info);
			assertThat(registered.getStatusInfo()).isEqualTo(status);
		}).verifyComplete();
	}

	@Test
	public void findByName() {
		InstanceId id1 = registry.register(Registration.create("abc", "http://localhost:8080/health").build()).block();
		InstanceId id2 = registry.register(Registration.create("abc", "http://localhost:8081/health").build()).block();
		InstanceId id3 = registry.register(Registration.create("zzz", "http://localhost:9999/health").build()).block();

		StepVerifier.create(registry.getInstances("abc"))
			.recordWith(ArrayList::new)
			.thenConsumeWhile((a) -> true)
			.consumeRecordedWith(
					(applications) -> assertThat(applications.stream().map(Instance::getId)).doesNotContain(id3)
						.containsExactlyInAnyOrder(id1, id2))
			.verifyComplete();
	}

	@Test
	public void findByNameAndFilter() {
		InstanceId id1 = registry.register(Registration.create("abc", "http://localhost:8080/health").build()).block();
		registry
			.register(Registration.create("abc", "http://localhost:8081/health").metadata("displayed", "false").build())
			.block();

		StepVerifier.create(registry.getInstances("abc"))
			.recordWith(ArrayList::new)
			.thenConsumeWhile((a) -> true)
			.consumeRecordedWith(
					(applications) -> assertThat(applications.stream().map(Instance::getId)).containsExactly(id1))
			.verifyComplete();
	}

	private static class TestInstanceFilter extends InstanceFilter {

		@Override
		public boolean filter(Instance instance) {
			Map<String, String> metadata = instance.getRegistration().getMetadata();
			return !metadata.containsKey("displayed") || !metadata.get("displayed").equals("false");
		}

	}

}
