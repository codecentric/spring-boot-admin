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
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import reactor.test.StepVerifier;

import java.time.Duration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class InfoUpdaterTest {
    @Rule
    public WireMockRule wireMock = new WireMockRule(Options.DYNAMIC_PORT);

    private InfoUpdater updater;
    private InMemoryEventStore eventStore;
    private InstanceRepository repository;

    @Before
    public void setup() {
        eventStore = new InMemoryEventStore();
        repository = new EventsourcingInstanceRepository(eventStore);
        updater = new InfoUpdater(repository,
            InstanceWebClient.builder()
                             .retries(singletonMap(Endpoint.INFO, 1))
                             .connectTimeout(Duration.ofSeconds(2))
                             .readTimeout(Duration.ofSeconds(2))
                             .build()
        );
    }

    @BeforeClass
    public static void setUp() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(5));
    }

    @AfterClass
    public static void tearDown() {
        StepVerifier.resetDefaultTimeout();
    }

    @Test
    public void should_update_info_for_online_with_info_endpoint_only() {
        //given
        Registration registration = Registration.create("foo", wireMock.url("/health")).build();
        Instance instance = Instance.create(InstanceId.of("onl"))
                                    .register(registration)
                                    .withEndpoints(Endpoints.single("info", wireMock.url("/info")))
                                    .withStatusInfo(StatusInfo.ofUp());
        StepVerifier.create(repository.save(instance)).expectNextCount(1).verifyComplete();
        String body = "{ \"foo\": \"bar\" }";
        wireMock.stubFor(get("/info").willReturn(okJson(body).withHeader("Content-Length",
            Integer.toString(body.length())
        )));

        Instance noInfo = Instance.create(InstanceId.of("noinfo"))
                                  .register(registration)
                                  .withEndpoints(Endpoints.single("beans", wireMock.url("/beans")))
                                  .withStatusInfo(StatusInfo.ofUp());
        StepVerifier.create(repository.save(noInfo)).expectNextCount(1).verifyComplete();

        Instance offline = Instance.create(InstanceId.of("off"))
                                   .register(registration)
                                   .withStatusInfo(StatusInfo.ofOffline());
        StepVerifier.create(repository.save(offline)).expectNextCount(1).verifyComplete();

        Instance unknown = Instance.create(InstanceId.of("unk"))
                                   .register(registration)
                                   .withStatusInfo(StatusInfo.ofUnknown());
        StepVerifier.create(repository.save(unknown)).expectNextCount(1).verifyComplete();


        //when
        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateInfo(offline.getId())).verifyComplete())
                    .then(() -> StepVerifier.create(updater.updateInfo(unknown.getId())).verifyComplete())
                    .then(() -> StepVerifier.create(updater.updateInfo(noInfo.getId())).verifyComplete())
                    .expectNoEvent(Duration.ofMillis(100L))
                    .then(() -> StepVerifier.create(updater.updateInfo(instance.getId())).verifyComplete())
                    //then
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class))
                    .thenCancel()
                    .verify();

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getInfo()).isEqualTo(Info.from(singletonMap("foo", "bar"))))
                    .verifyComplete();
    }

    @Test
    public void should_clear_info_on_http_error() {
        //given
        Instance instance = Instance.create(InstanceId.of("onl"))
                                    .register(Registration.create("foo", wireMock.url("/health")).build())
                                    .withEndpoints(Endpoints.single("info", wireMock.url("/info")))
                                    .withStatusInfo(StatusInfo.ofUp())
                                    .withInfo(Info.from(singletonMap("foo", "bar")));
        StepVerifier.create(repository.save(instance)).expectNextCount(1).verifyComplete();

        wireMock.stubFor(get("/info").willReturn(serverError()));

        //when
        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateInfo(instance.getId())).verifyComplete())
                    //then
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class))
                    .thenCancel()
                    .verify();

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getInfo()).isEqualTo(Info.empty()))
                    .verifyComplete();
    }


    @Test
    public void should_clear_info_on_exception() {
        updater = new InfoUpdater(repository,
            InstanceWebClient.builder()
                             .connectTimeout(Duration.ofMillis(250L))
                             .readTimeout(Duration.ofMillis(250L))
                             .build()
        );

        //given
        Instance instance = Instance.create(InstanceId.of("onl"))
                                    .register(Registration.create("foo", wireMock.url("/health")).build())
                                    .withEndpoints(Endpoints.single("info", wireMock.url("/info")))
                                    .withStatusInfo(StatusInfo.ofUp())
                                    .withInfo(Info.from(singletonMap("foo", "bar")));
        StepVerifier.create(repository.save(instance)).expectNextCount(1).verifyComplete();

        wireMock.stubFor(get("/info").willReturn(okJson("{ \"foo\": \"bar\" }").withFixedDelay(1500)));

        //when
        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateInfo(instance.getId())).verifyComplete())
                    //then
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class))
                    .thenCancel()
                    .verify();

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getInfo()).isEqualTo(Info.empty()))
                    .verifyComplete();
    }

    @Test
    public void should_retry() {
        //given
        Registration registration = Registration.create("foo", wireMock.url("/health")).build();
        Instance instance = Instance.create(InstanceId.of("onl"))
                                    .register(registration)
                                    .withEndpoints(Endpoints.single("info", wireMock.url("/info")))
                                    .withStatusInfo(StatusInfo.ofUp());
        StepVerifier.create(repository.save(instance)).expectNextCount(1).verifyComplete();

        wireMock.stubFor(get("/info").inScenario("retry")
                                     .whenScenarioStateIs(STARTED)
                                     .willReturn(aResponse().withFixedDelay(5000))
                                     .willSetStateTo("recovered"));

        String body = "{ \"foo\": \"bar\" }";
        wireMock.stubFor(get("/info").inScenario("retry")
                                     .whenScenarioStateIs("recovered")
                                     .willReturn(okJson(body).withHeader("Content-Length",
                                         Integer.toString(body.length())
                                     )));

        //when
        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateInfo(instance.getId())).verifyComplete())
                    //then
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class))
                    .thenCancel()
                    .verify();

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getInfo()).isEqualTo(Info.from(singletonMap("foo", "bar"))))
                    .verifyComplete();
    }
}
