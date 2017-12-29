/*
 * Copyright 2014-2017 the original author or authors.
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
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
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

    @Test
    public void should_start_and_stop_monitor() throws Exception {
        //given
        StatusUpdater updater = mock(StatusUpdater.class);
        when(updater.updateStatus(any(InstanceId.class))).thenReturn(Mono.empty());

        TestPublisher<InstanceEvent> publisher = TestPublisher.create();
        StatusUpdateTrigger trigger = new StatusUpdateTrigger(updater, publisher);
        trigger.setUpdateInterval(Duration.ofMillis(10));
        trigger.setStatusLifetime(Duration.ofMillis(10));

        //when trigger is initialized and an appliation is registered
        trigger.start();
        Thread.sleep(50L); //wait for subscription
        publisher.next(new InstanceRegisteredEvent(instance.getId(), 0L, instance.getRegistration()));

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
    public void should_update_on_event() throws InterruptedException {
        //given
        StatusUpdater updater = mock(StatusUpdater.class);
        when(updater.updateStatus(any(InstanceId.class))).thenReturn(Mono.empty());

        TestPublisher<InstanceEvent> events = TestPublisher.create();
        StatusUpdateTrigger trigger = new StatusUpdateTrigger(updater, events.flux());
        trigger.start();
        Thread.sleep(50L); //wait for subscription

        //when some non-registered event is emitted
        events.next(new InstanceInfoChangedEvent(instance.getId(), instance.getVersion(), Info.empty()));
        //then should not update
        verify(updater, never()).updateStatus(instance.getId());

        //when registered event is emitted
        events.next(new InstanceRegisteredEvent(instance.getId(), instance.getVersion(), instance.getRegistration()));
        //then should update
        verify(updater, times(1)).updateStatus(instance.getId());

        //when registered event is emitted but the trigger has been stopped
        trigger.stop();
        clearInvocations(updater);
        events.next(new InstanceRegisteredEvent(instance.getId(), instance.getVersion(), instance.getRegistration()));
        //then should not update
        verify(updater, never()).updateStatus(instance.getId());
    }

}
