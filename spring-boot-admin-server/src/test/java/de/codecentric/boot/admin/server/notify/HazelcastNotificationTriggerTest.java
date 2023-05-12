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

import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Test;
import reactor.test.publisher.TestPublisher;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class HazelcastNotificationTriggerTest {

	private final Instance instance = Instance.create(InstanceId.of("id-1"))
		.register(Registration.create("foo", "http://health-1").build());

	private final Notifier notifier = mock(Notifier.class);

	private final TestPublisher<InstanceEvent> events = TestPublisher.create();

	private final ConcurrentHashMap<InstanceId, Long> sentNotifications = new ConcurrentHashMap<>();

	private final HazelcastNotificationTrigger trigger = new HazelcastNotificationTrigger(this.notifier, this.events,
			this.sentNotifications);

	@Test
	void should_trigger_notifications() {
		// given then notifier has subscribed to the events and no notification was sent
		// before
		this.sentNotifications.clear();
		this.trigger.start();
		await().until(this.events::wasSubscribed);

		// when registered event is emitted
		InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(this.instance.getId(),
				this.instance.getVersion(), StatusInfo.ofDown());
		this.events.next(event);
		// then should notify
		verify(this.notifier, times(1)).notify(event);
	}

	@Test
	void should_not_trigger_notifications() {
		// given the event is in the already sent notifications.
		InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(this.instance.getId(),
				this.instance.getVersion(), StatusInfo.ofDown());
		this.sentNotifications.put(event.getInstance(), event.getVersion());
		this.trigger.start();
		await().until(this.events::wasSubscribed);

		// when registered event is emitted
		this.events.next(event);
		// then should not notify
		verify(this.notifier, never()).notify(event);
	}

}
