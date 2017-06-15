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

package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.model.StatusInfo;
import reactor.test.publisher.TestPublisher;

import org.junit.Test;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class NotificationTriggerTest {
    private final Application application = Application.create(ApplicationId.of("id-1"),
            Registration.create("foo", "http://health-1").build()).build();

    @Test
    public void should_notify_on_event() throws InterruptedException {
        //given
        Notifier notifier = mock(Notifier.class);
        TestPublisher<ClientApplicationEvent> events = TestPublisher.create();
        NotificationTrigger trigger = new NotificationTrigger(notifier, events);
        trigger.start();
        doNothing().when(notifier).notify(isA(ClientApplicationEvent.class));

        //when registered event is emitted
        ClientApplicationStatusChangedEvent event = new ClientApplicationStatusChangedEvent(application,
                StatusInfo.ofUp(), StatusInfo.ofDown());
        events.next(event);
        //then should notify
        verify(notifier, times(1)).notify(event);

        //when registered event is emitted but the trigger has been stopped
        trigger.stop();
        reset(notifier);
        events.next(new ClientApplicationRegisteredEvent(application, application.getRegistration()));
        //then should not notify
        verify(notifier, never()).notify(event);
    }
}