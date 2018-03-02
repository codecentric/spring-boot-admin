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

package de.codecentric.boot.admin.server.domain.entities;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InstanceRepository storing instances using an event log.
 *
 * @author Johannes Edmeier
 */
public class SnapshottingInstanceRepository extends EventsourcingInstanceRepository {
    private static final Logger log = LoggerFactory.getLogger(SnapshottingInstanceRepository.class);
    private final ConcurrentMap<InstanceId, Instance> snapshots = new ConcurrentHashMap<>();
    private Disposable subscription;
    private final Retry<Object> retryOnAny = Retry.any()
                                                  .retryMax(Integer.MAX_VALUE)
                                                  .doOnRetry(ctx -> log.error("Resubscribing after uncaught error",
                                                      ctx.exception()));

    public SnapshottingInstanceRepository(InstanceEventStore eventStore) {
        super(eventStore);
    }

    @Override
    public Flux<Instance> findAll() {
        return Mono.fromSupplier(snapshots::values).flatMapIterable(Function.identity());
    }

    @Override
    public Mono<Instance> find(InstanceId id) {
        return Mono.defer(() -> Mono.justOrEmpty(snapshots.get(id)));
    }

    @Override
    public Flux<Instance> findByName(String name) {
        return findAll().filter(a -> a.isRegistered() && name.equals(a.getRegistration().getName()));
    }

    public void start() {
        this.subscription = getEventStore().findAll()
                                           .concatWith(getEventStore())
                                           .doOnNext(this::updateSnapshot)
                                           .retryWhen(retryOnAny)
                                           .subscribe();
    }

    public void stop() {
        if (this.subscription != null) {
            this.subscription.dispose();
        }
    }

    protected void updateSnapshot(InstanceEvent event) {
        snapshots.compute(event.getInstance(), (key, old) -> {
            Instance instance = old != null ? old : Instance.create(key);
            return instance.apply(event);
        });
    }
}
