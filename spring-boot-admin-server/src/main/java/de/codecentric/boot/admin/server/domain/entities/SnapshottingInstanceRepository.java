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

package de.codecentric.boot.admin.server.domain.entities;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.eventstore.OptimisticLockingException;

/**
 * InstanceRepository storing instances using an event log.
 *
 * @author Johannes Edmeier
 */
public class SnapshottingInstanceRepository extends EventsourcingInstanceRepository {

	private static final Logger log = LoggerFactory.getLogger(SnapshottingInstanceRepository.class);

	private final ConcurrentMap<InstanceId, Instance> snapshots = new ConcurrentHashMap<>();

	private final Set<InstanceId> oudatedSnapshots = ConcurrentHashMap.newKeySet();

	private final InstanceEventStore eventStore;

	@Nullable
	private Disposable subscription;

	public SnapshottingInstanceRepository(InstanceEventStore eventStore) {
		super(eventStore);
		this.eventStore = eventStore;
	}

	@Override
	public Flux<Instance> findAll() {
		return Mono.fromSupplier(this.snapshots::values).flatMapIterable(Function.identity());
	}

	@Override
	public Mono<Instance> find(InstanceId id) {
		return Mono.defer(() -> {
			if (!this.oudatedSnapshots.contains(id)) {
				return Mono.justOrEmpty(this.snapshots.get(id));
			}
			else {
				return rehydrateSnapshot(id).doOnSuccess((v) -> this.oudatedSnapshots.remove(v.getId()));
			}
		});
	}

	@Override
	public Mono<Instance> save(Instance instance) {
		return super.save(instance).doOnError(OptimisticLockingException.class,
				(e) -> this.oudatedSnapshots.add(instance.getId()));
	}

	public void start() {
		this.subscription = this.eventStore.findAll().concatWith(this.eventStore).subscribe(this::updateSnapshot);
	}

	public void stop() {
		if (this.subscription != null) {
			this.subscription.dispose();
			this.subscription = null;
		}
	}

	protected Mono<Instance> rehydrateSnapshot(InstanceId id) {
		return super.find(id).map((instance) -> this.snapshots.compute(id, (key, snapshot) -> {
			// check if the loaded version hasn't been already outdated by a snapshot
			if (snapshot == null || instance.getVersion() >= snapshot.getVersion()) {
				return instance;
			}
			else {
				return snapshot;
			}
		}));
	}

	protected void updateSnapshot(InstanceEvent event) {
		try {
			this.snapshots.compute(event.getInstance(), (key, old) -> {
				Instance instance = (old != null) ? old : Instance.create(key);
				if (event.getVersion() > instance.getVersion()) {
					return instance.apply(event);
				}
				return instance;
			});
		}
		catch (Exception ex) {
			log.warn("Error while updating the snapshot with event {}", event, ex);
		}
	}

}
