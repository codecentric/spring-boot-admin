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
import java.util.concurrent.atomic.AtomicLong;
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
import org.springframework.web.reactive.function.client.ExchangeFunction;

import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.ATTRIBUTE_ENDPOINT;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class InstanceExchangeFilterFunctionsTest {
    private static final DefaultDataBufferFactory BUFFER_FACTORY = new DefaultDataBufferFactory();
    private static final DataBuffer ORIGINAL = BUFFER_FACTORY.wrap("ORIGINAL".getBytes(StandardCharsets.UTF_8));
    private static final DataBuffer CONVERTED = BUFFER_FACTORY.wrap("CONVERTED".getBytes(StandardCharsets.UTF_8));

    @Test
    public void should_convert_v1_actuator() {
        ExchangeFilterFunction filter = InstanceExchangeFilterFunctions.convertLegacyEndpoint(new TestLegacyEndpointConverter());

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
        ExchangeFilterFunction filter = InstanceExchangeFilterFunctions.convertLegacyEndpoint(new TestLegacyEndpointConverter());

        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
                                             .attribute(ATTRIBUTE_ENDPOINT, "test")
                                             .build();
        ClientResponse response = ClientResponse.create(HttpStatus.OK)
                                                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
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
        ExchangeFilterFunction filter = InstanceExchangeFilterFunctions.convertLegacyEndpoint(new TestLegacyEndpointConverter());

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

    @Test
    public void should_retry_using_default() {
        ExchangeFilterFunction filter = InstanceExchangeFilterFunctions.retry(1, emptyMap());

        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test")).build();
        ClientResponse response = mock(ClientResponse.class);

        AtomicLong invocationCount = new AtomicLong(0L);
        ExchangeFunction exchange = r -> Mono.fromSupplier(() -> {
            if (invocationCount.getAndIncrement() == 0) {
                throw new IllegalStateException("Test");
            }
            return response;
        });

        StepVerifier.create(filter.filter(request, exchange)).expectNext(response).verifyComplete();
        assertThat(invocationCount.get()).isEqualTo(2);
    }

    @Test
    public void should_retry_using_endpoint_value_default() {
        ExchangeFilterFunction filter = InstanceExchangeFilterFunctions.retry(0, singletonMap("test", 1));

        ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
                                             .attribute(ATTRIBUTE_ENDPOINT, "test")
                                             .build();
        ClientResponse response = mock(ClientResponse.class);

        AtomicLong invocationCount = new AtomicLong(0L);
        ExchangeFunction exchange = r -> Mono.fromSupplier(() -> {
            if (invocationCount.getAndIncrement() == 0) {
                throw new IllegalStateException("Test");
            }
            return response;
        });

        StepVerifier.create(filter.filter(request, exchange)).expectNext(response).verifyComplete();
        assertThat(invocationCount.get()).isEqualTo(2);
    }


    @Test
    public void should_not_retry_for_put_post_patch_delete() {
        ExchangeFilterFunction filter = InstanceExchangeFilterFunctions.retry(1, emptyMap());

        AtomicLong invocationCount = new AtomicLong(0L);
        ExchangeFunction exchange = r -> Mono.fromSupplier(() -> {
            invocationCount.incrementAndGet();
            throw new IllegalStateException("Test");
        });

        ClientRequest patchRequest = ClientRequest.create(HttpMethod.PATCH, URI.create("/test")).build();
        StepVerifier.create(filter.filter(patchRequest, exchange)).expectError(IllegalStateException.class).verify();
        assertThat(invocationCount.get()).isEqualTo(1);

        invocationCount.set(0L);
        ClientRequest putRequest = ClientRequest.create(HttpMethod.PUT, URI.create("/test")).build();
        StepVerifier.create(filter.filter(putRequest, exchange)).expectError(IllegalStateException.class).verify();
        assertThat(invocationCount.get()).isEqualTo(1);

        invocationCount.set(0L);
        ClientRequest postRequest = ClientRequest.create(HttpMethod.POST, URI.create("/test")).build();
        StepVerifier.create(filter.filter(postRequest, exchange)).expectError(IllegalStateException.class).verify();
        assertThat(invocationCount.get()).isEqualTo(1);

        invocationCount.set(0L);
        ClientRequest deleteRequest = ClientRequest.create(HttpMethod.DELETE, URI.create("/test")).build();
        StepVerifier.create(filter.filter(deleteRequest, exchange)).expectError(IllegalStateException.class).verify();
        assertThat(invocationCount.get()).isEqualTo(1);
    }

    static class TestLegacyEndpointConverter extends LegacyEndpointConverter {
        TestLegacyEndpointConverter() {
            super("test", from -> Flux.just(CONVERTED));
        }
    }
}
