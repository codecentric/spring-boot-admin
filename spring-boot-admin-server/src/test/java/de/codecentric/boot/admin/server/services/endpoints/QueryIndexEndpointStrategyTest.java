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
import de.codecentric.boot.admin.server.web.client.InstanceOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class QueryIndexEndpointStrategyTest {
    private Mono<ClientResponse> responseNotFound = mockResponse(HttpStatus.NOT_FOUND, null, null);
    private Mono<ClientResponse> responseOkWrongContentType = mockResponse(HttpStatus.OK, MediaType.APPLICATION_JSON,
            null);
    private Mono<ClientResponse> responseOk = mockResponse(HttpStatus.OK,
            MediaType.parseMediaType(ActuatorMediaType.V2_JSON),
            createBody("metrics=http://app/mgmt/stats", "info=http://app/mgmt/info", "self=http://app/mgmt"));
    private Mono<ClientResponse> responseOkEmpty = mockResponse(HttpStatus.OK,
            MediaType.parseMediaType(ActuatorMediaType.V2_JSON), createBody("self=http://app/mgmt"));

    private Instance instance = Instance.create(InstanceId.of("id"))
                                        .register(Registration.create("test", "http://app/mgmt/health")
                                                              .managementUrl("http://app/mgmt")
                                                              .build());

    @Test
    public void should_return_endpoints() {
        //given
        InstanceOperations ops = mock(InstanceOperations.class);
        when(ops.exchange(HttpMethod.GET, instance, URI.create("http://app/mgmt"))).thenReturn(responseOk);
        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(ops);

        //when/then
        StepVerifier.create(strategy.detectEndpoints(instance))
                    .expectNext(Endpoints.single("metrics", "http://app/mgmt/stats")
                                         .withEndpoint("info", "http://app/mgmt/info"))
                    .verifyComplete();
    }

    @Test
    public void should_return_empty_on_empty_endpoints() {
        //given
        InstanceOperations ops = mock(InstanceOperations.class);
        when(ops.exchange(HttpMethod.GET, instance, URI.create("http://app/mgmt"))).thenReturn(responseOkEmpty);
        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(ops);

        //when/then
        StepVerifier.create(strategy.detectEndpoints(instance)).verifyComplete();
    }

    @Test
    public void should_return_empty_on_not_found() {
        //given
        InstanceOperations ops = mock(InstanceOperations.class);
        when(ops.exchange(HttpMethod.GET, instance, URI.create("http://app/mgmt"))).thenReturn(responseNotFound);
        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(ops);

        //when/then
        StepVerifier.create(strategy.detectEndpoints(instance)).verifyComplete();
    }

    @Test
    public void should_return_empty_on_wrong_content_type() {
        //given
        InstanceOperations ops = mock(InstanceOperations.class);
        when(ops.exchange(HttpMethod.GET, instance, URI.create("http://app/mgmt"))).thenReturn(
                responseOkWrongContentType);
        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(ops);

        //when/then
        StepVerifier.create(strategy.detectEndpoints(instance)).verifyComplete();
    }

    @Test
    public void should_return_empty_when_mgmt_equals_service_url() {
        //given
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", "http://app/mgmt/health")
                                                          .managementUrl("http://app")
                                                          .serviceUrl("http://app")
                                                          .build());
        InstanceOperations ops = mock(InstanceOperations.class);
        QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(ops);

        //when/then
        StepVerifier.create(strategy.detectEndpoints(instance)).verifyComplete();
        verifyZeroInteractions(ops);
    }

    @SuppressWarnings("unchecked")
    private Mono<ClientResponse> mockResponse(HttpStatus statusCode,
                                              MediaType mediaType,
                                              QueryIndexEndpointStrategy.Response body) {
        ClientResponse mock = mock(org.springframework.web.reactive.function.client.ClientResponse.class);
        when(mock.statusCode()).thenReturn(statusCode);
        ClientResponse.Headers headersMock = mock(ClientResponse.Headers.class);
        when(headersMock.contentType()).thenReturn(Optional.ofNullable(mediaType));
        when(mock.headers()).thenReturn(headersMock);
        when(mock.bodyToMono(QueryIndexEndpointStrategy.Response.class)).thenReturn(Mono.justOrEmpty(body));
        return Mono.just(mock);
    }


    private QueryIndexEndpointStrategy.Response createBody(String... endpoints) {
        QueryIndexEndpointStrategy.Response body = new QueryIndexEndpointStrategy.Response();
        Map<String, QueryIndexEndpointStrategy.Response.EndpointRef> links = new HashMap<>();
        for (String endpoint : endpoints) {
            String[] tokens = endpoint.split("=");
            QueryIndexEndpointStrategy.Response.EndpointRef ref = new QueryIndexEndpointStrategy.Response.EndpointRef();
            ref.setHref(tokens[1]);
            links.put(tokens[0], ref);
        }
        body.set_links(links);
        return body;
    }

}