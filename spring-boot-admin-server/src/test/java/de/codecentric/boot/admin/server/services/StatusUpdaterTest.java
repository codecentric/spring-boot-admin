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
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.ConcurrentMapEventStore;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.MediaType;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okForContentType;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class StatusUpdaterTest {
    @Rule
    public WireMockRule wireMock = new WireMockRule(Options.DYNAMIC_PORT);

    private StatusUpdater updater;
    private ConcurrentMapEventStore eventStore;
    private InstanceRepository repository;
    private Instance instance;

    @BeforeClass
    public static void setUp() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(5));
    }

    @AfterClass
    public static void tearDown() {
        StepVerifier.resetDefaultTimeout();
    }

    @Before
    public void setup() {
        eventStore = new InMemoryEventStore();
        repository = new EventsourcingInstanceRepository(eventStore);
        instance = Instance.create(InstanceId.of("id"))
                           .register(Registration.create("foo", wireMock.url("/health")).build());
        StepVerifier.create(repository.save(instance)).expectNextCount(1).verifyComplete();

        updater = new StatusUpdater(repository,
            InstanceWebClient.builder()
                             .connectTimeout(Duration.ofSeconds(2))
                             .readTimeout(Duration.ofSeconds(2))
                             .retries(singletonMap(Endpoint.HEALTH, 1))
                             .build()
        );
    }

    @Test
    public void should_change_status_to_down() {
        String body = "{ \"status\" : \"UP\", \"details\" : { \"foo\" : \"bar\" } }";
        wireMock.stubFor(get("/health").willReturn(okForContentType(ActuatorMediaType.V2_JSON, body).withHeader("Content-Length",
            Integer.toString(body.length())
        )));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .assertNext(event -> {
                        assertThat(event).isInstanceOf(InstanceStatusChangedEvent.class);
                        assertThat(event.getInstance()).isEqualTo(instance.getId());
                        InstanceStatusChangedEvent statusChangedEvent = (InstanceStatusChangedEvent) event;
                        assertThat(statusChangedEvent.getStatusInfo().getStatus()).isEqualTo("UP");
                        assertThat(statusChangedEvent.getStatusInfo().getDetails()).isEqualTo(singletonMap("foo",
                            "bar"
                        ));
                    })
                    .thenCancel()
                    .verify();

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getStatusInfo().getStatus()).isEqualTo("UP"))
                    .verifyComplete();

        StepVerifier.create(repository.computeIfPresent(instance.getId(),
            (key, instance) -> Mono.just(instance.deregister())
        ))
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .thenCancel()
                    .verify();

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getStatusInfo().getStatus()).isEqualTo("UNKNOWN"))
                    .verifyComplete();
    }

    @Test
    public void should_not_change_status() {
        String body = "{ \"status\" : \"UNKNOWN\" }";
        wireMock.stubFor(get("/health").willReturn(okJson(body).withHeader("Content-Type",
            Integer.toString(body.length())
        )));


        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .expectNoEvent(Duration.ofMillis(100L))
                    .thenCancel()
                    .verify();
    }

    @Test
    public void should_change_status_to_up() {
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
    public void should_change_status_to_down_with_details() {
        String body = "{ \"foo\" : \"bar\" }";
        wireMock.stubFor(get("/health").willReturn(status(503).withHeader("Content-Type",
            MediaType.APPLICATION_JSON_VALUE
        )
                                                              .withHeader(
                                                                  "Content-Length",
                                                                  Integer.toString(body.length())
                                                              )
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
    public void should_change_status_to_down_without_details_incompatible_content_type() {
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
    public void should_change_status_to_down_without_details_no_body() {
        wireMock.stubFor(get("/health").willReturn(status(503).withHeader("Content-Type",
            MediaType.APPLICATION_JSON_VALUE
        )));

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
    public void should_change_status_to_offline() {
        wireMock.stubFor(get("/health").willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete())
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceStatusChangedEvent.class))
                    .thenCancel()
                    .verify();

        StepVerifier.create(repository.find(instance.getId())).assertNext(app -> {
            assertThat(app.getStatusInfo().getStatus()).isEqualTo("OFFLINE");
            assertThat(app.getStatusInfo().getDetails()).containsKeys("message", "exception");
        }).verifyComplete();

        StepVerifier.create(updater.updateStatus(instance.getId())).verifyComplete();
    }

    @Test
    public void should_retry() {
        wireMock.stubFor(get("/health").inScenario("retry")
                                       .whenScenarioStateIs(STARTED)
                                       .willReturn(aResponse().withFixedDelay(5000))
                                       .willSetStateTo("recovered"));
        wireMock.stubFor(get("/health").inScenario("retry").whenScenarioStateIs("recovered").willReturn(ok()));


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
}
