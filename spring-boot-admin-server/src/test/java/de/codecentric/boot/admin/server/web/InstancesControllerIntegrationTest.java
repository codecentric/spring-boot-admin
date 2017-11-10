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
package de.codecentric.boot.admin.server.web;

import de.codecentric.boot.admin.server.AdminApplicationTest;

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
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

    @Before
    public void setUp() {
        instance = new SpringApplicationBuilder().sources(AdminApplicationTest.TestAdminApplication.class)
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
    @SuppressWarnings("unchecked")
    public void should_return_registered_instances() {
        EntityExchangeResult<Map> result = client.post()
                                                 .uri("/instances")
                                                 .accept(MediaType.APPLICATION_JSON)
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .syncBody(register_as_test)
                                                 .exchange()
                                                 .expectStatus()
                                                 .isCreated()
                                                 .expectHeader()
                                                 .contentType(MediaType.APPLICATION_JSON_UTF8)
                                                 .expectHeader()
                                                 .valueMatches("location",
                                                         "http://localhost:" + localPort + "/instances/[0-9a-f]+")
                                                 .expectBody(Map.class)
                                                 .returnResult();

        assertThat(result.getResponseBody()).containsKeys("id");
        URI location = result.getResponseHeaders().getLocation();
        String id = result.getResponseBody().get("id").toString();

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
                   .valueEquals("location", location.toString())
                   .expectBody(Map.class)
                   .isEqualTo(singletonMap("id", id));

        this.client.get()
                   .uri("/instances")
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectHeader()
                   .contentType(MediaType.APPLICATION_JSON_UTF8)
                   .expectBody()
                   .jsonPath("$[0].id", id);

        this.client.get()
                   .uri("/instances?name=twice")
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectHeader()
                   .contentType(MediaType.APPLICATION_JSON_UTF8)
                   .expectBody()
                   .jsonPath("$[0].id", id);

        this.client.get()
                   .uri(location)
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectHeader()
                   .contentType(MediaType.APPLICATION_JSON_UTF8)
                   .expectBody()
                   .jsonPath("$[0].id", id);

        this.client.delete().uri(location).exchange().expectStatus().isNoContent();

        this.client.get().uri(location).exchange().expectStatus().isNotFound();

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
                       System.out.println(response.getResponseBody());
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

}
