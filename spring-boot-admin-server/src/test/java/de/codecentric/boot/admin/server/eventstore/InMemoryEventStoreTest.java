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

package de.codecentric.boot.admin.server.eventstore;

import java.util.List;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;

public class InMemoryEventStoreTest extends AbstractEventStoreTest {

	@Override
	protected InstanceEventStore createStore(int maxLogSizePerAggregate) {
		return new InMemoryEventStore(maxLogSizePerAggregate);
	}

	@Override
	protected void shutdownStore() {
		// NOOP;
	}

	@Test
	public void should_prune_info_updated_events_when_enabled() {
		InstanceEventStore store = new InMemoryEventStore(100, true);
		InstanceId id = InstanceId.of("id");
		Registration registration = Registration.create("foo", "https://health").build();

		InstanceEvent event1 = new InstanceRegisteredEvent(id, 0L, registration);
		InstanceEvent event2 = new InstanceInfoChangedEvent(id, 1L, Info.empty());
		InstanceEvent event3 = new InstanceInfoChangedEvent(id, 2L, Info.empty());
		InstanceEvent event4 = new InstanceInfoChangedEvent(id, 3L, Info.empty());

		StepVerifier.create(store.append(List.of(event1, event2, event3, event4))).verifyComplete();

		StepVerifier.create(store.findAll()).expectNext(event1, event4).verifyComplete();
	}

	@Test
	public void should_not_prune_info_updated_events_when_disabled() {
		InstanceEventStore store = new InMemoryEventStore(100, false);
		InstanceId id = InstanceId.of("id");
		Registration registration = Registration.create("foo", "https://health").build();

		InstanceEvent event1 = new InstanceRegisteredEvent(id, 0L, registration);
		InstanceEvent event2 = new InstanceInfoChangedEvent(id, 1L, Info.empty());
		InstanceEvent event3 = new InstanceInfoChangedEvent(id, 2L, Info.empty());

		StepVerifier.create(store.append(List.of(event1, event2, event3))).verifyComplete();

		StepVerifier.create(store.findAll()).expectNext(event1, event2, event3).verifyComplete();
	}

}
