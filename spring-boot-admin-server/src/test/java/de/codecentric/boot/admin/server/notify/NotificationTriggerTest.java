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

package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import reactor.test.publisher.TestPublisher;

import org.junit.Test;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NotificationTriggerTest {
    private final Instance instance = Instance.create(InstanceId.of("id-1"))
                                              .register(Registration.create("foo", "http://health-1").build());

    @Test
    public void should_notify_on_event() throws InterruptedException {
        //given
        Notifier notifier = mock(Notifier.class);
        TestPublisher<InstanceEvent> events = TestPublisher.create();
        NotificationTrigger trigger = new NotificationTrigger(notifier, events);
        trigger.start();
        Thread.sleep(500L); //wait for subscription

        //when registered event is emitted
        InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(),
            StatusInfo.ofDown());
        events.next(event);
        //then should notify
        verify(notifier, times(1)).notify(event);

        //when registered event is emitted but the trigger has been stopped
        trigger.stop();
        clearInvocations(notifier);
        events.next(new InstanceRegisteredEvent(instance.getId(), instance.getVersion(), instance.getRegistration()));
        //then should not notify
        verify(notifier, never()).notify(event);
    }
}
