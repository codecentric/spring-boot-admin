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
import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.logging.Level;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfoUpdateTrigger {
    private static final Logger log = LoggerFactory.getLogger(InfoUpdateTrigger.class);
    private final Publisher<ClientApplicationEvent> events;
    private final InfoUpdater infoUpdater;
    private Disposable subscription;

    public InfoUpdateTrigger(InfoUpdater infoUpdater, Publisher<ClientApplicationEvent> events) {
        this.infoUpdater = infoUpdater;
        this.events = events;
    }

    public void start() {
        log.debug("Subscribed to {} events for info updates", ClientApplicationStatusChangedEvent.class);
        subscription = Flux.from(events)
                           .log(log.getName(), Level.FINEST)
                           .subscribeOn(Schedulers.newSingle("info-updater"))
                           .ofType(ClientApplicationStatusChangedEvent.class)
                           .cast(ClientApplicationStatusChangedEvent.class)
                           .doOnNext(this::updateInfo)
                           .retry()
                           .subscribe();
    }

    public void stop() {
        if (subscription != null) {
            subscription.dispose();
        }
    }

    protected void updateInfo(ClientApplicationStatusChangedEvent event) {
        infoUpdater.updateInfo(event.getApplication());
    }
}
