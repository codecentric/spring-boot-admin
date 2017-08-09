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

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.ApplicationOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProbeEndpointsStrategyTest {
    private Mono<ClientResponse> responseOK = mockResponse(HttpStatus.OK);
    private Mono<ClientResponse> responseNotFound = mockResponse(HttpStatus.NOT_FOUND);

    @Test
    public void invariants() {
        ApplicationOperations ops = mock(ApplicationOperations.class);
        assertThatThrownBy(() -> {
            new ProbeEndpointsStrategy(ops, null);
        }).isInstanceOf(IllegalArgumentException.class).hasMessage("'endpoints' must not be null.");
        assertThatThrownBy(() -> {
            new ProbeEndpointsStrategy(ops, new String[]{null});
        }).isInstanceOf(IllegalArgumentException.class).hasMessage("'endpoints' must not contain null.");
    }

    @Test
    public void should_return_detect_endpoints() {
        //given
        Application application = Application.create(ApplicationId.of("id"))
                                             .register(Registration.create("test", "http://app/mgmt/health")
                                                                   .managementUrl("http://app/mgmt")
                                                                   .build());
        ApplicationOperations ops = mock(ApplicationOperations.class);

        when(ops.exchange(HttpMethod.OPTIONS, application, URI.create("http://app/mgmt/stats"))).thenReturn(responseOK);
        when(ops.exchange(HttpMethod.OPTIONS, application, URI.create("http://app/mgmt/info"))).thenReturn(responseOK);
        when(ops.exchange(HttpMethod.OPTIONS, application, URI.create("http://app/mgmt/non-exist"))).thenReturn(
                responseNotFound);

        ProbeEndpointsStrategy strategy = new ProbeEndpointsStrategy(ops,
                new String[]{"metrics:stats", "info", "non-exist"});

        //when/then
        StepVerifier.create(strategy.detectEndpoints(application))
                    .expectNext(Endpoints.single("metrics", "http://app/mgmt/stats")
                                         .withEndpoint("info", "http://app/mgmt/info"))
                    .verifyComplete();
    }

    @Test
    public void should_return_empty() {
        //given
        Application application = Application.create(ApplicationId.of("id"))
                                             .register(Registration.create("test", "http://app/mgmt/health")
                                                                   .managementUrl("http://app/mgmt")
                                                                   .build());
        ApplicationOperations ops = mock(ApplicationOperations.class);

        when(ops.exchange(HttpMethod.OPTIONS, application, URI.create("http://app/mgmt/stats"))).thenReturn(
                responseNotFound);

        ProbeEndpointsStrategy strategy = new ProbeEndpointsStrategy(ops, new String[]{"metrics:stats"});

        //when/then
        StepVerifier.create(strategy.detectEndpoints(application)).verifyComplete();
    }

    private Mono<ClientResponse> mockResponse(HttpStatus statusCode) {
        ClientResponse mock = mock(ClientResponse.class);
        when(mock.statusCode()).thenReturn(statusCode);
        return Mono.just(mock);
    }
}