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

import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import reactor.test.StepVerifier;

import java.time.Duration;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static org.assertj.core.api.Assertions.assertThat;

public class StatusUpdaterTest {
    @ClassRule
    public static WireMockClassRule wireMockClass = new WireMockClassRule(Options.DYNAMIC_PORT);
    @Rule
    public WireMockClassRule wireMock = wireMockClass;

    private StatusUpdater updater;
    private ConcurrentMapEventStore eventStore;
    private InstanceRepository repository;
    private Instance instance;

    @Before
    public void setup() {
        eventStore = new InMemoryEventStore();
        repository = new EventsourcingInstanceRepository(eventStore);
        instance = Instance.create(InstanceId.of("id"))
                           .register(Registration.create("foo", wireMock.url("/health")).build());
        StepVerifier.create(repository.save(instance)).expectNextCount(1).verifyComplete();

        updater = new StatusUpdater(repository,
            new InstanceWebClient(instance -> HttpHeaders.EMPTY, Duration.ofSeconds(5), Duration.ofSeconds(20)));
    }

    @Test
    public void test_update_statusChanged() {
        String body = "{ \"status\" : \"UP\" }";
        wireMock.stubFor(
            get("/health").willReturn(okJson(body).withHeader("Content-Length", Integer.toString(body.length()))));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .assertNext(event -> {
                        assertThat(event).isInstanceOf(InstanceStatusChangedEvent.class);
                        assertThat(event.getInstance()).isEqualTo(instance.getId());
                        InstanceStatusChangedEvent statusChangedEvent = (InstanceStatusChangedEvent) event;
                        assertThat(statusChangedEvent.getStatusInfo().getStatus()).isEqualTo("UP");
                    })
                    .thenCancel()
                    .verify();

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getStatusInfo().getStatus()).isEqualTo("UP"))
                    .verifyComplete();
    }

    @Test
    public void test_update_statusUnchanged() {
        String body = "{ \"status\" : \"UNKNOWN\" }";
        wireMock.stubFor(
            get("/health").willReturn(okJson(body).withHeader("Content-Type", Integer.toString(body.length()))));


        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .expectNoEvent(Duration.ofMillis(100L))
                    .thenCancel()
                    .verify();
    }

    @Test
    public void test_update_up_noBody() {
        wireMock.stubFor(get("/health").willReturn(ok()));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceStatusChangedEvent.class))
                    .thenCancel()
                    .verify();

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getStatusInfo().getStatus()).isEqualTo("UP"))
                    .verifyComplete();
    }

    @Test
    public void test_update_down() {
        String body = "{ \"foo\" : \"bar\" }";
        wireMock.stubFor(get("/health").willReturn(
            status(503).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                       .withHeader("Content-Length", Integer.toString(body.length()))
                       .withBody(body)));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceStatusChangedEvent.class))
                    .thenCancel()
                    .verify();

        StepVerifier.create(repository.find(instance.getId())).assertNext(app -> {
            assertThat(app.getStatusInfo().getStatus()).isEqualTo("DOWN");
            assertThat(app.getStatusInfo().getDetails()).containsEntry("foo", "bar");
        }).verifyComplete();
    }

    @Test
    public void test_update_down_noBody() {
        wireMock.stubFor(get("/health").willReturn(status(503)));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceStatusChangedEvent.class))
                    .thenCancel()
                    .verify();


        StepVerifier.create(repository.find(instance.getId())).assertNext(app -> {
            assertThat(app.getStatusInfo().getStatus()).isEqualTo("DOWN");
            assertThat(app.getStatusInfo().getDetails()).containsEntry("status", 503)
                                                        .containsEntry("error", "Service Unavailable");
        }).verifyComplete();
    }

    @Test
    public void test_update_offline() {
        Instance offlineInstance = Instance.create(InstanceId.of("offline"))
                                           .register(Registration.create("foo", "http://0.0.0.0/health").build());

        StepVerifier.create(repository.save(offlineInstance)).expectNextCount(1).verifyComplete();

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(offlineInstance.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceStatusChangedEvent.class))
                    .thenCancel()
                    .verify();

        StepVerifier.create(repository.find(offlineInstance.getId())).assertNext(app -> {
            assertThat(app.getStatusInfo().getStatus()).isEqualTo("OFFLINE");
            assertThat(app.getStatusInfo().getDetails()).containsKeys("message", "exception");
        }).verifyComplete();
    }
}
