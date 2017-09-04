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

package de.codecentric.boot.admin.server.services;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusUpdateTrigger extends ResubscribingEventHandler<InstanceRegisteredEvent> {
    private static final Logger log = LoggerFactory.getLogger(StatusUpdateTrigger.class);
    private final StatusUpdater statusUpdater;
    private Map<InstanceId, Long> lastQueried = new HashMap<>();
    private long updateInterval = 10_000L;
    private long statusLifetime = 10_000L;
    private Disposable intervalSubscription;


    public StatusUpdateTrigger(StatusUpdater statusUpdater, Publisher<InstanceEvent> publisher) {
        super(publisher, InstanceRegisteredEvent.class);
        this.statusUpdater = statusUpdater;
    }

    @Override
    public void start() {
        super.start();
        intervalSubscription = Flux.interval(Duration.ofMillis(updateInterval))
                                   .doOnSubscribe(subscription -> log.debug("Scheduled status update every {}ms",
                                           updateInterval))
                                   .log(log.getName(), Level.FINEST)
                                   .subscribeOn(Schedulers.newSingle("status-monitor"))
                                   .flatMap((i) -> this.updateStatusForAllInstances())
                                   .retryWhen(Retry.any()
                                                   .retryMax(Integer.MAX_VALUE)
                                                   .doOnRetry(ctx -> log.error("Resubscribing after uncaught error",
                                                           ctx.exception())))
                                   .subscribe();
    }

    @Override
    protected Publisher<?> handle(Flux<InstanceRegisteredEvent> publisher) {
        return publisher.subscribeOn(Schedulers.newSingle("status-updater"))
                        .flatMap(event -> updateStatus(event.getInstance()));
    }

    @Override
    public void stop() {
        super.stop();
        if (intervalSubscription != null) {
            intervalSubscription.dispose();
        }
    }

    protected Mono<Void> updateStatusForAllInstances() {
        log.debug("Updating status for all instances");
        long expiryInstant = System.currentTimeMillis() - statusLifetime;
        return Flux.fromIterable(lastQueried.entrySet())
                   .filter(e -> e.getValue() < expiryInstant)
                   .map(Map.Entry::getKey)
                   .flatMap(this::updateStatus)
                   .then();
    }

    protected Mono<Void> updateStatus(InstanceId instanceId) {
        return statusUpdater.updateStatus(instanceId)
                            .doFinally((s) -> lastQueried.put(instanceId, System.currentTimeMillis()));
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }

    public void setStatusLifetime(long statusLifetime) {
        this.statusLifetime = statusLifetime;
    }
}
