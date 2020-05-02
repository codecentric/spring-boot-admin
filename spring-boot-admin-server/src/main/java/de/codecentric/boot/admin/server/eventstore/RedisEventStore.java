/*
 * Copyright 2014-2020 the original author or authors.
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

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * Event-Store backed by a Redis-map.
 *
 * @author Stefan Rempfer
 */
public class RedisEventStore extends ConcurrentMapEventStore {

	private static final Logger log = LoggerFactory.getLogger(RedisEventStore.class);

	public RedisEventStore(ConcurrentMap<InstanceId, List<InstanceEvent>> eventLogs) {
		this(100, eventLogs);
	}

	public RedisEventStore(int maxLogSizePerAggregate, ConcurrentMap<InstanceId, List<InstanceEvent>> eventLog) {
		super(maxLogSizePerAggregate, eventLog);
	}

	@Override
	public Mono<Void> append(List<InstanceEvent> events) {
		return super.append(events).then(Mono.fromRunnable(() -> this.publish(events)));
	}

}
