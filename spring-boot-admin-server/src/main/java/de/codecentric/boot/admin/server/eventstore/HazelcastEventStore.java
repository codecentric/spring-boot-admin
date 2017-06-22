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

import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.core.EntryAdapter;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.IMap;
import com.hazelcast.map.listener.MapListener;

/**
 * Event-Store backed by a Hazelcast-map.
 *
 * @author Johannes Edmeier
 */
public class HazelcastEventStore extends ConcurrentMapEventStore {

    private static final Logger log = LoggerFactory.getLogger(HazelcastEventStore.class);

    public HazelcastEventStore(IMap<ApplicationId, List<ClientApplicationEvent>> eventLogs) {
        this(100, eventLogs);
    }

    public HazelcastEventStore(int maxLogSizePerAggregate, IMap<ApplicationId, List<ClientApplicationEvent>> eventLog) {
        super(maxLogSizePerAggregate, eventLog);

        eventLog.addEntryListener((MapListener) new EntryAdapter<ApplicationId, List<ClientApplicationEvent>>() {
            @Override
            public void entryUpdated(EntryEvent<ApplicationId, List<ClientApplicationEvent>> event) {
                log.debug("Updated {}", event);
                long lastKnownVersion = getLastVersion(event.getOldValue());
                List<ClientApplicationEvent> newEvents = event.getValue()
                                                              .stream()
                                                              .filter(e -> e.getVersion() > lastKnownVersion)
                                                              .collect(Collectors.toList());
                HazelcastEventStore.this.publish(newEvents);
            }
        }, true);
    }

}
