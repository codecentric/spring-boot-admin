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

import de.codecentric.boot.admin.server.domain.entities.EventSourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
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
import static org.mockito.Mockito.mock;

public class StatusUpdaterTest {
    @ClassRule
    public static WireMockClassRule wireMockClassRule = new WireMockClassRule(Options.DYNAMIC_PORT);

    @Rule
    public WireMockClassRule wireMock = wireMockClassRule;

    private StatusUpdater updater;
    private ConcurrentMapEventStore eventStore;
    private EventSourcingInstanceRepository repository;
    private InstanceWebClient instanceWebClient = new InstanceWebClient(
            mock(HttpHeadersProvider.class, invocation -> HttpHeaders.EMPTY));
    private Instance instance;

    @Before
    public void setup() {
        eventStore = new InMemoryEventStore();
        repository = new EventSourcingInstanceRepository(eventStore);
        repository.start();

        instance = Instance.create(InstanceId.of("id"))
                           .register(Registration.create("foo", wireMock.url("/health")).build());
        StepVerifier.create(repository.save(instance)).expectNextCount(1).verifyComplete();

        updater = new StatusUpdater(repository, instanceWebClient);
    }

    @Test
    public void test_update_statusChanged() {
        wireMock.stubFor(get("/health").willReturn(okJson("{ \"status\" : \"UP\" } ")));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceStatusChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getStatusInfo().getStatus()).isEqualTo("UP"))
                    .verifyComplete();
    }

    @Test
    public void test_update_statusUnchanged() {
        wireMock.stubFor(get("/health").willReturn(okJson("{ \"status\" : \"UNKNOWN\" } ")));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .expectNoEvent(Duration.ofMillis(10L))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));
    }

    @Test
    public void test_update_up_noBody() {
        wireMock.stubFor(get("/health").willReturn(ok()));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceStatusChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getStatusInfo().getStatus()).isEqualTo("UP"))
                    .verifyComplete();
    }

    @Test
    public void test_update_down() {
        wireMock.stubFor(get("/health").willReturn(
                status(503).withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                           .withBody("{ \"foo\" : \"bar\" } ")));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceStatusChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

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
                    .verify(Duration.ofMillis(500L));


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
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(offlineInstance.getId())).assertNext(app -> {
            assertThat(app.getStatusInfo().getStatus()).isEqualTo("OFFLINE");
            assertThat(app.getStatusInfo().getDetails()).containsKeys("message", "exception");
        }).verifyComplete();
    }
}
