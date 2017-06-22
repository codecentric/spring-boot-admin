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

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.Registration;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

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
    private final Application application = Application.create(ApplicationId.of("id-1"))
                                                       .register(Registration.create("foo", "http://health-1").build());

    @Test
    public void should_start_and_stop_monitor() throws Exception {
        //given
        StatusUpdater updater = mock(StatusUpdater.class);
        when(updater.updateStatus(any(ApplicationId.class))).thenReturn(Mono.empty());

        TestPublisher<ClientApplicationEvent> publisher = TestPublisher.create();
        StatusUpdateTrigger trigger = new StatusUpdateTrigger(updater, publisher);
        trigger.setUpdateInterval(10L);
        trigger.setStatusLifetime(10L);

        //when trigger is initialized and an appliation is registered
        trigger.start();
        Thread.sleep(50L); //wait for subscription
        publisher.next(new ClientApplicationRegisteredEvent(application.getId(), 0L, application.getRegistration()));

        Thread.sleep(50L);
        //then it should start updating one time for registration and at least once for monitor
        verify(updater, atLeast(2)).updateStatus(application.getId());

        //given long lifetime
        trigger.setStatusLifetime(10_000L);
        Thread.sleep(50L);
        clearInvocations(updater);
        //when the lifetime is not expired
        Thread.sleep(50L);
        //should never update
        verify(updater, never()).updateStatus(any(ApplicationId.class));

        //when trigger ist destroyed
        trigger.setStatusLifetime(10L);
        trigger.stop();
        clearInvocations(updater);
        Thread.sleep(15L);

        // it should stop updating
        verify(updater, never()).updateStatus(any(ApplicationId.class));
    }

    @Test
    public void should_update_on_event() throws InterruptedException {
        //given
        StatusUpdater updater = mock(StatusUpdater.class);
        when(updater.updateStatus(any(ApplicationId.class))).thenReturn(Mono.empty());

        TestPublisher<ClientApplicationEvent> events = TestPublisher.create();
        StatusUpdateTrigger trigger = new StatusUpdateTrigger(updater, events.flux());
        trigger.start();
        Thread.sleep(50L); //wait for subscription

        //when some non-registered event is emitted
        events.next(new ClientApplicationInfoChangedEvent(application.getId(), application.getVersion(), Info.empty()));
        //then should not update
        verify(updater, never()).updateStatus(application.getId());

        //when registered event is emitted
        events.next(new ClientApplicationRegisteredEvent(application.getId(), application.getVersion(),
                application.getRegistration()));
        //then should update
        verify(updater, times(1)).updateStatus(application.getId());

        //when registered event is emitted but the trigger has been stopped
        trigger.stop();
        clearInvocations(updater);
        events.next(new ClientApplicationRegisteredEvent(application.getId(), application.getVersion(),
                application.getRegistration()));
        //then should not update
        verify(updater, never()).updateStatus(application.getId());
    }

}
