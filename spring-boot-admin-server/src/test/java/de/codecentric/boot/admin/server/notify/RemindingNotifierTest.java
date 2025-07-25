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

package de.codecentric.boot.admin.server.notify;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RemindingNotifierTest {

	private static final Instance instance1 = Instance.create(InstanceId.of("id-1"))
		.register(Registration.create("App", "https://health").build())
		.withStatusInfo(StatusInfo.ofDown());

	private static final Instance instance2 = Instance.create(InstanceId.of("id-2"))
		.register(Registration.create("App", "https://health").build())
		.withStatusInfo(StatusInfo.ofDown());

	private static final InstanceEvent appDown = new InstanceStatusChangedEvent(instance1.getId(), 0L,
			StatusInfo.ofDown());

	private static final InstanceEvent appUp = new InstanceStatusChangedEvent(instance1.getId(), 0L, StatusInfo.ofUp());

	private static final InstanceEvent appEndpointsDiscovered = new InstanceEndpointsDetectedEvent(instance1.getId(),
			0L, Endpoints.empty());

	private static final InstanceEvent appDeregister = new InstanceDeregisteredEvent(instance1.getId(), 0L);

	private static final InstanceEvent otherAppUp = new InstanceStatusChangedEvent(instance2.getId(), 0L,
			StatusInfo.ofUp());

	private static final InstanceEndpointsDetectedEvent errorTriggeringEvent = new InstanceEndpointsDetectedEvent(
			instance1.getId(), 999L, Endpoints.empty());

	private InstanceRepository repository;

	@BeforeEach
	void setUp() {
		this.repository = mock(InstanceRepository.class);
		when(this.repository.find(any())).thenReturn(Mono.empty());
		when(this.repository.find(instance1.getId())).thenReturn(Mono.just(instance1));
		when(this.repository.find(instance2.getId())).thenReturn(Mono.just(instance2));
	}

	@ParameterizedTest
	@NullSource
	void should_throw_on_invalid_ctor(Iterable<Notifier> delegates) {
		assertThatThrownBy(() -> new CompositeNotifier(delegates)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void should_remind_only_down_events() {
		TestNotifier notifier = new TestNotifier();
		RemindingNotifier reminder = new RemindingNotifier(notifier, this.repository);
		reminder.setReminderPeriod(Duration.ZERO);

		StepVerifier.create(reminder.notify(appDown)).verifyComplete();
		StepVerifier.create(reminder.notify(appEndpointsDiscovered)).verifyComplete();
		StepVerifier.create(reminder.notify(otherAppUp)).verifyComplete();

		await().pollDelay(Duration.ofMillis(10))
			.untilAsserted(() -> StepVerifier.create(reminder.sendReminders()).verifyComplete());
		await().pollDelay(Duration.ofMillis(10))
			.untilAsserted(() -> StepVerifier.create(reminder.sendReminders()).verifyComplete());

		assertThat(notifier.getEvents()).containsExactlyInAnyOrder(appDown, appEndpointsDiscovered, otherAppUp, appDown,
				appDown);
	}

	@Test
	void should_not_remind_remind_after_up() {
		TestNotifier notifier = new TestNotifier();
		RemindingNotifier reminder = new RemindingNotifier(notifier, this.repository);
		reminder.setReminderPeriod(Duration.ZERO);

		StepVerifier.create(reminder.notify(appDown)).verifyComplete();
		StepVerifier.create(reminder.notify(appUp)).verifyComplete();
		StepVerifier.create(reminder.sendReminders()).verifyComplete();

		assertThat(notifier.getEvents()).containsExactlyInAnyOrder(appDown, appUp);
	}

	@Test
	void should_not_remind_remind_after_deregister() {
		TestNotifier notifier = new TestNotifier();
		RemindingNotifier reminder = new RemindingNotifier(notifier, this.repository);
		reminder.setReminderPeriod(Duration.ZERO);

		StepVerifier.create(reminder.notify(appDown)).verifyComplete();
		StepVerifier.create(reminder.notify(appDeregister)).verifyComplete();
		StepVerifier.create(reminder.sendReminders()).verifyComplete();

		assertThat(notifier.getEvents()).containsExactlyInAnyOrder(appDown, appDeregister);
	}

	@Test
	void should_not_remind_remind_before_period_ends() {
		TestNotifier notifier = new TestNotifier();
		RemindingNotifier reminder = new RemindingNotifier(notifier, this.repository);
		reminder.setReminderPeriod(Duration.ofHours(24));

		StepVerifier.create(reminder.notify(appDown)).verifyComplete();
		StepVerifier.create(reminder.sendReminders()).verifyComplete();

		assertThat(notifier.getEvents()).containsExactlyInAnyOrder(appDown);
	}

	@Test
	void should_resubscribe_after_error() {
		TestPublisher<InstanceEvent> eventPublisher = TestPublisher.create();

		Flux<InstanceEvent> emittedNotifications = Flux.create((emitter) -> {
			Notifier notifier = (event) -> {
				emitter.next(event);
				if (event.equals(errorTriggeringEvent)) {
					return Mono.error(new IllegalArgumentException("TEST-ERROR"));
				}
				return Mono.empty();
			};

			RemindingNotifier reminder = new RemindingNotifier(notifier, this.repository);
			eventPublisher.flux().flatMap(reminder::notify).subscribe();
			reminder.setCheckReminderInverval(Duration.ofMillis(10));
			reminder.setReminderPeriod(Duration.ofMillis(10));
			reminder.start();
		});

		StepVerifier.create(emittedNotifications)
			.expectSubscription()
			.then(() -> eventPublisher.next(appDown))
			.expectNext(appDown, appDown)
			.then(() -> eventPublisher.next(errorTriggeringEvent))
			.thenConsumeWhile((e) -> !e.equals(errorTriggeringEvent))
			.expectNext(errorTriggeringEvent, appDown, appDown)
			.thenCancel()
			.verify();
	}

}
