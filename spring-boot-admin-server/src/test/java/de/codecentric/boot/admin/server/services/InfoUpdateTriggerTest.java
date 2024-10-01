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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InfoUpdateTriggerTest {

	private final Instance instance = Instance.create(InstanceId.of("id-1"))
		.register(Registration.create("foo", "http://health-1").build());

	private final InfoUpdater updater = mock(InfoUpdater.class);

	private final TestPublisher<InstanceEvent> events = TestPublisher.create();

	private InfoUpdateTrigger trigger;

	@BeforeEach
	public void setUp() throws Exception {
		when(this.updater.updateInfo(any(InstanceId.class))).thenReturn(Mono.empty());

		this.trigger = new InfoUpdateTrigger(this.updater, this.events.flux(), Duration.ofMinutes(5),
				Duration.ofMinutes(1), Duration.ofMinutes(10));
		this.trigger.start();
		await().until(this.events::wasSubscribed);
	}

	@Test
	public void should_start_and_stop_monitor() throws Exception {
		// given
		this.trigger.stop();
		this.trigger.setInterval(Duration.ofMillis(10));
		this.trigger.setLifetime(Duration.ofMillis(10));
		this.trigger.start();
		await().until(this.events::wasSubscribed);

		this.events.next(
				new InstanceStatusChangedEvent(this.instance.getId(), this.instance.getVersion(), StatusInfo.ofDown()));

		Thread.sleep(50L);
		// then it should start updating one time for registration and at least once for
		// monitor
		verify(this.updater, atLeast(2)).updateInfo(this.instance.getId());

		// given long lifetime
		this.trigger.setLifetime(Duration.ofSeconds(10));
		Thread.sleep(50L);
		clearInvocations(this.updater);
		// when the lifetime is not expired
		Thread.sleep(50L);
		// should never update
		verify(this.updater, never()).updateInfo(any(InstanceId.class));

		// when trigger ist destroyed
		this.trigger.setLifetime(Duration.ofMillis(10));
		this.trigger.stop();
		clearInvocations(this.updater);
		Thread.sleep(15L);

		// it should stop updating
		verify(this.updater, never()).updateInfo(any(InstanceId.class));
	}

	@Test
	public void should_not_update_when_stopped() {
		// when registered event is emitted but the trigger has been stopped
		this.trigger.stop();
		clearInvocations(this.updater);
		this.events.next(new InstanceRegisteredEvent(this.instance.getId(), this.instance.getVersion(),
				this.instance.getRegistration()));
		// then should not update
		verify(this.updater, never()).updateInfo(this.instance.getId());
	}

	@Test
	public void should_update_on_endpoints_detectes_event() {
		// when registered event is emitted
		this.events.next(new InstanceEndpointsDetectedEvent(this.instance.getId(), this.instance.getVersion(),
				this.instance.getEndpoints()));
		// then should update
		verify(this.updater, times(1)).updateInfo(this.instance.getId());
	}

	@Test
	public void should_update_on_status_changed_event() {
		// when registered event is emitted
		this.events.next(
				new InstanceStatusChangedEvent(this.instance.getId(), this.instance.getVersion(), StatusInfo.ofDown()));
		// then should update
		verify(this.updater, times(1)).updateInfo(this.instance.getId());
	}

	@Test
	public void should_update_on_instance_registration_update_event() {
		// when registered event is emitted
		this.events.next(new InstanceRegistrationUpdatedEvent(this.instance.getId(), this.instance.getVersion(),
				this.instance.getRegistration()));
		// then should update
		verify(this.updater, times(1)).updateInfo(this.instance.getId());
	}

	@Test
	public void should_not_update_on_non_relevant_event() {
		// when some non-registered event is emitted
		this.events.next(new InstanceInfoChangedEvent(this.instance.getId(), this.instance.getVersion(), Info.empty()));
		// then should not update
		verify(this.updater, never()).updateInfo(this.instance.getId());
	}

	@Test
	public void should_continue_update_after_error() throws InterruptedException {
		// when status-change event is emitted and an error is emitted
		when(this.updater.updateInfo(any())).thenReturn(Mono.error(IllegalStateException::new))
			.thenReturn(Mono.empty());

		this.events.next(
				new InstanceStatusChangedEvent(this.instance.getId(), this.instance.getVersion(), StatusInfo.ofDown()));
		this.events
			.next(new InstanceStatusChangedEvent(this.instance.getId(), this.instance.getVersion(), StatusInfo.ofUp()));

		// then should update
		verify(this.updater, times(2)).updateInfo(this.instance.getId());
	}

}
