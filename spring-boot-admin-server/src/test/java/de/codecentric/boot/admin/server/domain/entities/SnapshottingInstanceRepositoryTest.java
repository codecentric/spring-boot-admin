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

package de.codecentric.boot.admin.server.domain.entities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.OptimisticLockingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SnapshottingInstanceRepositoryTest extends AbstractInstanceRepositoryTest {

	private final Instance instance = Instance.create(InstanceId.of("app-1"))
		.register(Registration.create("app", "http://health").build());

	private InMemoryEventStore eventStore = spy(new InMemoryEventStore());

	private SnapshottingInstanceRepository repository;

	@BeforeEach
	public void setUp() {
		this.repository = new SnapshottingInstanceRepository(this.eventStore);
		this.repository.start();
		super.setUp(this.repository);
	}

	@AfterEach
	public void tearDown() {
		this.repository.stop();
	}

	@Test
	public void should_return_instance_from_cache() {
		// given
		StepVerifier.create(this.repository.save(this.instance)).expectNext(this.instance).verifyComplete();
		// when
		reset(this.eventStore);
		StepVerifier.create(this.repository.find(this.instance.getId())).expectNext(this.instance).verifyComplete();
		// then
		verify(this.eventStore, never()).find(any());
	}

	@Test
	public void should_return_all_instances_from_cache() {
		// given
		StepVerifier.create(this.repository.save(this.instance)).expectNext(this.instance).verifyComplete();
		// when
		reset(this.eventStore);
		StepVerifier.create(this.repository.findAll()).expectNext(this.instance).verifyComplete();
		// then
		verify(this.eventStore, never()).findAll();
	}

	@Test
	public void should_update_cache_after_error() {
		// given
		this.repository.stop();
		when(this.eventStore.findAll()).thenReturn(
				Flux.just(new InstanceRegisteredEvent(InstanceId.of("broken"), 0L, this.instance.getRegistration()),
						new InstanceRegisteredEvent(InstanceId.of("broken"), 0L, this.instance.getRegistration()),
						new InstanceRegisteredEvent(this.instance.getId(), 0L, this.instance.getRegistration()),
						new InstanceRegisteredEvent(InstanceId.of("broken"), 1L, this.instance.getRegistration())));
		// when
		this.repository.start();
		// then
		reset(this.eventStore);
		StepVerifier.create(this.repository.find(this.instance.getId())).expectNext(this.instance).verifyComplete();
		StepVerifier.create(this.repository.find(InstanceId.of("broken")))
			.assertNext((i) -> assertThat(i.getVersion()).isEqualTo(1L))
			.verifyComplete();
	}

	@Test
	public void should_return_outdated_instance_not_present_in_cache() {
		this.repository.stop();
		// given
		StepVerifier.create(this.repository.save(this.instance)).expectNext(this.instance).verifyComplete();
		StepVerifier.create(this.repository.save(this.instance)).verifyError(OptimisticLockingException.class);
		// when
		StepVerifier.create(this.repository.find(this.instance.getId())).expectNext(this.instance).verifyComplete();
	}

	@Test
	public void should_refresh_snapshots_eagerly_on_optimistick_lock_exception() {
		// given
		StepVerifier.create(this.repository.save(this.instance)).expectNextCount(1L).verifyComplete();
		this.repository.stop();
		StepVerifier
			.create(this.repository.save(this.instance.clearUnsavedEvents().withStatusInfo(StatusInfo.ofDown())))
			.expectNextCount(1L)
			.verifyComplete();
		// when
		StepVerifier
			.create(this.repository.computeIfPresent(this.instance.getId(),
					(id, i) -> Mono.just(i.withStatusInfo(StatusInfo.ofUp()))))
			.expectNextCount(1L)
			.verifyComplete();
	}

}
