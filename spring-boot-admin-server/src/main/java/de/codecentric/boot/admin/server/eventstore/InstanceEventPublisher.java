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

package de.codecentric.boot.admin.server.eventstore;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.UnicastProcessor;

import java.util.List;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceEventPublisher implements Publisher<InstanceEvent> {
    private static final Logger log = LoggerFactory.getLogger(InstanceEventPublisher.class);
    private final Flux<InstanceEvent> publishedFlux;
    private final FluxSink<InstanceEvent> sink;

    protected InstanceEventPublisher() {
        UnicastProcessor<InstanceEvent> unicastProcessor = UnicastProcessor.create();
        this.publishedFlux = unicastProcessor.publish().autoConnect(0);
        this.sink = unicastProcessor.sink();
    }

    protected void publish(List<InstanceEvent> events) {
        events.forEach(event -> {
            log.debug("Event published {}", event);
            this.sink.next(event);
        });
    }

    @Override
    public void subscribe(Subscriber<? super InstanceEvent> s) {
        publishedFlux.subscribe(s);
    }
}
