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
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import reactor.test.StepVerifier;
import wiremock.org.eclipse.jetty.http.HttpStatus;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.options;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProbeEndpointsStrategyTest {
    @ClassRule
    public static final WireMockClassRule wireMockClassRule = new WireMockClassRule(Options.DYNAMIC_PORT);

    @Rule
    public WireMockClassRule wireMock = wireMockClassRule;

    private InstanceWebClient instanceWebClient = new InstanceWebClient(instance -> HttpHeaders.EMPTY);

    @Test
    public void invariants() {
        assertThatThrownBy(() -> new ProbeEndpointsStrategy(instanceWebClient, null)).isInstanceOf(
            IllegalArgumentException.class).hasMessage("'endpoints' must not be null.");
        assertThatThrownBy(() -> new ProbeEndpointsStrategy(instanceWebClient, new String[]{null})).isInstanceOf(
            IllegalArgumentException.class).hasMessage("'endpoints' must not contain null.");
    }

    @Test
    public void should_return_detect_endpoints() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/mgmt/health"))
                                                          .managementUrl(wireMock.url("/mgmt"))
                                                          .build());

        wireMock.stubFor(options(urlEqualTo("/mgmt/stats")).willReturn(ok()));
        wireMock.stubFor(options(urlEqualTo("/mgmt/info")).willReturn(ok()));
        wireMock.stubFor(options(urlEqualTo("/mgmt/non-exist")).willReturn(notFound()));

        ProbeEndpointsStrategy strategy = new ProbeEndpointsStrategy(instanceWebClient,
            new String[]{"metrics:stats", "info", "non-exist"});

        //when
        StepVerifier.create(strategy.detectEndpoints(instance))
                    //then
                    .expectNext(Endpoints.single("metrics", wireMock.url("/mgmt/stats"))
                                         .withEndpoint("info", wireMock.url("/mgmt/info")))//
                    .verifyComplete();
    }

    @Test
    public void should_return_empty() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/mgmt/health"))
                                                          .managementUrl(wireMock.url("/mgmt"))
                                                          .build());

        wireMock.stubFor(
            options(urlEqualTo("/mgmt/stats")).willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND_404)));

        ProbeEndpointsStrategy strategy = new ProbeEndpointsStrategy(instanceWebClient, new String[]{"metrics:stats"});

        //when
        StepVerifier.create(strategy.detectEndpoints(instance))
                    //then
                    .verifyComplete();
    }
}
