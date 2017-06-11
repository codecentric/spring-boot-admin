/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.server.journal.store;

import de.codecentric.boot.admin.server.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleJournaledEventStoreTest {
    private final Application application = Application.create(ApplicationId.of("id"),
            Registration.create("foo", "http://health").build()).build();

    @Test
    public void test_store() {
        SimpleJournaledEventStore store = new SimpleJournaledEventStore();

        List<ClientApplicationEvent> events = Arrays.asList(new ClientApplicationRegisteredEvent(application),
                new ClientApplicationDeregisteredEvent(application));

        for (ClientApplicationEvent event : events) {
            store.store(event);
        }

        assertThat(store.findAll()).containsExactly(events.get(1), events.get(0));
    }

    @Test
    public void test_store_capacity() {
        SimpleJournaledEventStore store = new SimpleJournaledEventStore();
        store.setCapacity(2);

        List<ClientApplicationEvent> events = Arrays.asList(new ClientApplicationRegisteredEvent(application),
                new ClientApplicationDeregisteredEvent(application),
                new ClientApplicationDeregisteredEvent(application));

        for (ClientApplicationEvent event : events) {
            store.store(event);
        }

        assertThat(store.findAll()).containsExactly(events.get(2), events.get(1));
    }

}
