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

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NotificationTriggerTest {

	private final Instance instance = Instance.create(InstanceId.of("id-1"))
		.register(Registration.create("foo", "http://health-1").build());

	private final Notifier notifier = mock(Notifier.class);

	private final TestPublisher<InstanceEvent> events = TestPublisher.create();

	private final NotificationTrigger trigger = new NotificationTrigger(this.notifier, this.events);

	public NotificationTriggerTest() {
		when(this.notifier.notify(any())).thenReturn(Mono.empty());
	}

	@Test
	public void should_notify_on_event() throws InterruptedException {
		// given the notifier subscribed to the events
		this.trigger.start();
		await().until(this.events::wasSubscribed);

		// when registered event is emitted
		InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(this.instance.getId(),
				this.instance.getVersion(), StatusInfo.ofDown());
		this.events.next(event);

		// then should notify
		verify(this.notifier, times(1)).notify(event);

		// when registered event is emitted but the trigger has been stopped
		this.trigger.stop();
		clearInvocations(this.notifier);
		this.events.next(new InstanceRegisteredEvent(this.instance.getId(), this.instance.getVersion(),
				this.instance.getRegistration()));
		// then should not notify
		verify(this.notifier, never()).notify(event);
	}

	@Test

	public void should_resume_on_exceptopn() throws InterruptedException {
		// given
		this.trigger.start();
		await().until(this.events::wasSubscribed);

		when(this.notifier.notify(any())).thenReturn(Mono.error(new IllegalStateException("Test")))
			.thenReturn(Mono.empty());

		// when exception for the first event is thrown and a subsequent event is fired
		InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(this.instance.getId(),
				this.instance.getVersion(), StatusInfo.ofDown());
		this.events.next(event);
		this.events.next(event);

		// the notifier was after the exception
		verify(this.notifier, times(2)).notify(event);
	}

}
