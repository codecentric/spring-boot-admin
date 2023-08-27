/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.services;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;

public class AbstractEventHandlerTest {

	private static final Logger log = LoggerFactory.getLogger(AbstractEventHandlerTest.class);

	private static final Registration registration = Registration.create("foo", "http://health").build();

	private static final InstanceRegisteredEvent firstEvent = new InstanceRegisteredEvent(InstanceId.of("id"), 0L,
			registration);

	private static final InstanceRegisteredEvent secondEvent = new InstanceRegisteredEvent(InstanceId.of("id"), 1L,
			registration);

	private static final InstanceRegisteredEvent errorEvent = new InstanceRegisteredEvent(InstanceId.of("err"), 2L,
			registration);

	private static final InstanceDeregisteredEvent ignoredEvent = new InstanceDeregisteredEvent(InstanceId.of("id"),
			2L);

	@Test
	public void should_resubscribe_after_error() {
		TestPublisher<InstanceEvent> testPublisher = TestPublisher.create();

		TestEventHandler eventHandler = new TestEventHandler(testPublisher.flux());
		eventHandler.start();

		StepVerifier.create(eventHandler.getFlux())
			.expectSubscription()
			.then(() -> testPublisher.next(firstEvent, errorEvent, secondEvent))
			.expectNext(firstEvent, secondEvent)
			.thenCancel()
			.verify(Duration.ofSeconds(2));
	}

	@Test
	public void should_filter() {
		TestPublisher<InstanceEvent> testPublisher = TestPublisher.create();

		TestEventHandler eventHandler = new TestEventHandler(testPublisher.flux());
		eventHandler.start();

		StepVerifier.create(eventHandler.getFlux())
			.expectSubscription()
			.then(() -> testPublisher.next(firstEvent, ignoredEvent, secondEvent))
			.expectNext(firstEvent, secondEvent)
			.thenCancel()
			.verify(Duration.ofSeconds(1));
	}

	public static final class TestEventHandler extends AbstractEventHandler<InstanceRegisteredEvent> {

		private final Sinks.Many<InstanceEvent> unicast;

		private final Flux<InstanceEvent> flux;

		private TestEventHandler(Publisher<InstanceEvent> publisher) {
			super(publisher, InstanceRegisteredEvent.class);
			this.unicast = Sinks.many().unicast().onBackpressureBuffer();
			this.flux = this.unicast.asFlux();
		}

		@Override
		protected Publisher<Void> handle(Flux<InstanceRegisteredEvent> publisher) {
			return publisher.flatMap((event) -> {
				if (event.equals(errorEvent)) {
					return Mono.error(new IllegalStateException("TestError"));
				}
				else {
					log.info("Event {}", event);
					this.unicast.tryEmitNext(event);
					return Mono.empty();
				}
			}).then();
		}

		public Flux<InstanceEvent> getFlux() {
			return this.flux;
		}

	}

}
