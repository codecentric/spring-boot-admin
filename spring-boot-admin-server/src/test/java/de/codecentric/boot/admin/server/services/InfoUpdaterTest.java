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
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
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
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class InfoUpdaterTest {
    @ClassRule
    public static WireMockClassRule wireMockClassRule = new WireMockClassRule(Options.DYNAMIC_PORT);

    @Rule
    public WireMockClassRule wireMock = wireMockClassRule;

    private InfoUpdater updater;
    private InMemoryEventStore eventStore;
    private EventSourcingInstanceRepository repository;

    @Before
    public void setup() {
        eventStore = new InMemoryEventStore();
        repository = new EventSourcingInstanceRepository(eventStore);
        repository.start();
        InstanceWebClient instanceWebClient = new InstanceWebClient(
                mock(HttpHeadersProvider.class, invocation -> HttpHeaders.EMPTY), 1000, 1000);
        updater = new InfoUpdater(repository, instanceWebClient);
    }


    @Test
    public void should_update_info_for_online_with_info_endpoint_only() {
        //given
        Registration registration = Registration.create("foo", wireMock.url("/health")).build();
        Instance instance = Instance.create(InstanceId.of("onl"))
                                    .register(registration)
                                    .withEndpoints(Endpoints.single("info", wireMock.url("/info")))
                                    .withStatusInfo(StatusInfo.ofUp());
        StepVerifier.create(repository.save(instance)).verifyComplete();

        Instance noInfo = Instance.create(InstanceId.of("noinfo"))
                                  .register(registration)
                                  .withEndpoints(Endpoints.single("beans", wireMock.url("/info")))
                                  .withStatusInfo(StatusInfo.ofUp());
        StepVerifier.create(repository.save(noInfo)).verifyComplete();

        Instance offline = Instance.create(InstanceId.of("off"))
                                   .register(registration)
                                   .withStatusInfo(StatusInfo.ofOffline());
        StepVerifier.create(repository.save(offline)).verifyComplete();

        Instance unknown = Instance.create(InstanceId.of("unk"))
                                   .register(registration)
                                   .withStatusInfo(StatusInfo.ofUnknown());
        StepVerifier.create(repository.save(unknown)).verifyComplete();

        wireMock.stubFor(get("/info").willReturn(okJson("{ \"foo\": \"bar\" }")));

        //when
        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateInfo(offline.getId())).verifyComplete())
                    .then(() -> StepVerifier.create(updater.updateInfo(unknown.getId())).verifyComplete())
                    .then(() -> StepVerifier.create(updater.updateInfo(noInfo.getId())).verifyComplete())
                    .expectNoEvent(Duration.ofMillis(10L))
                    .then(() -> StepVerifier.create(updater.updateInfo(instance.getId())).verifyComplete())
                    //then
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

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
        StepVerifier.create(repository.save(instance)).verifyComplete();

        wireMock.stubFor(get("/info").willReturn(serverError()));

        //when
        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateInfo(instance.getId())).verifyComplete())
                    //then
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getInfo()).isEqualTo(Info.empty()))
                    .verifyComplete();
    }


    @Test
    public void should_clear_info_on_exception() {
        //given
        Instance instance = Instance.create(InstanceId.of("onl"))
                                    .register(Registration.create("foo", wireMock.url("/health")).build())
                                    .withEndpoints(Endpoints.single("info", wireMock.url("/info")))
                                    .withStatusInfo(StatusInfo.ofUp())
                                    .withInfo(Info.from(singletonMap("foo", "bar")));
        StepVerifier.create(repository.save(instance)).verifyComplete();

        wireMock.stubFor(get("/info").willReturn(okJson("{ \"foo\": \"bar\" }").withFixedDelay(1500)));

        //when
        StepVerifier.create(eventStore)
                    .expectSubscription()
                    .then(() -> StepVerifier.create(updater.updateInfo(instance.getId())).verifyComplete())
                    //then
                    .assertNext(event -> assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class))
                    .thenCancel()
                    .verify(Duration.ofMillis(500L));

        StepVerifier.create(repository.find(instance.getId()))
                    .assertNext(app -> assertThat(app.getInfo()).isEqualTo(Info.empty()))
                    .verifyComplete();
    }

}