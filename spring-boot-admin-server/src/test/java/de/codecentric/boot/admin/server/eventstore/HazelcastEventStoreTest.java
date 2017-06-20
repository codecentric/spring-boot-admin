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
package de.codecentric.boot.admin.server.eventstore;

import de.codecentric.boot.admin.server.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class HazelcastEventStoreTest {

    private HazelcastEventStore store;

    @Before
    public void setup() {
        HazelcastInstance hazelcast = HazelcastInstanceFactory.newHazelcastInstance(new Config());
        store = new HazelcastEventStore(hazelcast.getList("testList"));
    }

    @Test
    public void test_store() {
        Application application = Application.create(ApplicationId.of("id"),
                Registration.create("foo", "http://health").build()).build();
        List<ClientApplicationEvent> events = Arrays.asList(
                new ClientApplicationRegisteredEvent(application.getId(), application.getRegistration()),
                new ClientApplicationDeregisteredEvent(application.getId()));

        for (ClientApplicationEvent event : events) {
            store.store(event);
        }

        // Items are stored in reverse order
        List<ClientApplicationEvent> reversed = new ArrayList<>(events);
        Collections.reverse(reversed);

        assertThat(store.findAll()).containsOnlyElementsOf(reversed);
    }
}
