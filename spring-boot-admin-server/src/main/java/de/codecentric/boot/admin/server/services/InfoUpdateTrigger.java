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

package de.codecentric.boot.admin.server.services;

import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.reactivestreams.Publisher;

public class InfoUpdateTrigger extends AbstractEventHandler<InstanceEvent> {
    private final InfoUpdater infoUpdater;

    public InfoUpdateTrigger(InfoUpdater infoUpdater, Publisher<InstanceEvent> publisher) {
        super(publisher, InstanceEvent.class);
        this.infoUpdater = infoUpdater;
    }

    @Override
    protected Publisher<Void> handle(Flux<InstanceEvent> publisher) {
        Scheduler scheduler = Schedulers.newSingle("info-updater");
        return publisher.subscribeOn(scheduler)
                        .filter(event -> event instanceof InstanceEndpointsDetectedEvent ||
                                         event instanceof InstanceStatusChangedEvent ||
                                         event instanceof InstanceRegistrationUpdatedEvent)
                        .flatMap(this::updateInfo)
                        .doFinally(s -> scheduler.dispose());
    }

    protected Mono<Void> updateInfo(InstanceEvent event) {
        return infoUpdater.updateInfo(event.getInstance());
    }
}
