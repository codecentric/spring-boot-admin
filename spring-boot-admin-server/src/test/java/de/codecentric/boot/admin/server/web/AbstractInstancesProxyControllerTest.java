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

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static de.codecentric.boot.admin.server.utils.MediaType.ACTUATOR_V2_MEDIATYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

public abstract class AbstractInstancesProxyControllerTest {
    private WebTestClient client;
    private ConfigurableApplicationContext context;
    private ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    @Rule
    public WireMockRule wireMock = new WireMockRule(Options.DYNAMIC_PORT);


    @Before
    public void setUp() {
        context = setupContext();
        int localPort = context.getEnvironment().getProperty("local.server.port", Integer.class, 0);
        this.client = WebTestClient.bindToServer().baseUrl("http://localhost:" + localPort).build();
    }

    protected abstract ConfigurableApplicationContext setupContext();

    @After
    public void shutdown() {
        context.close();
    }

    @Test
    public void should_return_status_503_404() {
        String instanceId = registerInstance("http://localhost:" + wireMock.port() + "/mgmt");

        //503 on invalid instance
        client.get()
              .uri("/instances/{instanceId}/actuator/info", "UNKNOWN")
              .accept(ACTUATOR_V2_MEDIATYPE)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        //404 on non-existent endpoint
        client.get()
              .uri("/instances/{instanceId}/actuator/not-exist", instanceId)
              .accept(ACTUATOR_V2_MEDIATYPE)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void should_return_status_502_504() {
        String instanceId = registerInstance("http://localhost:" + wireMock.port() + "/mgmt");

        //502 on invalid response
        wireMock.stubFor(get(urlEqualTo("/mgmt/test")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        client.get()
              .uri("/instances/{instanceId}/actuator/test", instanceId)
              .accept(ACTUATOR_V2_MEDIATYPE)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.BAD_GATEWAY);

        //504 on read timeout
        wireMock.stubFor(get(urlEqualTo("/mgmt/test")).willReturn(ok().withFixedDelay(5000)));

        client.get()
              .uri("/instances/{instanceId}/actuator/test", instanceId)
              .accept(ACTUATOR_V2_MEDIATYPE)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    public void should_forward_requests() {
        String instanceId = registerInstance("http://localhost:" + wireMock.port() + "/mgmt");

        wireMock.stubFor(get(urlEqualTo("/mgmt/test")).willReturn(
            ok("{ \"foo\" : \"bar\" }").withHeader(CONTENT_TYPE, ActuatorMediaType.V2_JSON + ";charset=UTF-8")));

        client.get()
              .uri("/instances/{instanceId}/actuator/test", instanceId)
              .accept(ACTUATOR_V2_MEDIATYPE)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.OK)
              .expectBody()
              .jsonPath("$.foo")
              .isEqualTo("bar");

        wireMock.stubFor(post(urlEqualTo("/mgmt/test")).willReturn(ok()));

        client.post()
              .uri("/instances/{instanceId}/actuator/test", instanceId)
              .syncBody("PAYLOAD")
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.OK);

        wireMock.verify(postRequestedFor(urlEqualTo("/mgmt/test")).withRequestBody(equalTo("PAYLOAD")));

        wireMock.stubFor(delete(urlEqualTo("/mgmt/test")).willReturn(
            serverError().withBody("\"error\": \"You're doing it wrong!\"}")
                         .withHeader(CONTENT_TYPE, ActuatorMediaType.V2_JSON + ";charset=UTF-8")));

        client.delete()
              .uri("/instances/{instanceId}/actuator/test", instanceId)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
              .expectBody()
              .jsonPath("$.error", "You're doing it wrong!");

        wireMock.verify(postRequestedFor(urlEqualTo("/mgmt/test")).withRequestBody(equalTo("PAYLOAD")));
    }

    private String registerInstance(String managementUrl) {
        AtomicReference<String> instanceId = new AtomicReference<>();

        wireMock.stubFor(get(urlEqualTo("/mgmt/health")).willReturn(
            ok("{ \"status\" : \"UP\" }").withHeader(CONTENT_TYPE, ActuatorMediaType.V2_JSON)));

        wireMock.stubFor(
            get(urlEqualTo("/mgmt/info")).willReturn(ok("{ }").withHeader(CONTENT_TYPE, ActuatorMediaType.V2_JSON)));

        String actuatorIndex = "{ \"_links\": { \"test\": { \"href\": \"" +
                               managementUrl +
                               "/test\", \"templated\": false } } }";
        wireMock.stubFor(
            get(urlEqualTo("/mgmt")).willReturn(ok(actuatorIndex).withHeader(CONTENT_TYPE, ActuatorMediaType.V2_JSON)));

        StepVerifier.create(getEventStream())
                    .expectSubscription()
                    .then(() -> instanceId.set(sendRegistration(managementUrl)))
                    .thenConsumeWhile(event -> !event.get("type").equals("ENDPOINTS_DETECTED"))
                    .assertNext(event -> assertThat(event).containsEntry("type", "ENDPOINTS_DETECTED"))
                    .thenCancel()
                    .verify();

        return instanceId.get();
    }

    private String sendRegistration(String managementUrl) {
        String registration = "{ \"name\": \"test\", \"healthUrl\": \"" +
                              managementUrl +
                              "/health\", \"managementUrl\": \"" +
                              managementUrl +
                              "\" }";
        //@formatter:off
        EntityExchangeResult<Map<String, Object>> result = client.post()
                                                                .uri("/instances")
                                                                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
                                                                .syncBody(registration)
                                                                .exchange()
                                                                .expectStatus().isCreated()
                                                                .expectBody(RESPONSE_TYPE)
                                                                .returnResult();
        //@formatter:on
        assertThat(result.getResponseBody()).containsKeys("id");
        return result.getResponseBody().get("id").toString();
    }

    private Flux<Map<String, Object>> getEventStream() {
        //@formatter:off
        return this.client.get().uri("/instances/events").accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(RESPONSE_TYPE).getResponseBody();
        //@formatter:on
    }
}
