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

package de.codecentric.boot.admin.server.services.endpoints;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import reactor.test.StepVerifier;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.MediaType;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.util.Collections.singletonMap;

public class QueryIndexEndpointStrategyTest {

    @Rule
    public WireMockRule wireMock = new WireMockRule(Options.DYNAMIC_PORT);

    private InstanceWebClient instanceWebClient = InstanceWebClient.builder()
                                                                   .retries(singletonMap(
                                                                       Endpoint.ACTUATOR_INDEX,
                                                                       1
                                                                   ))
                                                                   .build();

    @Test
    public void should_return_endpoints() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/mgmt/health"))
                                                          .managementUrl(wireMock.url("/mgmt"))
                                                          .build());

        String body = "{\"_links\":{\"metrics-requiredMetricName\":{\"templated\":true,\"href\":\"\\/mgmt\\/metrics\\/{requiredMetricName}\"},\"self\":{\"templated\":false,\"href\":\"\\/mgmt\"},\"metrics\":{\"templated\":false,\"href\":\"\\/mgmt\\/stats\"},\"info\":{\"templated\":false,\"href\":\"\\/mgmt\\/info\"}}}";

        wireMock.stubFor(get("/mgmt").willReturn(ok(body).withHeader("Content-Type", ActuatorMediaType.V2_JSON)
                                                         .withHeader("Content-Length",
                                                             Integer.toString(body.length())
                                                         )));

        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(instanceWebClient);

        //when
        StepVerifier.create(strategy.detectEndpoints(instance))
                    //then
                    .expectNext(Endpoints.single("metrics", "/mgmt/stats").withEndpoint("info", "/mgmt/info"))//
                    .verifyComplete();
    }

    @Test
    public void should_return_empty_on_empty_endpoints() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/mgmt/health"))
                                                          .managementUrl(wireMock.url("/mgmt"))
                                                          .build());

        String body = "{\"_links\":{}}";
        wireMock.stubFor(get("/mgmt").willReturn(okJson(body).withHeader("Content-Type", ActuatorMediaType.V2_JSON)
                                                             .withHeader("Content-Length",
                                                                 Integer.toString(body.length())
                                                             )));

        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(instanceWebClient);

        //when
        StepVerifier.create(strategy.detectEndpoints(instance))
                    //then
                    .verifyComplete();
    }

    @Test
    public void should_return_empty_on_not_found() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/mgmt/health"))
                                                          .managementUrl(wireMock.url("/mgmt"))
                                                          .build());

        wireMock.stubFor(get("/mgmt").willReturn(notFound()));

        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(instanceWebClient);

        //when
        StepVerifier.create(strategy.detectEndpoints(instance))
                    //then
                    .verifyComplete();
    }

    @Test
    public void should_return_empty_on_wrong_content_type() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/mgmt/health"))
                                                          .managementUrl(wireMock.url("/mgmt"))
                                                          .build());

        String body = "HELLOW WORLD";
        wireMock.stubFor(get("/mgmt").willReturn(ok(body).withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                                                         .withHeader("Content-Length",
                                                             Integer.toString(body.length())
                                                         )));

        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(instanceWebClient);

        //when
        StepVerifier.create(strategy.detectEndpoints(instance))
                    //then
                    .verifyComplete();
    }

    @Test
    public void should_return_empty_when_mgmt_equals_service_url() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/app/health"))
                                                          .managementUrl(wireMock.url("/app"))
                                                          .serviceUrl(wireMock.url("/app"))
                                                          .build());

        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(instanceWebClient);

        //when/then
        StepVerifier.create(strategy.detectEndpoints(instance)).verifyComplete();
        wireMock.verify(0, anyRequestedFor(urlPathEqualTo("/app")));
    }

    @Test
    public void should_retry() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/mgmt/health"))
                                                          .managementUrl(wireMock.url("/mgmt"))
                                                          .build());

        String body = "{\"_links\":{\"metrics-requiredMetricName\":{\"templated\":true,\"href\":\"\\/mgmt\\/metrics\\/{requiredMetricName}\"},\"self\":{\"templated\":false,\"href\":\"\\/mgmt\"},\"metrics\":{\"templated\":false,\"href\":\"\\/mgmt\\/stats\"},\"info\":{\"templated\":false,\"href\":\"\\/mgmt\\/info\"}}}";

        wireMock.stubFor(get("/mgmt").inScenario("retry")
                                     .whenScenarioStateIs(STARTED)
                                     .willReturn(aResponse().withFixedDelay(5000))
                                     .willSetStateTo("recovered"));

        wireMock.stubFor(get("/mgmt").inScenario("retry")
                                     .whenScenarioStateIs("recovered")
                                     .willReturn(ok(body).withHeader("Content-Type", ActuatorMediaType.V2_JSON)
                                                         .withHeader("Content-Length",
                                                             Integer.toString(body.length())
                                                         )));

        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(instanceWebClient);

        //when
        StepVerifier.create(strategy.detectEndpoints(instance))
                    //then
                    .expectNext(Endpoints.single("metrics", "/mgmt/stats").withEndpoint("info", "/mgmt/info"))//
                    .verifyComplete();
    }
}
