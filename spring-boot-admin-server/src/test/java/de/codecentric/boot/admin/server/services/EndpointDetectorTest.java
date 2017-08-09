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

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.EventSourcingApplicationRepository;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.services.endpoints.EndpointDetectionStrategy;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EndpointDetectorTest {
    private EndpointDetector detector;
    private EventSourcingApplicationRepository repository;
    private ConcurrentMapEventStore eventStore;
    private EndpointDetectionStrategy strategy;

    @Before
    public void setup() {
        eventStore = new InMemoryEventStore();
        repository = new EventSourcingApplicationRepository(eventStore);
        repository.start();
        strategy = mock(EndpointDetectionStrategy.class);
        detector = new EndpointDetector(repository, strategy);
    }

    @Test
    public void should_update_endpoints() {
        //given
        Registration registration = Registration.create("foo", "http://health").managementUrl("http://mgmt").build();
        Application application = Application.create(ApplicationId.of("onl"))
                                             .register(registration)
                                             .withStatusInfo(StatusInfo.ofUp());
        StepVerifier.create(repository.save(application)).verifyComplete();

        Application noActuator = Application.create(ApplicationId.of("noActuator"))
                                            .register(Registration.create("foo", "http://health").build())
                                            .withStatusInfo(StatusInfo.ofUp());
        StepVerifier.create(repository.save(noActuator)).verifyComplete();

        Application offline = Application.create(ApplicationId.of("off"))
                                         .register(registration)
                                         .withStatusInfo(StatusInfo.ofOffline());
        StepVerifier.create(repository.save(offline)).verifyComplete();

        Application unknown = Application.create(ApplicationId.of("unk"))
                                         .register(registration)
                                         .withStatusInfo(StatusInfo.ofUnknown());
        StepVerifier.create(repository.save(unknown)).verifyComplete();

        when(strategy.detectEndpoints(any(Application.class))).thenReturn(Mono.just(Endpoints.single("id", "url")));

        //when/then
        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(detector.detectEndpoints(offline.getId())).verifyComplete())
                    .then(() -> StepVerifier.create(detector.detectEndpoints(unknown.getId())).verifyComplete())
                    .then(() -> StepVerifier.create(detector.detectEndpoints(noActuator.getId())).verifyComplete())
                    .expectNoEvent(Duration.ofMillis(10L))
                    .then(() -> StepVerifier.create(detector.detectEndpoints(application.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(ClientApplicationEndpointsDetectedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(application.getId()))
                    .assertNext(app -> assertThat(app.getEndpoints()).isEqualTo(Endpoints.single("id", "url")))
                    .verifyComplete();
    }

}