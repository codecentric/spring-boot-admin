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

package de.codecentric.boot.admin.server.services.endpoints;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import net.minidev.json.JSONObject;
import reactor.test.StepVerifier;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.mockito.Mockito.mock;

public class QueryIndexEndpointStrategyTest {
    @ClassRule
    public static WireMockClassRule wireMockClassRule = new WireMockClassRule(Options.DYNAMIC_PORT);

    @Rule
    public WireMockClassRule wireMock = wireMockClassRule;

    private InstanceWebClient instanceWebClient = new InstanceWebClient(
            mock(HttpHeadersProvider.class, invocation -> HttpHeaders.EMPTY));

    @Test
    public void should_return_endpoints() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/mgmt/health"))
                                                          .managementUrl(wireMock.url("/mgmt"))
                                                          .build());

        JSONObject links = new JSONObject();
        links.appendField("self", new JSONObject()//
                                                  .appendField("href", wireMock.url("/mgmt"))//
                                                  .appendField("templated", false));
        links.appendField("info", new JSONObject()//
                                                  .appendField("href", wireMock.url("/mgmt/info"))//
                                                  .appendField("templated", false));
        links.appendField("metrics", new JSONObject()//
                                                     .appendField("href", wireMock.url("/mgmt/stats"))//
                                                     .appendField("templated", false));
        links.appendField("metrics-requiredMetricName", new JSONObject()//
                                                                        .appendField("href",
                                                                                wireMock.url("/mgmt/metrics") +
                                                                                "/{requiredMetricName}")//
                                                                        .appendField("templated", true));

        JSONObject body = new JSONObject().appendField("_links", links);

        wireMock.stubFor(
                get("/mgmt").willReturn(ok(body.toJSONString()).withHeader("Content-Type", ActuatorMediaType.V2_JSON)));

        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(instanceWebClient);

        //when
        StepVerifier.create(strategy.detectEndpoints(instance))
                    //then
                    .expectNext(Endpoints.single("metrics", wireMock.url("/mgmt/stats"))
                                         .withEndpoint("info", wireMock.url("/mgmt/info")))//
                    .verifyComplete();
    }

    @Test
    public void should_return_empty_on_empty_endpoints() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/mgmt/health"))
                                                          .managementUrl(wireMock.url("/mgmt"))
                                                          .build());

        wireMock.stubFor(get("/mgmt").willReturn(ok().withHeader("Content-Type", ActuatorMediaType.V2_JSON)));

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

        wireMock.stubFor(
                get("/mgmt").willReturn(ok("HELLOW WORLD").withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)));

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

}