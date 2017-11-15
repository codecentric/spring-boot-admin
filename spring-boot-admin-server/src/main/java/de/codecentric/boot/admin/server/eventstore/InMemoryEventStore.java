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

package de.codecentric.boot.admin.server.eventstore;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event-Store backed by a ConcurrentHashMap.
 *
 * @author Johannes Edmeier
 */
public class InMemoryEventStore extends ConcurrentMapEventStore {
    public InMemoryEventStore() {
        this(100);
    }

    public InMemoryEventStore(int maxLogSizePerAggregate) {
        super(maxLogSizePerAggregate, new ConcurrentHashMap<>());
    }

    @Override
    public Mono<Void> append(List<InstanceEvent> events) {
        return super.append(events).then(Mono.fromRunnable(() -> this.publish(events)));
    }
}
