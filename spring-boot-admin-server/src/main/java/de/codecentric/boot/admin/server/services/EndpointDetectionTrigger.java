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

package de.codecentric.boot.admin.server.services;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.reactivestreams.Publisher;

public class EndpointDetectionTrigger extends AbstractEventHandler<InstanceEvent> {
    private final EndpointDetector endpointDetector;
    private Scheduler scheduler;

    public EndpointDetectionTrigger(EndpointDetector endpointDetector, Publisher<InstanceEvent> publisher) {
        super(publisher, InstanceEvent.class);
        this.endpointDetector = endpointDetector;
    }

    @Override
    public void start() {
        assert scheduler == null;
        scheduler = Schedulers.newSingle("endpoint-detector");
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        if (scheduler != null) {
            scheduler.dispose();
            scheduler = null;
        }
    }

    @Override
    protected Publisher<Void> handle(Flux<InstanceEvent> publisher) {
        return publisher.subscribeOn(scheduler)
                        .filter(event -> event instanceof InstanceStatusChangedEvent ||
                                         event instanceof InstanceRegistrationUpdatedEvent)
                        .flatMap(this::detectEndpoints);
    }

    protected Mono<Void> detectEndpoints(InstanceEvent event) {
        return endpointDetector.detectEndpoints(event.getInstance());
    }
}
