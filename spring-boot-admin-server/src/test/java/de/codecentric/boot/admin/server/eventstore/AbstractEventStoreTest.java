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
package de.codecentric.boot.admin.server.eventstore;

import de.codecentric.boot.admin.server.domain.events.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.LongStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractEventStoreTest {
    private static final Logger log = LoggerFactory.getLogger(AbstractEventStoreTest.class);
    private final ApplicationId id = ApplicationId.of("id");
    private final Registration registration = Registration.create("foo", "http://health").build();

    protected abstract ClientApplicationEventStore createStore(int maxLogSizePerAggregate);

    @Test
    public void should_store_events() throws InterruptedException {
        ClientApplicationEventStore store = createStore(100);
        StepVerifier.create(store.findAll()).verifyComplete();

        ClientApplicationEvent event1 = new ClientApplicationRegisteredEvent(id, 0L, registration);
        ClientApplicationEvent eventOther = new ClientApplicationRegisteredEvent(ApplicationId.of("other"), 0L,
                registration);
        Thread.sleep(5L); // get later timestamp
        ClientApplicationEvent event2 = new ClientApplicationDeregisteredEvent(id, 1L);

        StepVerifier.create(store).expectSubscription().then(() -> {
            StepVerifier.create(store.append(singletonList(event1))).verifyComplete();
            StepVerifier.create(store.append(singletonList(eventOther))).verifyComplete();
            StepVerifier.create(store.append(singletonList(event2))).verifyComplete();
        }).expectNext(event1, eventOther, event2).thenCancel().verify(Duration.ofMillis(250L));

        StepVerifier.create(store.findAll()).expectNext(event1, eventOther, event2).verifyComplete();
        StepVerifier.create(store.find(id)).expectNext(event1, event2).verifyComplete();
        StepVerifier.create(store.find(ApplicationId.of("-"))).verifyComplete();
    }

    @Test
    public void should_shorten_log_on_exceeded_capacity() {
        ClientApplicationEventStore store = createStore(2);

        ClientApplicationEvent event1 = new ClientApplicationRegisteredEvent(id, 0L, registration);
        ClientApplicationEvent event2 = new ClientApplicationStatusChangedEvent(id, 1L, StatusInfo.ofDown());
        ClientApplicationEvent event3 = new ClientApplicationStatusChangedEvent(id, 2L, StatusInfo.ofUp());

        StepVerifier.create(store.append(asList(event1, event2, event3))).verifyComplete();

        StepVerifier.create(store.findAll()).expectNext(event1, event3).verifyComplete();
    }

    @Test
    public void should_throw_optimictic_locking_exception() {
        ClientApplicationEvent event0 = new ClientApplicationRegisteredEvent(id, 0L, registration);
        ClientApplicationEvent event1 = new ClientApplicationStatusChangedEvent(id, 1L, StatusInfo.ofDown());
        ClientApplicationEvent event1b = new ClientApplicationDeregisteredEvent(id, 1L);

        ClientApplicationEventStore store = createStore(100);
        StepVerifier.create(store.append(asList(event0, event1))).verifyComplete();

        StepVerifier.create(store.append(singletonList(event1b))).verifyError(OptimisticLockingException.class);
    }


    @Test
    public void concurrent_read_writes() {
        ApplicationId id = ApplicationId.of("a");
        ClientApplicationEventStore store = createStore(1000);

        Function<Integer, ClientApplicationEvent> eventFactory = i -> new ClientApplicationDeregisteredEvent(id, i);
        Flux<Void> eventgenerator = Flux.range(0, 1000)
                                        .map(eventFactory)
                                        .buffer(2)
                                        .flatMap(events -> store.append(events)
                                                                .onErrorResume(OptimisticLockingException.class,
                                                                        (ex) -> {
                                                                            log.info("skipped {}", ex.getMessage());
                                                                            return Mono.empty();
                                                                        })
                                                                .delayElement(Duration.ofMillis(5L)));

        StepVerifier.create(eventgenerator.subscribeOn(Schedulers.newSingle("a"))
                                          .mergeWith(eventgenerator.subscribeOn(Schedulers.newSingle("a")))
                                          .mergeWith(eventgenerator.subscribeOn(Schedulers.newSingle("a")))
                                          .mergeWith(eventgenerator.subscribeOn(Schedulers.newSingle("a")))
                                          .then()).verifyComplete();

        List<Long> versions = store.find(id).map(ClientApplicationEvent::getVersion).collectList().block();
        List<Long> expected = LongStream.range(0, 1000).boxed().collect(toList());
        assertThat(versions).containsExactlyElementsOf(expected);
    }
}
