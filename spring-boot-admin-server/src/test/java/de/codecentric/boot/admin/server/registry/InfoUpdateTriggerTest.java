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

package de.codecentric.boot.admin.server.registry;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.model.StatusInfo;
import reactor.test.publisher.TestPublisher;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class InfoUpdateTriggerTest {
    private final Application application = Application.create(ApplicationId.of("id-1"),
            Registration.create("foo", "http://health-1").build()).build();

    @Test
    public void should_update_on_event() {
        //given
        InfoUpdater updater = mock(InfoUpdater.class);
        TestPublisher<ClientApplicationEvent> events = TestPublisher.create();
        InfoUpdateTrigger trigger = new InfoUpdateTrigger(updater, events);
        trigger.start();

        //when some non-registered event is emitted
        events.next(new ClientApplicationRegisteredEvent(application, application.getRegistration()));
        //then should not update
        verify(updater, never()).updateInfo(application);

        //when registered event is emitted
        events.next(new ClientApplicationStatusChangedEvent(application, StatusInfo.ofUp(), StatusInfo.ofDown()));
        //then should update
        verify(updater, times(1)).updateInfo(application);

        //when registered event is emitted but the trigger has been stopped
        trigger.stop();
        reset(updater);
        events.next(new ClientApplicationRegisteredEvent(application, application.getRegistration()));
        //then should not update
        verify(updater, never()).updateInfo(application);
    }

}
