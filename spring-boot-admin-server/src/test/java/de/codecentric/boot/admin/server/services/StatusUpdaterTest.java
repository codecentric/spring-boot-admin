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
import de.codecentric.boot.admin.server.domain.events.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Registration;
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
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StatusUpdaterTest {
    private ApplicationOperations applicationOps;
    private StatusUpdater updater;
    private EventSourcingApplicationRepository repository;
    private final Application application = Application.create(ApplicationId.of("id"))
                                                       .register(Registration.create("foo", "http://health").build());
    private ConcurrentMapEventStore eventStore;

    @Before
    public void setup() {
        eventStore = new InMemoryEventStore();
        repository = new EventSourcingApplicationRepository(eventStore);
        repository.start();
        StepVerifier.create(repository.save(application)).verifyComplete();

        applicationOps = mock(ApplicationOperations.class);
        updater = new StatusUpdater(repository, applicationOps);
    }

    @Test
    public void test_update_statusChanged() {
        when(applicationOps.getHealth(isA(Application.class))).thenReturn(
                Mono.just(ResponseEntity.ok().body(singletonMap("status", "UP"))));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(application.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(ClientApplicationStatusChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(application.getId()))
                    .assertNext(app -> assertThat(app.getStatusInfo().getStatus()).isEqualTo("UP"))
                    .verifyComplete();
    }

    @Test
    public void test_update_statusUnchanged() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(
                Mono.just(ResponseEntity.ok(singletonMap("status", "UNKNOWN"))));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(application.getId())).verifyComplete())
                    .expectNoEvent(Duration.ofMillis(10L))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));
    }

    @Test
    public void test_update_up_noBody() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(Mono.just(ResponseEntity.ok().build()));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(application.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(ClientApplicationStatusChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(application.getId()))
                    .assertNext(app -> assertThat(app.getStatusInfo().getStatus()).isEqualTo("UP"))
                    .verifyComplete();
    }

    @Test
    public void test_update_down() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(
                Mono.just(ResponseEntity.status(503).body(singletonMap("foo", "bar"))));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(application.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(ClientApplicationStatusChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(application.getId())).assertNext(app -> {
            assertThat(app.getStatusInfo().getStatus()).isEqualTo("DOWN");
            assertThat(app.getStatusInfo().getDetails()).containsEntry("foo", "bar");
        }).verifyComplete();
    }

    @Test
    public void test_update_down_noBody() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(
                Mono.just(ResponseEntity.status(503).body(null)));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(application.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(ClientApplicationStatusChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));


        StepVerifier.create(repository.find(application.getId())).assertNext(app -> {
            assertThat(app.getStatusInfo().getStatus()).isEqualTo("DOWN");
            assertThat(app.getStatusInfo().getDetails()).containsEntry("status", 503)
                                                        .containsEntry("error", "Service Unavailable");
        }).verifyComplete();
    }

    @Test
    public void test_update_offline() {
        when(applicationOps.getHealth(any(Application.class))).thenReturn(
                Mono.error(new ResourceAccessException("error")));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(application.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(ClientApplicationStatusChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(application.getId())).assertNext(app -> {
            assertThat(app.getStatusInfo().getStatus()).isEqualTo("OFFLINE");
            assertThat(app.getStatusInfo().getDetails()).containsEntry("message", "error")
                                                        .containsEntry("exception",
                                                                "org.springframework.web.client.ResourceAccessException");
        }).verifyComplete();
    }

}
