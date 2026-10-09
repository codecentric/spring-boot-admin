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

package de.codecentric.boot.admin.server.notify;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AbstractStatusChangeNotifierTest {

	private static final InstanceId ID = InstanceId.of("id-1");

	private final Instance instance = Instance.create(ID)
		.register(Registration.create("foo", "http://health-1").build());

	private final InstanceRepository repository = mock(InstanceRepository.class);

	AbstractStatusChangeNotifierTest() {
		when(this.repository.find(ID)).thenReturn(Mono.just(this.instance));
	}

	@Test
	void should_notify_only_on_actual_status_changes() {
		RecordingNotifier notifier = new RecordingNotifier(this.repository);

		notify(notifier, new InstanceStatusChangedEvent(ID, 1L, StatusInfo.ofDown()));
		notify(notifier, new InstanceStatusChangedEvent(ID, 2L, StatusInfo.ofDown()));
		notify(notifier, new InstanceStatusChangedEvent(ID, 3L, StatusInfo.ofUp()));

		assertThat(notifier.lastStatuses).containsExactly("UNKNOWN", "DOWN");
	}

	@Test
	void should_forget_status_on_deregistration() {
		RecordingNotifier notifier = new RecordingNotifier(this.repository);

		notify(notifier, new InstanceStatusChangedEvent(ID, 1L, StatusInfo.ofDown()));
		notify(notifier, new InstanceDeregisteredEvent(ID, 2L));
		notify(notifier, new InstanceStatusChangedEvent(ID, 3L, StatusInfo.ofDown()));

		assertThat(notifier.lastStatuses).containsExactly("UNKNOWN", "UNKNOWN");
	}

	@Test
	void should_not_drop_recovery_when_previous_status_was_handled_by_another_member() {
		ConcurrentMap<String, String> shared = new ConcurrentHashMap<>();
		RecordingNotifier memberA = new RecordingNotifier(this.repository);
		memberA.setLastStatusStore(new LastStatusStore(shared, "notifier"));
		RecordingNotifier memberB = new RecordingNotifier(this.repository);
		memberB.setLastStatusStore(new LastStatusStore(shared, "notifier"));

		notify(memberA, new InstanceStatusChangedEvent(ID, 1L, StatusInfo.ofDown()));
		notify(memberB, new InstanceStatusChangedEvent(ID, 2L, StatusInfo.ofUp()));

		assertThat(memberA.lastStatuses).containsExactly("UNKNOWN");
		assertThat(memberB.lastStatuses).containsExactly("DOWN");
	}

	@Test
	void should_keep_state_of_different_notifiers_apart() {
		ConcurrentMap<String, String> shared = new ConcurrentHashMap<>();
		RecordingNotifier first = new RecordingNotifier(this.repository);
		first.setLastStatusStore(new LastStatusStore(shared, "first"));
		RecordingNotifier second = new RecordingNotifier(this.repository);
		second.setLastStatusStore(new LastStatusStore(shared, "second"));

		InstanceStatusChangedEvent down = new InstanceStatusChangedEvent(ID, 1L, StatusInfo.ofDown());
		notify(first, down);
		notify(second, down);

		assertThat(first.events).containsExactly(down);
		assertThat(second.events).containsExactly(down);
	}

	private void notify(RecordingNotifier notifier, InstanceEvent event) {
		StepVerifier.create(notifier.notify(event)).verifyComplete();
	}

	private static class RecordingNotifier extends AbstractStatusChangeNotifier {

		private final List<InstanceEvent> events = new ArrayList<>();

		private final List<String> lastStatuses = new ArrayList<>();

		RecordingNotifier(InstanceRepository repository) {
			super(repository);
		}

		@Override
		protected Mono<Void> doNotify(InstanceEvent event, Instance instance) {
			return Mono.fromRunnable(() -> {
				this.events.add(event);
				this.lastStatuses.add(getLastStatus(event.getInstance()));
			});
		}

	}

}
