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
import de.codecentric.boot.admin.server.domain.events.ClientApplicationInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.web.client.ApplicationOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InfoUpdaterTest {
    private ApplicationOperations applicationOps;
    private InfoUpdater updater;
    private EventSourcingApplicationRepository repository;
    private ConcurrentMapEventStore eventStore;

    @Before
    public void setup() {
        eventStore = new InMemoryEventStore();
        repository = new EventSourcingApplicationRepository(eventStore);
        repository.start();
        applicationOps = mock(ApplicationOperations.class);
        updater = new InfoUpdater(repository, applicationOps);
    }

    @Test
    public void should_update_info_for_online_with_endpoint_only() {
        //given
        Registration registration = Registration.create("foo", "http://health").build();
        Application application = Application.create(ApplicationId.of("onl"))
                                             .register(registration).withEndpoints(Endpoints.single("info", "info"))
                                             .withStatusInfo(StatusInfo.ofUp());
        StepVerifier.create(repository.save(application)).verifyComplete();

        Application noInfo = Application.create(ApplicationId.of("noinfo"))
                                        .register(registration)
                                        .withEndpoints(Endpoints.single("beans", "beans"))
                                        .withStatusInfo(StatusInfo.ofUp());
        StepVerifier.create(repository.save(noInfo)).verifyComplete();

        Application offline = Application.create(ApplicationId.of("off"))
                                         .register(registration)
                                         .withStatusInfo(StatusInfo.ofOffline());
        StepVerifier.create(repository.save(offline)).verifyComplete();

        Application unknown = Application.create(ApplicationId.of("unk"))
                                         .register(registration)
                                         .withStatusInfo(StatusInfo.ofUnknown());
        StepVerifier.create(repository.save(unknown)).verifyComplete();

        when(applicationOps.getInfo(any(Application.class))).thenReturn(
                Mono.just(ResponseEntity.ok(singletonMap("foo", "bar"))));

        //when/then
        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateInfo(offline.getId())).verifyComplete())
                    .then(() -> StepVerifier.create(updater.updateInfo(unknown.getId())).verifyComplete())
                    .then(() -> StepVerifier.create(updater.updateInfo(noInfo.getId())).verifyComplete())
                    .expectNoEvent(Duration.ofMillis(10L))
                    .then(() -> StepVerifier.create(updater.updateInfo(application.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(ClientApplicationInfoChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(application.getId()))
                    .assertNext(app -> assertThat(app.getInfo()).isEqualTo(Info.from(singletonMap("foo", "bar"))))
                    .verifyComplete();
    }

    @Test
    public void should_clear_info_on_http_error() {
        //given
        Application application = Application.create(ApplicationId.of("onl"))
                                             .register(Registration.create("foo", "http://health").build())
                                             .withEndpoints(Endpoints.single("info", "info"))
                                             .withStatusInfo(StatusInfo.ofUp())
                                             .withInfo(Info.from(singletonMap("foo", "bar")));
        StepVerifier.create(repository.save(application)).verifyComplete();

        when(applicationOps.getInfo(any(Application.class))).thenReturn(Mono.just(ResponseEntity.status(500).build()));

        //when/then
        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateInfo(application.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(ClientApplicationInfoChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(application.getId()))
                    .assertNext(app -> assertThat(app.getInfo()).isEqualTo(Info.empty()))
                    .verifyComplete();
    }

    @Test
    public void should_clear_info_on_exception() {
        //given
        Application application = Application.create(ApplicationId.of("onl"))
                                             .register(Registration.create("foo", "http://health").build())
                                             .withEndpoints(Endpoints.single("info", "info"))
                                             .withStatusInfo(StatusInfo.ofUp())
                                             .withInfo(Info.from(singletonMap("foo", "bar")));
        StepVerifier.create(repository.save(application)).verifyComplete();

        when(applicationOps.getInfo(any(Application.class))).thenReturn(
                Mono.error(new ResourceAccessException("error")));

        //when/then
        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateInfo(application.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(ClientApplicationInfoChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(application.getId()))
                    .assertNext(app -> assertThat(app.getInfo()).isEqualTo(Info.empty()))
                    .verifyComplete();

    }
}