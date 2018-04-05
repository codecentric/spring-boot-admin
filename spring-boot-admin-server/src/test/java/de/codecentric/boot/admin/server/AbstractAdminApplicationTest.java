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

package de.codecentric.boot.admin.server;

import de.codecentric.boot.admin.server.domain.values.Registration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;

import static org.assertj.core.api.Assertions.assertThat;


public abstract class AbstractAdminApplicationTest {
    private WebTestClient webClient;
    private int port;


    public void setUp(int port) {
        this.port = port;
        this.webClient = createWebClient(port);
    }

    @Test
    public void lifecycle() {
        AtomicReference<URI> location = new AtomicReference<>();

        StepVerifier.create(getEventStream().log())
                    .expectSubscription()
                    .then(() -> {
                        listEmptyInstances();
                        location.set(registerInstance());
                    })
                    .assertNext((event) -> assertThat(event.opt("type")).isEqualTo("REGISTERED"))
                    .assertNext((event) -> assertThat(event.opt("type")).isEqualTo("STATUS_CHANGED"))
                    .assertNext((event) -> assertThat(event.opt("type")).isEqualTo("ENDPOINTS_DETECTED"))
                    .assertNext((event) -> assertThat(event.opt("type")).isEqualTo("INFO_CHANGED"))
                    .then(() -> {
                        getInstance(location.get());
                        listInstances();
                        deregisterInstance(location.get());
                    })
                    .assertNext((event) -> assertThat(event.opt("type")).isEqualTo("DEREGISTERED"))
                    .then(this::listEmptyInstances)
                    .thenCancel()
                    .verify(Duration.ofSeconds(60));
    }

    protected Flux<JSONObject> getEventStream() {
        //@formatter:off
        return webClient.get().uri("/instances/events").accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                        .returnResult(JSONObject.class).getResponseBody();
        //@formatter:on
    }

    protected URI registerInstance() {
        //@formatter:off
        return webClient.post().uri("/instances").contentType(MediaType.APPLICATION_JSON).syncBody(createRegistration())
                        .exchange()
                        .expectStatus().isCreated()
                        .expectHeader().valueMatches("location", "^http://localhost:" + port + "/instances/[a-f0-9]+$")
                        .returnResult(Void.class).getResponseHeaders().getLocation();
        //@formatter:on
    }

    protected void getInstance(URI uri) {
        //@formatter:off
        webClient.get().uri(uri).accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.registration.name").isEqualTo("Test-Instance")
                .jsonPath("$.statusInfo.status").isEqualTo("UP")
                .jsonPath("$.info.test").isEqualTo("foobar");
        //@formatter:on
    }

    protected void listInstances() {
        //@formatter:off
        webClient.get().uri("/instances").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                    .jsonPath("$[0].registration.name").isEqualTo("Test-Instance")
                    .jsonPath("$[0].statusInfo.status").isEqualTo("UP")
                    .jsonPath("$[0].info.test").isEqualTo("foobar");
        //@formatter:on
    }

    protected void listEmptyInstances() {
        //@formatter:off
        webClient.get().uri("/instances").accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");
        //@formatter:on
    }

    protected void deregisterInstance(URI uri) {
        webClient.delete().uri(uri).exchange().expectStatus().isNoContent();
    }


    private Registration createRegistration() {
        return Registration.builder()
                           .name("Test-Instance")
                           .healthUrl("http://localhost:" + port + "/mgmt/health")
                           .managementUrl("http://localhost:" + port + "/mgmt")
                           .serviceUrl("http://localhost:" + port)
                           .build();
    }

    protected WebTestClient createWebClient(int port) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JsonOrgModule());
        return WebTestClient.bindToServer()
                            .baseUrl("http://localhost:" + port)
                            .exchangeStrategies(ExchangeStrategies.builder().codecs((configurer) -> {
                                configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                                configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
                            }).build())
                            .build();
    }

    public int getPort() {
        return port;
    }

    public WebTestClient getWebClient() {
        return webClient;
    }
}
