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
package de.codecentric.boot.admin.server.web.client;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Handles all rest operations invoked on a registered instance.
 *
 * @author Johannes Edmeier
 */
public class InstanceOperations {
    private static final Logger log = LoggerFactory.getLogger(InstanceOperations.class);
    @SuppressWarnings("unchecked")
    private static final Class<Map<String, Serializable>> RESPONSE_TYPE_MAP = (Class<Map<String, Serializable>>) (Class<?>) Map.class;
    private final WebClient webClient;
    private final HttpHeadersProvider httpHeadersProvider;

    public InstanceOperations(WebClient webClient, HttpHeadersProvider httpHeadersProvider) {
        this.webClient = webClient;
        this.httpHeadersProvider = httpHeadersProvider;
    }

    public Mono<ResponseEntity<Map<String, Serializable>>> getHealth(Instance instance) {
        URI uri = UriComponentsBuilder.fromHttpUrl(instance.getRegistration().getHealthUrl()).build().toUri();
        return this.exchange(HttpMethod.GET, instance, uri).flatMap(r -> r.toEntity(RESPONSE_TYPE_MAP));
    }

    public Mono<ResponseEntity<Map<String, Serializable>>> getInfo(Instance instance) {
        return getEndpoint(instance, Endpoint.INFO);
    }

    public Mono<ResponseEntity<Map<String, Serializable>>> getEndpoint(Instance instance, String endpointId) {
        URI uri = URI.create(instance.getEndpoints().get(endpointId).getUrl());
        return this.exchange(HttpMethod.GET, instance, uri).flatMap(r -> r.toEntity(RESPONSE_TYPE_MAP));
    }

    public Mono<ClientResponse> exchange(HttpMethod method, Instance instance, URI uri) {
        return webClient.method(method)
                        .uri(uri)
                        .accept(ActuatorMediaType.V2_JSON, MediaType.APPLICATION_JSON)
                        .headers(headers -> headers.putAll(httpHeadersProvider.getHeaders(instance)))
                        .exchange()
                        .doOnSubscribe((s) -> log.debug("Do {} on '{}' for {}", method, uri, instance));
    }
}
