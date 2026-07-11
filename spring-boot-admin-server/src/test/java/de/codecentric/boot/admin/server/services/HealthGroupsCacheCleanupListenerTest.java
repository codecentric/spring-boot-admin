/*
 * Copyright 2014-2025 the original author or authors.
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

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.publisher.TestPublisher;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class HealthGroupsCacheCleanupListenerTest {

	private final TestPublisher<InstanceEvent> events = TestPublisher.create();

	private HealthGroupsCache cache;

	private HealthGroupsCacheCleanupListener listener;

	@BeforeEach
	void setUp() {
		this.cache = new InMemoryHealthGroupsCache();
		this.listener = new HealthGroupsCacheCleanupListener(this.events.flux(), this.cache);
		this.listener.start();
		await().until(this.events::wasSubscribed);
	}

	@AfterEach
	void tearDown() {
		this.listener.stop();
	}

	@Test
	void should_remove_cache_entry_on_deregistration() {
		InstanceId instanceId = InstanceId.of("test-id");
		this.cache.updateGroups(instanceId, List.of("liveness", "readiness"));

		this.events.next(new InstanceDeregisteredEvent(instanceId, 1L));

		assertThat(this.cache.getGroups(instanceId)).isEmpty();
	}

	@Test
	void should_not_fail_on_unknown_instance() {
		InstanceId instanceId = InstanceId.of("test-id");

		this.events.next(new InstanceDeregisteredEvent(instanceId, 1L));

		assertThat(this.cache.getGroups(instanceId)).isEmpty();
	}

}
