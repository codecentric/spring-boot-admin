/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.eventstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.reducing;

public abstract class ConcurrentMapEventStore extends InstanceEventPublisher implements InstanceEventStore {

	private static final Logger log = LoggerFactory.getLogger(ConcurrentMapEventStore.class);

	private static final Comparator<InstanceEvent> byTimestampAndIdAndVersion = comparing(InstanceEvent::getTimestamp)
		.thenComparing(InstanceEvent::getInstance)
		.thenComparing(InstanceEvent::getVersion);

	private final int maxLogSizePerAggregate;

	private final ConcurrentMap<InstanceId, List<InstanceEvent>> eventLog;

	protected ConcurrentMapEventStore(int maxLogSizePerAggregate,
			ConcurrentMap<InstanceId, List<InstanceEvent>> eventLog) {
		this.eventLog = eventLog;
		this.maxLogSizePerAggregate = maxLogSizePerAggregate;
	}

	@Override
	public Flux<InstanceEvent> findAll() {
		return Flux.defer(() -> Flux.fromIterable(eventLog.values())
			.flatMapIterable(Function.identity())
			.sort(byTimestampAndIdAndVersion));
	}

	@Override
	public Flux<InstanceEvent> find(InstanceId id) {
		return Flux.defer(() -> Flux.fromIterable(eventLog.getOrDefault(id, Collections.emptyList())));
	}

	@Override
	public Mono<Void> append(List<InstanceEvent> events) {
		return Mono.fromRunnable(() -> {
			while (true) {
				if (doAppend(events)) {
					return;
				}
			}
		});
	}

	protected boolean doAppend(List<InstanceEvent> events) {
		if (events.isEmpty()) {
			return true;
		}

		InstanceId id = events.get(0).getInstance();
		if (!events.stream().allMatch((event) -> event.getInstance().equals(id))) {
			throw new IllegalArgumentException("'events' must only refer to the same instance.");
		}

		List<InstanceEvent> oldEvents = eventLog.computeIfAbsent(id,
				(key) -> new ArrayList<>(maxLogSizePerAggregate + 1));

		long lastVersion = getLastVersion(oldEvents);
		if (lastVersion >= events.get(0).getVersion()) {
			throw createOptimisticLockException(events.get(0), lastVersion);
		}

		List<InstanceEvent> newEvents = new ArrayList<>(oldEvents);
		newEvents.addAll(events);

		if (newEvents.size() > maxLogSizePerAggregate) {
			log.debug("Threshold for {} reached. Compacting events", id);
			compact(newEvents);
		}

		if (eventLog.replace(id, oldEvents, newEvents)) {
			log.debug("Events appended to log {}", events);
			return true;
		}

		log.debug("Unsuccessful attempt append the events {} ", events);
		return false;
	}

	private void compact(List<InstanceEvent> events) {
		BinaryOperator<InstanceEvent> latestEvent = (e1, e2) -> (e1.getVersion() > e2.getVersion()) ? e1 : e2;
		Map<Class<?>, Optional<InstanceEvent>> latestPerType = events.stream()
			.collect(groupingBy(InstanceEvent::getClass, reducing(latestEvent)));
		events.removeIf((e) -> !Objects.equals(e, latestPerType.get(e.getClass()).orElse(null)));
	}

	private OptimisticLockingException createOptimisticLockException(InstanceEvent event, long lastVersion) {
		return new OptimisticLockingException(
				"Version " + event.getVersion() + " was overtaken by " + lastVersion + " for " + event.getInstance());
	}

	protected static long getLastVersion(List<InstanceEvent> events) {
		return events.isEmpty() ? -1 : events.get(events.size() - 1).getVersion();
	}

}
