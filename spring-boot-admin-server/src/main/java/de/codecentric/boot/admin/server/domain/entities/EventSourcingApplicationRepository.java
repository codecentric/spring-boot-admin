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
package de.codecentric.boot.admin.server.domain.entities;

import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.eventstore.ClientApplicationEventStore;
import de.codecentric.boot.admin.server.eventstore.OptimisticLockingException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ApplicationRepository storing Applications using an event log.
 *
 * @author Johannes Edmeier
 */
public class EventSourcingApplicationRepository implements ApplicationRepository {
    private static final Logger log = LoggerFactory.getLogger(EventSourcingApplicationRepository.class);
    private final ConcurrentMap<ApplicationId, Application> snapshots = new ConcurrentHashMap<>();
    private final ClientApplicationEventStore eventStore;
    private Disposable subscription;
    private final Retry<Object> retryOnAny = Retry.any()
                                                  .retryMax(Integer.MAX_VALUE)
                                                  .doOnRetry(ctx -> log.error("Resubscribing after uncaught error",
                                                          ctx.exception()));
    private final Retry<?> retryOnOptimisticLockException = Retry.anyOf(OptimisticLockingException.class)
                                                                 .fixedBackoff(Duration.ofMillis(50L))
                                                                 .retryMax(10)
                                                                 .doOnRetry(ctx -> log.debug(
                                                                         "Retrying after OptimisticLockingException",
                                                                         ctx.exception()));
    ;

    public EventSourcingApplicationRepository(ClientApplicationEventStore eventStore) {
        this.eventStore = eventStore;
    }

    @Override
    public Mono<Void> save(Application application) {
        return eventStore.append(application.getUnsavedEvents()).then();
    }

    @Override
    public Flux<Application> findAll() {
        return Mono.fromSupplier(snapshots::values).flatMapIterable(Function.identity());
    }

    @Override
    public Mono<Application> find(ApplicationId id) {
        return Mono.defer(() -> Mono.justOrEmpty(snapshots.get(id)));
    }

    @Override
    public Flux<Application> findByName(String name) {
        return findAll().filter(a -> a.isRegistered() && name.equals(a.getRegistration().getName()));
    }

    public void start() {
        this.subscription = Flux.from(eventStore).doOnNext(this::updateSnapshot).retryWhen(retryOnAny).subscribe();
    }

    public void stop() {
        if (this.subscription != null) {
            this.subscription.dispose();
        }
    }

    protected void updateSnapshot(ClientApplicationEvent event) {
        snapshots.compute(event.getApplication(), (key, old) -> {
            Application application = old != null ? old : Application.create(key);
            return application.apply(event, false);
        });
    }

    @Override
    public Mono<Void> compute(ApplicationId id,
                              BiFunction<ApplicationId, Application, Mono<Application>> remappingFunction) {
        return this.find(id)
                   .flatMap(application -> remappingFunction.apply(id, application))
                   .switchIfEmpty(Mono.defer(() -> remappingFunction.apply(id, null)))
                   .flatMap(this::save)
                   .retryWhen(retryOnOptimisticLockException);
    }

    @Override
    public Mono<Void> computeIfPresent(ApplicationId id,
                                       BiFunction<ApplicationId, Application, Mono<Application>> remappingFunction) {
        return this.find(id)
                   .flatMap(application -> remappingFunction.apply(id, application))
                   .flatMap(this::save)
                   .retryWhen(retryOnOptimisticLockException);
    }
}
