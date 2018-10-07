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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.options;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static de.codecentric.boot.admin.server.utils.MediaType.ACTUATOR_V2_MEDIATYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ALLOW;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

public abstract class AbstractInstancesProxyControllerIntegrationTest {
    private static final String ACTUATOR_CONTENT_TYPE = ActuatorMediaType.V2_JSON + ";charset=UTF-8";
    private static ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    @Rule
    public WireMockRule wireMock = new WireMockRule(WireMockConfiguration.options()
                                                                         .dynamicPort()
                                                                         .extensions(new ConnectionCloseExtension()));

    private static WebTestClient client;
    private static String instanceId;

    @BeforeClass
    public static void setUp() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(5));
    }

    @AfterClass
    public static void tearDown() {
        StepVerifier.resetDefaultTimeout();
    }

    public void setUpClient(ConfigurableApplicationContext context) {
        int localPort = context.getEnvironment().getProperty("local.server.port", Integer.class, 0);
        client = WebTestClient.bindToServer()
                              .baseUrl("http://localhost:" + localPort)
                              .responseTimeout(Duration.ofSeconds(10))
                              .build();

        String managementUrl = "http://localhost:" + wireMock.port() + "/mgmt";
        //@formatter:off
        String actuatorIndex = "{ \"_links\": { " +
                               "\"env\": { \"href\": \"" + managementUrl + "/env\", \"templated\": false }," +
                               "\"test\": { \"href\": \"" + managementUrl + "/test\", \"templated\": false }," +
                               "\"invalid\": { \"href\": \"" + managementUrl + "/invalid\", \"templated\": false }," +
                               "\"timeout\": { \"href\": \"" + managementUrl + "/timeout\", \"templated\": false }" +
                               " } }";
        //@formatter:on
        wireMock.stubFor(get(urlEqualTo("/mgmt/health")).willReturn(ok("{ \"status\" : \"UP\" }").withHeader(CONTENT_TYPE,
            ActuatorMediaType.V2_JSON
        )));
        wireMock.stubFor(get(urlEqualTo("/mgmt/info")).willReturn(ok("{ }").withHeader(CONTENT_TYPE,
            ACTUATOR_CONTENT_TYPE
        )));
        wireMock.stubFor(options(urlEqualTo("/mgmt/env")).willReturn(ok().withHeader(ALLOW,
            HttpMethod.HEAD.name(),
            HttpMethod.GET.name(),
            HttpMethod.OPTIONS.name()
        )));
        wireMock.stubFor(get(urlEqualTo("/mgmt")).willReturn(ok(actuatorIndex).withHeader(CONTENT_TYPE,
            ACTUATOR_CONTENT_TYPE
        )));
        wireMock.stubFor(get(urlEqualTo("/mgmt/invalid")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));
        wireMock.stubFor(get(urlEqualTo("/mgmt/timeout")).willReturn(ok().withFixedDelay(10000)));
        wireMock.stubFor(get(urlEqualTo("/mgmt/test")).willReturn(ok("{ \"foo\" : \"bar\" }").withHeader(CONTENT_TYPE,
            ACTUATOR_CONTENT_TYPE
        )));
        wireMock.stubFor(get(urlEqualTo("/mgmt/test/has%20spaces")).willReturn(ok("{ \"foo\" : \"bar-with-spaces\" }").withHeader(CONTENT_TYPE,
            ACTUATOR_CONTENT_TYPE
        )));
        wireMock.stubFor(post(urlEqualTo("/mgmt/test")).willReturn(ok()));
        wireMock.stubFor(delete(urlEqualTo("/mgmt/test")).willReturn(serverError().withBody(
            "{\"error\": \"You're doing it wrong!\"}").withHeader(CONTENT_TYPE, ACTUATOR_CONTENT_TYPE)));

        instanceId = registerInstance(managementUrl);
    }

    @Test
    public void should_return_status_503_404() {
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
        //502 on invalid response
        client.get()
              .uri("/instances/{instanceId}/actuator/invalid", instanceId)
              .accept(ACTUATOR_V2_MEDIATYPE)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.BAD_GATEWAY);

        //504 on read timeout
        client.get()
              .uri("/instances/{instanceId}/actuator/timeout", instanceId)
              .accept(ACTUATOR_V2_MEDIATYPE)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    public void should_forward_requests() {
        client.options()
              .uri("/instances/{instanceId}/actuator/env", instanceId)
              .accept(ACTUATOR_V2_MEDIATYPE)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.OK)
              .expectHeader()
              .valueEquals(ALLOW, HttpMethod.HEAD.name(), HttpMethod.GET.name(), HttpMethod.OPTIONS.name());

        client.get()
              .uri("/instances/{instanceId}/actuator/test", instanceId)
              .accept(ACTUATOR_V2_MEDIATYPE)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.OK)
              .expectBody(String.class)
              .isEqualTo("{ \"foo\" : \"bar\" }");

        client.post()
              .uri("/instances/{instanceId}/actuator/test", instanceId)
              .syncBody("PAYLOAD")
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.OK);

        wireMock.verify(postRequestedFor(urlEqualTo("/mgmt/test")).withRequestBody(equalTo("PAYLOAD")));

        client.delete()
              .uri("/instances/{instanceId}/actuator/test", instanceId)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
              .expectBody(String.class)
              .isEqualTo("{\"error\": \"You're doing it wrong!\"}");

        wireMock.verify(postRequestedFor(urlEqualTo("/mgmt/test")).withRequestBody(equalTo("PAYLOAD")));
    }

    @Test
    public void should_forward_requests_with_spaces_in_path() {
        client.get()
              .uri("/instances/{instanceId}/actuator/test/has spaces", instanceId)
              .accept(ACTUATOR_V2_MEDIATYPE)
              .exchange()
              .expectStatus()
              .isEqualTo(HttpStatus.OK)
              .expectBody(String.class)
              .isEqualTo("{ \"foo\" : \"bar-with-spaces\" }");

        wireMock.verify(getRequestedFor(urlEqualTo("/mgmt/test/has%20spaces")));
    }

    private static String registerInstance(String managementUrl) {
        AtomicReference<String> instanceId = new AtomicReference<>();
        StepVerifier.create(getEventStream())
                    .expectSubscription()
                    .then(() -> instanceId.set(sendRegistration(managementUrl)))
                    .thenConsumeWhile(event -> !event.get("type").equals("ENDPOINTS_DETECTED"))
                    .assertNext(event -> assertThat(event).containsEntry("type", "ENDPOINTS_DETECTED"))
                    .thenCancel()
                    .verify();
        return instanceId.get();
    }

    private static String sendRegistration(String managementUrl) {
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

    private static Flux<Map<String, Object>> getEventStream() {
        //@formatter:off
        return client.get().uri("/instances/events").accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(RESPONSE_TYPE).getResponseBody();
        //@formatter:on
    }
}

//Force the connections to be closed...
//see https://github.com/tomakehurst/wiremock/issues/485
class ConnectionCloseExtension extends ResponseTransformer {
    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        return Response.Builder.like(response)
                               .headers(HttpHeaders.copyOf(response.getHeaders())
                                                   .plus(new HttpHeader("Connection", "Close")))
                               .build();
    }

    @Override
    public String getName() {
        return "ConnectionCloseExtension";
    }
}
