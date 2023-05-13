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

package de.codecentric.boot.admin.server.eventstore;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.stream.LongStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractEventStoreTest {

	private static final Logger log = LoggerFactory.getLogger(AbstractEventStoreTest.class);

	private final InstanceId id = InstanceId.of("id");

	private final Registration registration = Registration.create("foo", "http://health")
		.metadata("test", "dummy")
		.build();

	protected abstract InstanceEventStore createStore(int maxLogSizePerAggregate);

	protected abstract void shutdownStore();

	@AfterEach
	void tearDown() {
		this.shutdownStore();
	}

	@Test
	public void should_store_events() {
		InstanceEventStore store = createStore(100);
		StepVerifier.create(store.findAll()).verifyComplete();

		Instant now = Instant.now();
		InstanceEvent event1 = new InstanceRegisteredEvent(id, 0L, now, registration);
		InstanceEvent eventOther = new InstanceRegisteredEvent(InstanceId.of("other"), 0L, now.plusMillis(10),
				registration);
		InstanceEvent event2 = new InstanceDeregisteredEvent(id, 1L, now.plusMillis(20));

		StepVerifier.create(store)
			.expectSubscription()
			.then(() -> StepVerifier.create(store.append(singletonList(event1))).verifyComplete())
			.expectNext(event1)
			.then(() -> StepVerifier.create(store.append(singletonList(eventOther))).verifyComplete())
			.expectNext(eventOther)
			.then(() -> StepVerifier.create(store.append(singletonList(event2))).verifyComplete())
			.expectNext(event2)
			.thenCancel()
			.verify();

		StepVerifier.create(store.find(id)).expectNext(event1, event2).verifyComplete();
		StepVerifier.create(store.find(InstanceId.of("-"))).verifyComplete();
		StepVerifier.create(store.findAll()).expectNext(event1, eventOther, event2).verifyComplete();
	}

	@Test
	public void should_shorten_log_on_exceeded_capacity() {
		InstanceEventStore store = createStore(2);

		InstanceEvent event1 = new InstanceRegisteredEvent(id, 0L, registration);
		InstanceEvent event2 = new InstanceStatusChangedEvent(id, 1L, StatusInfo.ofDown());
		InstanceEvent event3 = new InstanceStatusChangedEvent(id, 2L, StatusInfo.ofUp());

		StepVerifier.create(store.append(asList(event1, event2, event3))).verifyComplete();

		StepVerifier.create(store.findAll()).expectNext(event1, event3).verifyComplete();
	}

	@Test
	public void should_throw_optimictic_locking_exception() {
		InstanceEvent event0 = new InstanceRegisteredEvent(id, 0L, registration);
		InstanceEvent event1 = new InstanceStatusChangedEvent(id, 1L, StatusInfo.ofDown());
		InstanceEvent event1b = new InstanceDeregisteredEvent(id, 1L);

		InstanceEventStore store = createStore(100);
		StepVerifier.create(store.append(asList(event0, event1))).verifyComplete();

		StepVerifier.create(store.append(singletonList(event1b))).verifyError(OptimisticLockingException.class);
	}

	@Test
	public void concurrent_read_writes() {
		InstanceId id = InstanceId.of("a");
		InstanceEventStore store = createStore(500);

		Function<Integer, InstanceEvent> eventFactory = (i) -> new InstanceDeregisteredEvent(id, i);
		Flux<Void> eventgenerator = Flux.range(0, 500)
			.map(eventFactory)
			.buffer(2)
			.flatMap((events) -> store.append(events).onErrorResume(OptimisticLockingException.class, (ex) -> {
				log.info("skipped {}", ex.getMessage());
				return Mono.empty();
			}).delayElement(Duration.ofMillis(5L)));

		StepVerifier
			.create(eventgenerator.subscribeOn(Schedulers.newSingle("a"))
				.mergeWith(eventgenerator.subscribeOn(Schedulers.newSingle("a")))
				.mergeWith(eventgenerator.subscribeOn(Schedulers.newSingle("a")))
				.mergeWith(eventgenerator.subscribeOn(Schedulers.newSingle("a")))
				.then())
			.verifyComplete();

		List<Long> versions = store.find(id).map(InstanceEvent::getVersion).collectList().block();
		List<Long> expected = LongStream.range(0, 500).boxed().collect(toList());
		assertThat(versions).containsExactlyElementsOf(expected);
	}

}
