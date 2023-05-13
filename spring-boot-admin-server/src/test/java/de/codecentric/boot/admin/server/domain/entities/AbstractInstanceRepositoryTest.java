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

import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractInstanceRepositoryTest {

	private final Instance instance1 = Instance.create(InstanceId.of("app-1"))
		.register(Registration.create("app", "http://health").build());

	private final Instance instance2 = Instance.create(InstanceId.of("app-2"))
		.register(Registration.create("app", "http://health").build());

	private final Instance instance3 = Instance.create(InstanceId.of("other-1"))
		.register(Registration.create("other", "http://health").build());

	private InstanceRepository repository;

	public void setUp(InstanceRepository repository) {
		this.repository = repository;
	}

	@Test
	public void should_save() {
		// when
		StepVerifier.create(this.repository.save(this.instance1)).expectNext(this.instance1).verifyComplete();
		// then
		StepVerifier.create(this.repository.find(this.instance1.getId())).expectNext(this.instance1).verifyComplete();
	}

	@Test
	public void should_find_instances() {
		// given
		StepVerifier.create(this.repository.save(this.instance1)).expectNextCount(1).verifyComplete();
		StepVerifier.create(this.repository.save(this.instance2)).expectNextCount(1).verifyComplete();
		StepVerifier.create(this.repository.save(this.instance3)).expectNextCount(1).verifyComplete();

		// when/then
		StepVerifier.create(this.repository.find(this.instance2.getId())).expectNext(this.instance2).verifyComplete();

		StepVerifier.create(this.repository.findByName("app").collectList())
			.assertNext((v) -> assertThat(v).containsExactlyInAnyOrder(this.instance1, this.instance2))
			.verifyComplete();

		StepVerifier.create(this.repository.findAll().collectList())
			.assertNext((v) -> assertThat(v).containsExactlyInAnyOrder(this.instance1, this.instance2, this.instance3))
			.verifyComplete();
	}

	@Test
	public void should_computeIfPresent() {
		AtomicLong counter = new AtomicLong(3L);
		Endpoints infoEndpoint = Endpoints.single("info", "info");

		// given
		StepVerifier.create(this.repository.save(this.instance1)).expectNextCount(1).verifyComplete();

		// when
		StepVerifier.create(this.repository.computeIfPresent(this.instance1.getId(), (key, value) -> {
			if (counter.getAndDecrement() > 0L) {
				return Mono.just(this.instance1); // causes OptimistickLockException
			}
			else {
				return Mono.just(value.withEndpoints(infoEndpoint));
			}
		})).expectNext(this.instance1.withEndpoints(infoEndpoint)).verifyComplete();

		// then
		StepVerifier.create(this.repository.find(this.instance1.getId()))
			.expectNext(this.instance1.withEndpoints(infoEndpoint))
			.verifyComplete();
	}

	@Test
	public void should_not_compute_if_not_present() {
		// given
		InstanceId instanceId = InstanceId.of("not-existent");

		// when
		StepVerifier
			.create(this.repository.computeIfPresent(instanceId,
					(key, application) -> Mono.error(new AssertionFailedError("Should not call any computation"))))
			.verifyComplete();

		// then
		StepVerifier.create(this.repository.find(instanceId)).verifyComplete();
	}

	@Test
	public void should_run_compute_with_null() {
		InstanceId instanceId = InstanceId.of("app-1");
		Registration registration = Registration.create("app", "http://health").build();

		// when
		StepVerifier.create(this.repository.compute(this.instance1.getId(), (key, application) -> {
			assertThat(application).isNull();
			return Mono.just(Instance.create(key).register(registration));
		})).assertNext((v) -> {
			assertThat(v.getId()).isEqualTo(instanceId);
			assertThat(v.getRegistration()).isEqualTo(registration);
		}).verifyComplete();

		// then
		StepVerifier.create(this.repository.find(instanceId)).assertNext((v) -> {
			assertThat(v.getId()).isEqualTo(instanceId);
			assertThat(v.getRegistration()).isEqualTo(registration);
		}).verifyComplete();
	}

	@Test
	public void should_retry_compute() {
		AtomicLong counter = new AtomicLong(3L);
		Endpoints infoEndpoint = Endpoints.single("info", "info");

		// given
		StepVerifier.create(this.repository.save(this.instance1)).expectNextCount(1).verifyComplete();

		// when
		StepVerifier.create(this.repository.compute(this.instance1.getId(), (key, value) -> {
			if (counter.getAndDecrement() > 0L) {
				return Mono.just(this.instance1); // causes OptimistickLockException
			}
			else {
				return Mono.just(value.withEndpoints(infoEndpoint));
			}
		})).expectNext(this.instance1.withEndpoints(infoEndpoint)).verifyComplete();

		// then
		StepVerifier.create(this.repository.find(this.instance1.getId()))
			.expectNext(this.instance1.withEndpoints(infoEndpoint))
			.verifyComplete();
	}

}
