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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationControllerIntegrationTest {
    private int localPort;
    private WebTestClient client;
    private String register_as_test;
    private String register_as_required;
    private ConfigurableApplicationContext instance;
    private ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
    };

    @Before
    public void setUp() {
        instance = new SpringApplicationBuilder().sources(AdminReactiveApplicationTest.TestAdminApplication.class)
                                                 .web(WebApplicationType.REACTIVE)
                                                 .run("--server.port=0", "--eureka.client.enabled=false", "--spring.boot.admin.applicationsRequired=required");

        localPort = instance.getEnvironment().getProperty("local.server.port", Integer.class, 0);

        this.client = WebTestClient.bindToServer().baseUrl("http://localhost:" + localPort).build();
        this.register_as_test = "{ \"name\": \"test\", \"healthUrl\": \"http://localhost:" +
                                localPort +
                                "/application/health\" }";
        this.register_as_required = "{ \"name\": \"required\", \"healthUrl\": \"http://localhost:" +
                localPort +
                "/application/health\" }";
    }

    @After
    public void shutdown() {
        instance.close();
    }


    @Test
    public void should_return_only_required_app() {
        this.client.get()
                   .uri("/applications")
                   .accept(MediaType.APPLICATION_JSON)
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectBody(List.class)
                   .value(list -> assertThat(list).hasSize(1));
    }

    @Test
    public void should_return_two_applications() {
        this.register();
        this.client.get()
                .uri("/applications")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(List.class)
                .value(list -> assertThat(list).hasSize(2));
    }


    @Test
    public void should_return_not_found_when_getting_unknown_application() {
        this.client.get().uri("/applications/test").exchange().expectStatus()
                .isNotFound();
    }

    @Test
    public void should_return_one_when_getting_required_application() {
        this.client.get().uri("/applications/required").exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    public void should_return_only_one_if_required_app_register() {
        this.registerRequired();
        this.client.get().uri("/applications").exchange()
                .expectStatus()
                .isOk()
                .expectBody(List.class)
                .value(list -> assertThat(list).hasSize(1));
    }



    private String registerRequired() {
        //@formatter:off
        EntityExchangeResult<Map<String, Object>> result = client.post()
                .uri("/instances")
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                .syncBody(register_as_required)
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
}
