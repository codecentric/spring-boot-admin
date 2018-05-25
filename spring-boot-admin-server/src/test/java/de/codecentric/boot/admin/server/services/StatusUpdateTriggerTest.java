/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.services;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatusUpdateTriggerTest {
    private final Instance instance = Instance.create(InstanceId.of("id-1"))
                                              .register(Registration.create("foo", "http://health-1").build());
    private StatusUpdater updater = mock(StatusUpdater.class);
    private TestPublisher<InstanceEvent> events = TestPublisher.create();
    private StatusUpdateTrigger trigger;

    @Before
    public void setUp() throws Exception {
        when(updater.updateStatus(any(InstanceId.class))).thenReturn(Mono.empty());

        trigger = new StatusUpdateTrigger(updater, events.flux());
        trigger.start();
        Thread.sleep(50L); //wait for subscription
    }

    @Test
    public void should_start_and_stop_monitor() throws Exception {
        //given
        trigger.stop();
        trigger.setUpdateInterval(Duration.ofMillis(10));
        trigger.setStatusLifetime(Duration.ofMillis(10));
        trigger.start();
        Thread.sleep(50L); //wait for subscription

        events.next(new InstanceRegisteredEvent(instance.getId(), 0L, instance.getRegistration()));

        Thread.sleep(50L);
        //then it should start updating one time for registration and at least once for monitor
        verify(updater, atLeast(2)).updateStatus(instance.getId());

        //given long lifetime
        trigger.setStatusLifetime(Duration.ofSeconds(10));
        Thread.sleep(50L);
        clearInvocations(updater);
        //when the lifetime is not expired
        Thread.sleep(50L);
        //should never update
        verify(updater, never()).updateStatus(any(InstanceId.class));

        //when trigger ist destroyed
        trigger.setStatusLifetime(Duration.ofMillis(10));
        trigger.stop();
        clearInvocations(updater);
        Thread.sleep(15L);

        // it should stop updating
        verify(updater, never()).updateStatus(any(InstanceId.class));
    }

    @Test
    public void should_update_on_instance_registered_event() {
        //when registered event is emitted
        events.next(new InstanceRegisteredEvent(instance.getId(), instance.getVersion(), instance.getRegistration()));
        //then should update
        verify(updater, times(1)).updateStatus(instance.getId());
    }

    @Test
    public void should_update_on_instance_registration_update_event() {
        //when registered event is emitted
        events.next(
            new InstanceRegistrationUpdatedEvent(instance.getId(), instance.getVersion(), instance.getRegistration()));
        //then should update
        verify(updater, times(1)).updateStatus(instance.getId());
    }

    @Test
    public void should_not_update_on_non_relevant_event() {
        //when some non-registered event is emitted
        events.next(new InstanceInfoChangedEvent(instance.getId(), instance.getVersion(), Info.empty()));
        //then should not update
        verify(updater, never()).updateStatus(instance.getId());
    }

    @Test
    public void should_not_update_when_stopped() {
        //when registered event is emitted but the trigger has been stopped
        trigger.stop();
        clearInvocations(updater);
        events.next(new InstanceRegisteredEvent(instance.getId(), instance.getVersion(), instance.getRegistration()));
        //then should not update
        verify(updater, never()).updateStatus(instance.getId());
    }
}
