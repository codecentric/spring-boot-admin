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

package de.codecentric.boot.admin.server.registry;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.utils.reactive.ReactiveUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.logging.Level;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusUpdateTrigger {
    private static final Logger log = LoggerFactory.getLogger(StatusUpdateTrigger.class);
    private final Publisher<ClientApplicationEvent> events;
    private final StatusUpdater statusUpdater;
    private long updateInterval = 10_000L;
    private Disposable eventSubscription;
    private Disposable intervalSubscription;

    public StatusUpdateTrigger(StatusUpdater statusUpdater, Publisher<ClientApplicationEvent> events) {
        this.statusUpdater = statusUpdater;
        this.events = events;
    }

    public void start() {
        log.debug("Subscribed to {} events for status updates", ClientApplicationRegisteredEvent.class);
        eventSubscription = Flux.from(events)
                                .log(log.getName(), Level.FINEST)
                                .subscribeOn(Schedulers.newSingle("status-updater"))
                                .ofType(ClientApplicationRegisteredEvent.class)
                                .cast(ClientApplicationRegisteredEvent.class)
                                .doOnNext(this::updateStatus).retryWhen(ReactiveUtils.logAndRetryAny(log))
                                .subscribe();

        log.debug("Scheduled status update every {}ms", updateInterval);
        intervalSubscription = Flux.interval(Duration.ofMillis(updateInterval))
                                   .log(log.getName(), Level.FINEST)
                                   .subscribeOn(Schedulers.newSingle("status-monitor"))
                                   .doOnNext((i) -> this.updateStatusForAllApplications())
                                   .retryWhen(ReactiveUtils.logAndRetryAny(log))
                                   .subscribe();
    }

    public void stop() {
        if (eventSubscription != null) {
            eventSubscription.dispose();
        }
        if (intervalSubscription != null) {
            intervalSubscription.dispose();
        }
    }

    protected void updateStatusForAllApplications() {
        statusUpdater.updateStatusForAllApplications();
    }

    protected void updateStatus(ClientApplicationRegisteredEvent event) {
        statusUpdater.updateStatus(event.getApplication());
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }
}
