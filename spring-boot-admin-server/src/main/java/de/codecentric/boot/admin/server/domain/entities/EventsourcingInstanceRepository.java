/*
 * Copyright 2014-2019 the original author or authors.
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
import de.codecentric.boot.admin.server.eventstore.OptimisticLockingException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import java.util.function.BiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InstanceRepository storing instances using an event log.
 *
 * @author Johannes Edmeier
 */
public class EventsourcingInstanceRepository implements InstanceRepository {
    private static final Logger log = LoggerFactory.getLogger(EventsourcingInstanceRepository.class);
    private final InstanceEventStore eventStore;
    private final Retry<?> retryOptimisticLockException = Retry.anyOf(OptimisticLockingException.class)
                                                               .retryMax(10)
                                                               .doOnRetry(ctx -> log.debug(
                                                                   "Retrying after OptimisticLockingException",
                                                                   ctx.exception()
                                                               ));

    public EventsourcingInstanceRepository(InstanceEventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public Mono<Instance> save(Instance instance) {
        return this.eventStore.append(instance.getUnsavedEvents()).then(Mono.just(instance.clearUnsavedEvents()));
    }

    @Override
    public Flux<Instance> findAll() {
        return this.eventStore.findAll()
                              .groupBy(InstanceEvent::getInstance)
                              .flatMap(f -> f.reduce(Instance.create(f.key()), Instance::apply));
    }

    @Override
    public Mono<Instance> find(InstanceId id) {
        return this.eventStore.find(id).collectList().filter(e -> !e.isEmpty()).map(e -> Instance.create(id).apply(e));
    }

    @Override
    public Flux<Instance> findByName(String name) {
        return findAll().filter(a -> a.isRegistered() && name.equals(a.getRegistration().getName()));
    }

    @Override
    public Mono<Instance> compute(InstanceId id, BiFunction<InstanceId, Instance, Mono<Instance>> remappingFunction) {
        return this.find(id)
                   .flatMap(application -> remappingFunction.apply(id, application))
                   .switchIfEmpty(Mono.defer(() -> remappingFunction.apply(id, null)))
                   .flatMap(this::save)
                   .retryWhen(this.retryOptimisticLockException);
    }

    @Override
    public Mono<Instance> computeIfPresent(InstanceId id,
                                           BiFunction<InstanceId, Instance, Mono<Instance>> remappingFunction) {
        return this.find(id)
                   .flatMap(application -> remappingFunction.apply(id, application))
                   .flatMap(this::save)
                   .retryWhen(this.retryOptimisticLockException);
    }
}
