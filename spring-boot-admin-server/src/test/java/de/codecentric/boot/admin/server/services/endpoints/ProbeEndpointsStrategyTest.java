/*
 * Copyright 2014-2019 the original author or authors.
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

import java.time.Duration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.options;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.retry;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.timeout;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ProbeEndpointsStrategyTest {
    @Rule
    public WireMockRule wireMock = new WireMockRule(Options.DYNAMIC_PORT);

    private InstanceWebClient instanceWebClient = InstanceWebClient.builder()
                                                                   .filter(retry(1, emptyMap()))
                                                                   .filter(timeout(Duration.ofSeconds(1), emptyMap()))
                                                                   .build();

    @BeforeClass
    public static void setUp() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(5));
    }

    @AfterClass
    public static void tearDown() {
        StepVerifier.resetDefaultTimeout();
    }

    @Test
    public void invariants() {
        assertThatThrownBy(() -> new ProbeEndpointsStrategy(this.instanceWebClient, null)).isInstanceOf(
            IllegalArgumentException.class).hasMessage("'endpoints' must not be null.");
        assertThatThrownBy(() -> new ProbeEndpointsStrategy(this.instanceWebClient, new String[]{null})).isInstanceOf(
            IllegalArgumentException.class).hasMessage("'endpoints' must not contain null.");
    }

    @Test
    public void should_return_detect_endpoints() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", this.wireMock.url("/mgmt/health"))
                                                          .managementUrl(this.wireMock.url("/mgmt"))
                                                          .build());

        this.wireMock.stubFor(options(urlEqualTo("/mgmt/metrics")).willReturn(ok()));
        this.wireMock.stubFor(options(urlEqualTo("/mgmt/stats")).willReturn(ok()));
        this.wireMock.stubFor(options(urlEqualTo("/mgmt/info")).willReturn(ok()));
        this.wireMock.stubFor(options(urlEqualTo("/mgmt/non-exist")).willReturn(notFound()));
        this.wireMock.stubFor(options(urlEqualTo("/mgmt/error")).willReturn(serverError()));
        this.wireMock.stubFor(options(urlEqualTo("/mgmt/exception")).willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

        ProbeEndpointsStrategy strategy = new ProbeEndpointsStrategy(
            this.instanceWebClient,
            new String[]{"metrics:stats", "metrics", "info", "non-exist", "error", "exception"}
        );

        //when
        StepVerifier.create(strategy.detectEndpoints(instance))
                    //then
                    .expectNext(Endpoints.single("metrics", this.wireMock.url("/mgmt/stats"))
                                         .withEndpoint("info", this.wireMock.url("/mgmt/info")))//
                    .verifyComplete();
    }

    @Test
    public void should_return_empty() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", this.wireMock.url("/mgmt/health"))
                                                          .managementUrl(this.wireMock.url("/mgmt"))
                                                          .build());

        this.wireMock.stubFor(options(urlEqualTo("/mgmt/stats")).willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND_404)));

        ProbeEndpointsStrategy strategy = new ProbeEndpointsStrategy(this.instanceWebClient,
            new String[]{"metrics:stats"}
        );

        //when
        StepVerifier.create(strategy.detectEndpoints(instance))
                    //then
                    .verifyComplete();
    }

    @Test
    public void should_retry() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", this.wireMock.url("/mgmt/health"))
                                                          .managementUrl(this.wireMock.url("/mgmt"))
                                                          .build());

        this.wireMock.stubFor(options(urlEqualTo("/mgmt/metrics")).inScenario("retry")
                                                                  .whenScenarioStateIs(STARTED)
                                                                  .willReturn(aResponse().withFixedDelay(5000))
                                                                  .willSetStateTo("recovered"));

        this.wireMock.stubFor(options(urlEqualTo("/mgmt/metrics")).inScenario("retry")
                                                                  .whenScenarioStateIs("recovered")
                                                                  .willReturn(ok()));
        this.wireMock.stubFor(options(urlEqualTo("/mgmt/stats")).willReturn(ok()));
        this.wireMock.stubFor(options(urlEqualTo("/mgmt/info")).willReturn(ok()));
        this.wireMock.stubFor(options(urlEqualTo("/mgmt/non-exist")).willReturn(notFound()));

        ProbeEndpointsStrategy strategy = new ProbeEndpointsStrategy(
            this.instanceWebClient,
            new String[]{"metrics:stats", "metrics", "info", "non-exist"}
        );

        //when
        StepVerifier.create(strategy.detectEndpoints(instance))
                    //then
                    .expectNext(Endpoints.single("metrics", this.wireMock.url("/mgmt/stats"))
                                         .withEndpoint("info", this.wireMock.url("/mgmt/info")))//
                    .verifyComplete();
    }
}
