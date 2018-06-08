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

package de.codecentric.boot.admin.server.web.client;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.ATTRIBUTE_ENDPOINT;
import static io.netty.handler.codec.http.HttpHeaders.Values.APPLICATION_JSON;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

public class InstanceExchangeFilterFunctionsTest {
    private static final DefaultDataBufferFactory BUFFER_FACTORY = new DefaultDataBufferFactory();
    private static final DataBuffer ORIGINAL = BUFFER_FACTORY.wrap("ORIGINAL".getBytes(StandardCharsets.UTF_8));
    private static final DataBuffer CONVERTED = BUFFER_FACTORY.wrap("CONVERTED".getBytes(StandardCharsets.UTF_8));

    @Test
    public void should_convert_v1_actuator() {
        ExchangeFilterFunction filter = InstanceExchangeFilterFunctions.convertLegacyEndpoint(
            new TestLegacyEndpointConverter());

        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
                                             .attribute(ATTRIBUTE_ENDPOINT, "test")
                                             .build();
        ClientResponse response = ClientResponse.create(HttpStatus.OK)
                                                .header(CONTENT_TYPE, ActuatorMediaType.V1_JSON)
                                                .body(Flux.just(ORIGINAL))
                                                .build();

        Mono<ClientResponse> convertedResponse = filter.filter(request, r -> Mono.just(response));

        StepVerifier.create(convertedResponse)
                    .assertNext(r -> StepVerifier.create(r.body(BodyExtractors.toDataBuffers()))
                                                 .expectNext(CONVERTED)
                                                 .verifyComplete())
                    .verifyComplete();
    }

    @Test
    public void should_convert_json() {
        ExchangeFilterFunction filter = InstanceExchangeFilterFunctions.convertLegacyEndpoint(
            new TestLegacyEndpointConverter());

        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
                                             .attribute(ATTRIBUTE_ENDPOINT, "test")
                                             .build();
        ClientResponse response = ClientResponse.create(HttpStatus.OK)
                                                .header(CONTENT_TYPE, APPLICATION_JSON)
                                                .body(Flux.just(ORIGINAL))
                                                .build();

        Mono<ClientResponse> convertedResponse = filter.filter(request, r -> Mono.just(response));

        StepVerifier.create(convertedResponse)
                    .assertNext(r -> StepVerifier.create(r.body(BodyExtractors.toDataBuffers()))
                                                 .expectNext(CONVERTED)
                                                 .verifyComplete())
                    .verifyComplete();
    }

    @Test
    public void should_not_convert_v2_actuator() {
        ExchangeFilterFunction filter = InstanceExchangeFilterFunctions.convertLegacyEndpoint(
            new TestLegacyEndpointConverter());

        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
                                             .attribute(ATTRIBUTE_ENDPOINT, "test")
                                             .build();
        ClientResponse response = ClientResponse.create(HttpStatus.OK)
                                                .header(CONTENT_TYPE, ActuatorMediaType.V2_JSON)
                                                .body(Flux.just(ORIGINAL))
                                                .build();

        Mono<ClientResponse> convertedResponse = filter.filter(request, r -> Mono.just(response));

        StepVerifier.create(convertedResponse)
                    .assertNext(r -> StepVerifier.create(r.body(BodyExtractors.toDataBuffers()))
                                                 .expectNext(ORIGINAL)
                                                 .verifyComplete())
                    .verifyComplete();
    }

    static class TestLegacyEndpointConverter extends LegacyEndpointConverter {
        TestLegacyEndpointConverter() {
            super("test", from -> Flux.just(CONVERTED));
        }
    }
}
