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

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.UnicastProcessor;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractEventHandlerTest {
    private static final Logger log = LoggerFactory.getLogger(AbstractEventHandlerTest.class);
    private static final Registration registration = Registration.create("foo", "http://health").build();
    private static final InstanceRegisteredEvent event = new InstanceRegisteredEvent(InstanceId.of("id"),
        0L,
        registration
    );
    private static final InstanceRegisteredEvent errorEvent = new InstanceRegisteredEvent(InstanceId.of("err"),
        0L,
        registration
    );
    private static final InstanceDeregisteredEvent ignoredEvent = new InstanceDeregisteredEvent(InstanceId.of("id"),
        1L
    );

    @Test
    public void should_resubscribe_after_error() {
        TestPublisher<InstanceEvent> testPublisher = TestPublisher.create();

        TestEventHandler eventHandler = new TestEventHandler(testPublisher.flux());
        eventHandler.start();

        StepVerifier.create(eventHandler.getFlux())
                    .expectSubscription()
                    .then(() -> testPublisher.next(event))
                    .expectNext(event)
                    .then(() -> testPublisher.next(errorEvent))
                    .expectNoEvent(Duration.ofMillis(100L))
                    .then(() -> testPublisher.next(event))
                    .expectNext(event)
                    .thenCancel()
                    .verify(Duration.ofSeconds(5));

    }

    @Test
    public void should_filter() {
        TestPublisher<InstanceEvent> testPublisher = TestPublisher.create();

        TestEventHandler eventHandler = new TestEventHandler(testPublisher.flux());
        eventHandler.start();

        StepVerifier.create(eventHandler.getFlux())
                    .expectSubscription()
                    .then(() -> testPublisher.next(event))
                    .expectNext(event)
                    .then(() -> testPublisher.next(ignoredEvent))
                    .expectNoEvent(Duration.ofMillis(100L))
                    .thenCancel()
                    .verify(Duration.ofSeconds(5));
    }


    private static class TestEventHandler extends AbstractEventHandler<InstanceRegisteredEvent> {
        private final FluxSink<InstanceEvent> sink;
        private final Flux<InstanceEvent> flux;

        protected TestEventHandler(Publisher<InstanceEvent> publisher) {
            super(publisher, InstanceRegisteredEvent.class);
            UnicastProcessor<InstanceEvent> processor = UnicastProcessor.create();
            this.sink = processor.sink();
            this.flux = processor;
        }

        @Override
        protected Publisher<Void> handle(Flux<InstanceRegisteredEvent> publisher) {
            return publisher.doOnNext(event -> {
                if (event.equals(errorEvent)) {
                    throw new IllegalStateException("Error");
                } else {
                    log.info("Event {}", event);
                    sink.next(event);
                }
            }).then();
        }

        public Flux<InstanceEvent> getFlux() {
            return flux;
        }
    }

}
