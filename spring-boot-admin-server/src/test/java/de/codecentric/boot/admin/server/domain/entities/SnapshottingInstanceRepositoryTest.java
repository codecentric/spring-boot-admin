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

package de.codecentric.boot.admin.server.domain.entities;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Subscriber;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SnapshottingInstanceRepositoryTest extends AbstractInstanceRepositoryTest<SnapshottingInstanceRepository> {
    public SnapshottingInstanceRepositoryTest() {
        super(new SnapshottingInstanceRepository(new InMemoryEventStore()));
    }

    @Before
    public void setUp() {
        this.repository.start();
    }

    @After
    public void tearDown() {
        this.repository.stop();
    }

    @Test
    public void should_recover_after_faulty_event_order() {
        //given a registered known instance 'foo'
        Instance instanceFoo = Instance.create(InstanceId.of("foo"))
                                       .register(Registration.create("name", "http://health").build())
                                       .withStatusInfo(StatusInfo.ofDown());
        InstanceEventStore eventStore = mock(InstanceEventStore.class);
        when(eventStore.findAll()).thenReturn(Flux.fromIterable(instanceFoo.getUnsavedEvents()));
        when(eventStore.find(instanceFoo.getId())).thenReturn(Flux.fromIterable(instanceFoo.getUnsavedEvents()));

        //and a second not known instance 'bar'
        Instance instanceBar = Instance.create(InstanceId.of("bar"))
                                       .register(Registration.create("second", "http://second/health").build());

        //when recieving a duplicated event for instance 'foo'
        //and a events for instance 'bar'
        doAnswer(invocation -> {
            Mono.just((InstanceEvent) new InstanceStatusChangedEvent(instanceFoo.getId(),
                instanceFoo.getVersion(),
                StatusInfo.ofOffline()
            ))
                .concatWith(Flux.fromIterable(instanceBar.getUnsavedEvents()))
                .subscribe(invocation.<Subscriber<InstanceEvent>>getArgument(0));
            return null;
        }).when(eventStore).subscribe(any());

        SnapshottingInstanceRepository repositoryWithMock = new SnapshottingInstanceRepository(eventStore);
        repositoryWithMock.start();

        try {
            //then the snapshot for instance 'foo'
            StepVerifier.create(repositoryWithMock.find(instanceFoo.getId()))
                        .expectNext(instanceFoo.clearUnsavedEvents())
                        .verifyComplete();
            //and bar are available
            StepVerifier.create(repositoryWithMock.find(instanceBar.getId()))
                        .expectNext(instanceBar.clearUnsavedEvents())
                        .verifyComplete();
        } finally {
            repositoryWithMock.stop();
        }
    }
}
