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
package de.codecentric.boot.admin.server;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.model.Registration;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;

import static org.assertj.core.api.Assertions.assertThat;


public class AdminApplicationTest {
    private WebTestClient webClient;
    private int port;
    private ServletWebServerApplicationContext instance;

    @Before
    public void setUp() {
        instance = (ServletWebServerApplicationContext) SpringApplication.run(TestAdminApplication.class,
                "--server.port=0", "--management.context-path=/mgmt", "--info.test=foobar");
        port = instance.getWebServer().getPort();

        ObjectMapper mapper = new ObjectMapper().registerModule(new JsonOrgModule());
        webClient = WebTestClient.bindToServer()
                                 .baseUrl("http://localhost:" + port)
                                 .exchangeStrategies(ExchangeStrategies.builder().codecs((configurer) -> {
                                     configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
                                     configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
                                 }).build())
                                 .build();
    }

    @After
    public void shutdown() {
        instance.close();
    }

    @Test
    public void lifecycle() throws InterruptedException {
        Flux<JSONObject> events = getEventStream();
        AtomicReference<URI> location = new AtomicReference<>();

        StepVerifier.create(events.log())
                    .expectSubscription()
                    .then(() -> {
                        listEmptyApplications();
                        location.set(registerApplication());
                    })
                    .assertNext((event) -> assertThat(event.opt("type")).isEqualTo("REGISTERED"))
                    .assertNext((event) -> assertThat(event.opt("type")).isEqualTo("STATUS_CHANGED"))
                    .assertNext((event) -> assertThat(event.opt("type")).isEqualTo("INFO_CHANGED"))
                    .then(() -> {
                        getApplication(location.get());
                        listApplications();
                        deregisterApplication(location.get());
                    })
                    .assertNext((event) -> assertThat(event.opt("type")).isEqualTo("DEREGISTERED"))
                    .then(this::listEmptyApplications)
                    .thenCancel()
                    .verify();
    }

    private Flux<JSONObject> getEventStream() {
        //@formatter:off
        return webClient.get().uri("/api/applications/events").accept(MediaType.APPLICATION_STREAM_JSON)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentType(MediaType.APPLICATION_STREAM_JSON)
                        .returnResult(JSONObject.class).getResponseBody();
        //@formatter:on
    }

    private URI registerApplication() {
        //@formatter:off
       return webClient.post().uri("/api/applications").contentType(MediaType.APPLICATION_JSON).syncBody(createRegistration())
                       .exchange()
                       .expectStatus().isCreated()
                       .expectHeader().valueMatches("location", "^http://localhost:" + port + "/api/applications/[a-f0-9]+$")
                       .returnResult(Void.class).getResponseHeaders().getLocation();
        //@formatter:on
    }

    private void getApplication(URI uri) {
        //@formatter:off
        webClient.get().uri(uri).accept(MediaType.APPLICATION_JSON_UTF8)
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody()
                     .jsonPath("$.registration.name").isEqualTo("Test-Application")
                     .jsonPath("$.statusInfo.status").isEqualTo("UP")
                     .jsonPath("$.info.test").isEqualTo("foobar");
       //@formatter:on
    }

    private void listApplications() {
        //@formatter:off
        webClient.get().uri("/api/applications").accept(MediaType.APPLICATION_JSON_UTF8)
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody()
                     .jsonPath("$[0].registration.name").isEqualTo("Test-Application")
                     .jsonPath("$[0].statusInfo.status").isEqualTo("UP")
                     .jsonPath("$[0].info.test").isEqualTo("foobar");
       //@formatter:on
    }

    private void listEmptyApplications() {
        //@formatter:off
        webClient.get().uri("/api/applications").accept(MediaType.APPLICATION_JSON_UTF8)
                 .exchange()
                 .expectStatus().isOk()
                 .expectBody().json("[]");
       //@formatter:on
    }

    private void deregisterApplication(URI uri) {
        webClient.delete().uri(uri).exchange().expectStatus().isNoContent();
    }


    private Registration createRegistration() {
        return Registration.builder()
                           .name("Test-Application")
                           .healthUrl("http://localhost:" + port + "/mgmt/health")
                           .managementUrl("http://localhost:" + port + "/mgmt")
                           .serviceUrl("http://localhost:" + port)
                           .build();
    }

    @EnableAdminServer
    @EnableAutoConfiguration
    @SpringBootConfiguration
    public static class TestAdminApplication {
    }
}
