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

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import io.netty.handler.timeout.ReadTimeoutException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.OptionalLong;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okForContentType;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.EMPTY;

public class InstanceWebClientTest {
    @ClassRule
    public static WireMockClassRule wireMockClass = new WireMockClassRule(Options.DYNAMIC_PORT);
    @Rule
    public WireMockClassRule wireMock = wireMockClass;

    private final HttpHeadersProvider headersProvider = mock(HttpHeadersProvider.class, invocation -> EMPTY);
    private final InstanceWebClient instanceWebClient = new InstanceWebClient(headersProvider);

    @Test
    public void should_rewirte_url() {
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/status")).build());
        wireMock.stubFor(get("/status").willReturn(ok()));

        Mono<ClientResponse> exchange = instanceWebClient.instance(instance).get().uri("health").exchange();

        StepVerifier.create(exchange).expectNextCount(1).verifyComplete();
        wireMock.verify(1,
            getRequestedFor(urlEqualTo("/status")).withHeader(ACCEPT, equalTo(MediaType.APPLICATION_JSON_VALUE))
                                                  .withHeader(ACCEPT, equalTo(ActuatorMediaType.V1_JSON))
                                                  .withHeader(ACCEPT, equalTo(ActuatorMediaType.V2_JSON)));
    }

    @Test
    public void should_not_rewrite_absolute_url() {
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/status")).build());
        wireMock.stubFor(get("/foo").willReturn(ok()));

        Mono<ClientResponse> exchange = instanceWebClient.instance(Mono.just(instance))
                                                         .get()
                                                         .uri(wireMock.url("/foo"))
                                                         .exchange();

        StepVerifier.create(exchange).expectNextCount(1).verifyComplete();
        wireMock.verify(1, getRequestedFor(urlEqualTo("/foo")));
    }

    @Test
    public void should_exchange_absolute_url_without_instance() {
        wireMock.stubFor(get("/foo").willReturn(ok()));

        Mono<ClientResponse> exchange = instanceWebClient.instance(Mono.empty())
                                                         .get()
                                                         .uri(wireMock.url("/foo"))
                                                         .exchange();

        StepVerifier.create(exchange).expectNextCount(1).verifyComplete();
    }

    @Test
    public void should_add_headers() {
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/status")).build());
        wireMock.stubFor(get("/status").willReturn(ok()));
        when(headersProvider.getHeaders(instance)).thenReturn(createHeaders(AUTHORIZATION, "streng:geheim"));

        Mono<ClientResponse> exchange = instanceWebClient.instance(instance).get().uri("health").exchange();

        StepVerifier.create(exchange).expectNextCount(1).verifyComplete();
        wireMock.verify(1, getRequestedFor(urlEqualTo("/status")).withHeader(AUTHORIZATION, equalTo("streng:geheim")));
    }

    @Test
    public void should_convert_legacy_endpont() {
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/status")).build());

        String responseBody = "{ \"status\" : \"UP\", \"foo\" : \"bar\" }";
        wireMock.stubFor(get("/status").willReturn(
            okForContentType(ActuatorMediaType.V1_JSON, responseBody).withHeader(CONTENT_LENGTH,
                Integer.toString(responseBody.length())).withHeader("X-Custom", "1234")));

        Mono<ClientResponse> exchange = instanceWebClient.instance(instance).get().uri("health").exchange();


        StepVerifier.create(exchange).assertNext(response -> {
            assertThat(response.headers().contentLength()).isEqualTo(OptionalLong.of(responseBody.length()));
            assertThat(response.headers().contentType()).contains(MediaType.parseMediaType(ActuatorMediaType.V2_JSON));
            assertThat(response.headers().header("X-Custom")).containsExactly("1234");
            assertThat(response.headers().header(CONTENT_TYPE)).containsExactly(ActuatorMediaType.V2_JSON);
            assertThat(response.headers().asHttpHeaders().get("X-Custom")).containsExactly("1234");
            assertThat(response.headers().asHttpHeaders().get(CONTENT_TYPE)).containsExactly(ActuatorMediaType.V2_JSON);
            assertThat(response.statusCode()).isEqualTo(HttpStatus.OK);
        }).verifyComplete();

        String expectedBody = "{\"status\":\"UP\",\"details\":{\"foo\":\"bar\"}}";
        StepVerifier.create(exchange.flatMap(r -> r.bodyToMono(String.class)))
                    .assertNext(actualBody -> assertThat(actualBody).isEqualTo(expectedBody))
                    .verifyComplete();

        wireMock.verify(2, getRequestedFor(urlEqualTo("/status")));
    }

    @Test
    public void should_error_on_relative_url_without_instance() {
        Mono<ClientResponse> exchange = instanceWebClient.instance(Mono.empty()).get().uri("health").exchange();
        StepVerifier.create(exchange)
                    .verifyErrorSatisfies(ex -> assertThat(ex).isInstanceOf(InstanceWebClientException.class)
                                                              .hasMessageContaining("Instance not found"));
    }

    @Test
    public void should_error_on_unknown_endpoint() {
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/status")).build());

        Mono<ClientResponse> exchange = instanceWebClient.instance(instance).get().uri("unknown").exchange();

        StepVerifier.create(exchange)
                    .verifyErrorSatisfies(ex -> assertThat(ex).isInstanceOf(InstanceWebClientException.class)
                                                              .hasMessageContaining("Endpoint 'unknown' not found"));
    }

    @Test
    public void should_error_on_missing_endpoint() {
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(Registration.create("test", wireMock.url("/status")).build());

        Mono<ClientResponse> exchange = instanceWebClient.instance(instance).get().uri("/").exchange();

        StepVerifier.create(exchange)
                    .verifyErrorSatisfies(ex -> assertThat(ex).isInstanceOf(InstanceWebClientException.class)
                                                              .hasMessageContaining("No endpoint specified"));
    }

    @Test
    public void should_error_on_timeout() {
        InstanceWebClient fastTimeoutClient = new InstanceWebClient(headersProvider, Duration.ofMillis(10),
            Duration.ofMillis(10));

        wireMock.stubFor(get("/foo").willReturn(ok().withFixedDelay(100)));

        Mono<ClientResponse> exchange = fastTimeoutClient.instance(Mono.empty())
                                                         .get()
                                                         .uri(wireMock.url("/foo"))
                                                         .exchange();

        StepVerifier.create(exchange).verifyError(ReadTimeoutException.class);
    }

    private HttpHeaders createHeaders(String k, String v) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(k, v);
        return headers;
    }

}
