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

package de.codecentric.boot.admin.server.web;

import de.codecentric.boot.admin.server.AdminReactiveApplicationTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class InstancesControllerIntegrationTest {
    private int localPort;
    private WebTestClient client;
    private String register_as_test;
    private String register_as_twice;
    private ConfigurableApplicationContext instance;
    private ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
    };

    @Before
    public void setUp() {
        instance = new SpringApplicationBuilder().sources(AdminReactiveApplicationTest.TestAdminApplication.class)
                                                 .web(WebApplicationType.REACTIVE)
                                                 .run("--server.port=0", "--eureka.client.enabled=false");

        localPort = instance.getEnvironment().getProperty("local.server.port", Integer.class, 0);

        this.client = WebTestClient.bindToServer().baseUrl("http://localhost:" + localPort).build();
        this.register_as_test = "{ \"name\": \"test\", \"healthUrl\": \"http://localhost:" +
                                localPort +
                                "/application/health\" }";
        this.register_as_twice = "{ \"name\": \"twice\", \"healthUrl\": \"http://localhost:" +
                                 localPort +
                                 "/application/health\" }";
    }

    @After
    public void shutdown() {
        instance.close();
    }

    @Test
    public void should_return_not_found_when_get_unknown_instance() {
        this.client.get().uri("/instances/unknown").exchange().expectStatus().isNotFound();
    }

    @Test
    public void should_return_empty_list() {
        this.client.get()
                   .uri("/instances?name=unknown")
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectBody(List.class)
                   .isEqualTo(emptyList());
    }

    @Test
    public void should_return_not_found_when_deleting_unknown_instance() {
        this.client.delete().uri("/instances/unknown").exchange().expectStatus().isNotFound();
    }

    @Test
    public void should_return_registered_instances() {
        AtomicReference<String> id = new AtomicReference<>();
        CountDownLatch cdl = new CountDownLatch(1);

        StepVerifier.create(this.getEventStream().log())
                    .expectSubscription()
                    .then(() -> {
                        id.set(register());
                        cdl.countDown();
                    })
                    .assertNext(body -> {
                        try {
                            cdl.await();
                        } catch (InterruptedException e) {
                            Thread.interrupted();
                        }
                        assertThat(body).containsEntry("instance", id.get()).containsEntry("version", 0)
                                        .containsEntry("type", "REGISTERED");
                    })
                    .then(() -> {
                        assertInstances(id.get());
                        assertInstancesByName(id.get());
                        assertInstanceById(id.get());
                    })
                    .assertNext(body -> assertThat(body).containsEntry("instance", id.get())
                                                        .containsEntry("version", 1)
                                                        .containsEntry("type", "STATUS_CHANGED"))
                    .then(() -> registerSecondTime(id.get()))
                    .assertNext(body -> assertThat(body).containsEntry("instance", id.get())
                                                        .containsEntry("version", 2)
                                                        .containsEntry("type", "REGISTRATION_UPDATED"))
                    .then(() -> deregister(id.get()))

                    .assertNext(body -> assertThat(body).containsEntry("instance", id.get())
                                                        .containsEntry("version", 3)
                                                        .containsEntry("type", "DEREGISTERED"))
                    .then(() -> {
                        assertInstanceNotFound(id.get());
                        assertEvents(id.get());
                    })
                    .thenCancel()
                    .verify(Duration.ofSeconds(60));
    }

    private void assertEvents(String id) {
        this.client.get()
                   .uri("/instances/events")
                   .accept(MediaType.APPLICATION_JSON)
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectHeader()
                   .contentType(MediaType.APPLICATION_JSON_UTF8)
                   .expectBody(String.class)
                   .consumeWith(response -> {
                       DocumentContext json = JsonPath.parse(response.getResponseBody());
                       assertThat(json.read("$[0].instance", String.class)).isEqualTo(id);
                       assertThat(json.read("$[0].version", Long.class)).isEqualTo(0L);
                       assertThat(json.read("$[0].type", String.class)).isEqualTo("REGISTERED");
                       assertThat(json.read("$[1].instance", String.class)).isEqualTo(id);
                       assertThat(json.read("$[1].version", Long.class)).isEqualTo(1L);
                       assertThat(json.read("$[1].type", String.class)).isEqualTo("STATUS_CHANGED");
                       assertThat(json.read("$[2].instance", String.class)).isEqualTo(id);
                       assertThat(json.read("$[2].version", Long.class)).isEqualTo(2L);
                       assertThat(json.read("$[2].type", String.class)).isEqualTo("REGISTRATION_UPDATED");
                       assertThat(json.read("$[3].instance", String.class)).isEqualTo(id);
                       assertThat(json.read("$[3].version", Long.class)).isEqualTo(3L);
                       assertThat(json.read("$[3].type", String.class)).isEqualTo("DEREGISTERED");
                   });
    }

    private void assertInstanceNotFound(String id) {
        this.client.get().uri(getLocation(id)).exchange().expectStatus().isNotFound();
    }

    private void deregister(String id) {
        this.client.delete().uri(getLocation(id)).exchange().expectStatus().isNoContent();
    }

    private void assertInstanceById(String id) {
        this.client.get()
                   .uri(getLocation(id))
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectHeader()
                   .contentType(MediaType.APPLICATION_JSON_UTF8)
                   .expectBody()
                   .jsonPath("$[0].id", id);
    }

    private void assertInstancesByName(String id) {
        this.client.get()
                   .uri("/instances?name=twice")
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectHeader()
                   .contentType(MediaType.APPLICATION_JSON_UTF8)
                   .expectBody()
                   .jsonPath("$[0].id", id);
    }

    private void assertInstances(String id) {
        this.client.get()
                   .uri("/instances")
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectHeader()
                   .contentType(MediaType.APPLICATION_JSON_UTF8)
                   .expectBody()
                   .jsonPath("$[0].id", id);
    }

    private void registerSecondTime(String id) {
        this.client.post()
                   .uri("/instances")
                   .accept(MediaType.APPLICATION_JSON)
                   .contentType(MediaType.APPLICATION_JSON)
                   .syncBody(register_as_twice)
                   .exchange()
                   .expectStatus()
                   .isCreated()
                   .expectHeader()
                   .contentType(MediaType.APPLICATION_JSON_UTF8)
                   .expectHeader()
                   .valueEquals("location", getLocation(id))
                   .expectBody(Map.class)
                   .isEqualTo(singletonMap("id", id));
    }

    private String register() {
        //@formatter:off
        EntityExchangeResult<Map<String, Object>> result = client.post()
                                                                .uri("/instances")
                                                                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                                                                .syncBody(register_as_test)
                                                                .exchange()
                                                                .expectStatus().isCreated()
                                                                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                                                                .expectHeader().valueMatches("location", "http://localhost:" + localPort + "/instances/[0-9a-f]+")
                                                                .expectBody(RESPONSE_TYPE)
                                                                .returnResult();
        //@formatter:on
        assertThat(result.getResponseBody()).containsKeys("id");
        return result.getResponseBody().get("id").toString();
    }

    private String getLocation(String id) {
        return "http://localhost:" + localPort + "/instances/" + id;

    }

    private Flux<Map<String, Object>> getEventStream() {
        //@formatter:off
        return this.client.get().uri("/instances/events").accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
                        .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                        .returnResult(RESPONSE_TYPE).getResponseBody();
        //@formatter:on
    }


}
