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

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import com.hazelcast.core.IList;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

/**
 * Event-Store backed by a Hazelcast-list.
 *
 * @author Johannes Edmeier
 */
public class HazelcastEventStore extends ClientApplicationEventPublisher implements ClientApplicationEventStore {

    private IList<ClientApplicationEvent> store;

    public HazelcastEventStore(IList<ClientApplicationEvent> store) {
        this.store = store;
        store.addItemListener(new ItemListener<ClientApplicationEvent>() {
            @Override
            public void itemAdded(ItemEvent<ClientApplicationEvent> item) {
                HazelcastEventStore.this.publish(item.getItem());
            }

            @Override
            public void itemRemoved(ItemEvent<ClientApplicationEvent> item) {
            }
        }, true);
    }

    @Override
    public Collection<ClientApplicationEvent> findAll() {
        ArrayList<ClientApplicationEvent> list = new ArrayList<>(store);
        Collections.reverse(list);
        return list;
    }

    @Override
    public void store(ClientApplicationEvent event) {
        store.add(event);
    }

}
