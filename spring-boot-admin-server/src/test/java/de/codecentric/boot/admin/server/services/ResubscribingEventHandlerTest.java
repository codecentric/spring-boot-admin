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

import de.codecentric.boot.admin.server.domain.events.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
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

public class ResubscribingEventHandlerTest {
    private static final Logger log = LoggerFactory.getLogger(ResubscribingEventHandlerTest.class);
    private static final Registration registration = Registration.create("foo", "http://health").build();
    private static final ClientApplicationRegisteredEvent event = new ClientApplicationRegisteredEvent(
            ApplicationId.of("id"), 0L, registration);
    private static final ClientApplicationRegisteredEvent errorEvent = new ClientApplicationRegisteredEvent(
            ApplicationId.of("err"), 0L, registration);
    private static final ClientApplicationDeregisteredEvent ignoredEvent = new ClientApplicationDeregisteredEvent(
            ApplicationId.of("id"), 1L);

    @Test
    public void should_resubscribe_after_error() {
        TestPublisher<ClientApplicationEvent> testPublisher = TestPublisher.create();

        TestEventHandler eventHandler = new TestEventHandler(testPublisher.flux());
        eventHandler.start();

        StepVerifier.create(eventHandler.getFlux())
                    .expectSubscription()
                    .then(() -> testPublisher.next(event))
                    .expectNext(event)
                    .then(() -> testPublisher.next(errorEvent))
                    .expectNoEvent(Duration.ofMillis(10L))
                    .then(() -> testPublisher.next(event))
                    .expectNext(event)
                    .thenCancel()
                    .verify(Duration.ofSeconds(5));

    }

    @Test
    public void should_filter() {
        TestPublisher<ClientApplicationEvent> testPublisher = TestPublisher.create();

        TestEventHandler eventHandler = new TestEventHandler(testPublisher.flux());
        eventHandler.start();

        StepVerifier.create(eventHandler.getFlux())
                    .expectSubscription()
                    .then(() -> testPublisher.next(event))
                    .expectNext(event)
                    .then(() -> testPublisher.next(ignoredEvent))
                    .expectNoEvent(Duration.ofMillis(10L))
                    .thenCancel()
                    .verify(Duration.ofSeconds(5));
    }


    private static class TestEventHandler extends ResubscribingEventHandler<ClientApplicationRegisteredEvent> {
        private final FluxSink<ClientApplicationEvent> sink;
        private final Flux<ClientApplicationEvent> flux;

        protected TestEventHandler(Publisher<ClientApplicationEvent> publisher) {
            super(publisher, ClientApplicationRegisteredEvent.class);
            UnicastProcessor<ClientApplicationEvent> processor = UnicastProcessor.create();
            this.sink = processor.sink();
            this.flux = processor;
        }

        @Override
        protected Publisher<?> handle(Flux<ClientApplicationRegisteredEvent> publisher) {
            return publisher.doOnNext(event -> {
                if (event.equals(errorEvent)) {
                    throw new IllegalStateException("Error");
                } else {
                    log.info("Event {}", event);
                    sink.next(event);
                }
            });
        }

        public Flux<ClientApplicationEvent> getFlux() {
            return flux;
        }
    }

}