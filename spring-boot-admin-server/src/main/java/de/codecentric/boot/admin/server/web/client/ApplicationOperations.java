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

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.mvc.ActuatorMediaTypes;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Handles all rest operations invoked on a registered application.
 *
 * @author Johannes Edmeier
 */
public class ApplicationOperations {
    private static final Logger log = LoggerFactory.getLogger(ApplicationOperations.class);
    @SuppressWarnings("unchecked")
    private static final Class<Map<String, Serializable>> RESPONSE_TYPE_MAP = (Class<Map<String, Serializable>>) (Class<?>) Map.class;
    private final WebClient webClient;
    private final HttpHeadersProvider httpHeadersProvider;

    public ApplicationOperations(WebClient webClient, HttpHeadersProvider httpHeadersProvider) {
        this.webClient = webClient;
        this.httpHeadersProvider = httpHeadersProvider;
    }

    public Mono<ResponseEntity<Map<String, Serializable>>> getHealth(Application application) {
        URI uri = UriComponentsBuilder.fromHttpUrl(application.getRegistration().getHealthUrl()).build().toUri();
        return this.exchange(HttpMethod.GET, application, uri).flatMap(r -> r.toEntity(RESPONSE_TYPE_MAP));
    }

    public Mono<ResponseEntity<Map<String, Serializable>>> getInfo(Application application) {
        return getEndpoint(application, Endpoint.INFO);
    }

    public Mono<ResponseEntity<Map<String, Serializable>>> getEndpoint(Application application, String endpointId) {
        URI uri = URI.create(application.getEndpoints().get(endpointId).getUrl());
        return this.exchange(HttpMethod.GET, application, uri).flatMap(r -> r.toEntity(RESPONSE_TYPE_MAP));
    }

    public Mono<ClientResponse> exchange(HttpMethod method, Application application, URI uri) {
        return webClient.method(method)
                        .uri(uri).accept(ActuatorMediaTypes.APPLICATION_ACTUATOR_V2_JSON, MediaType.APPLICATION_JSON)
                        .headers(headers -> headers.putAll(httpHeadersProvider.getHeaders(application)))
                        .exchange().doOnSubscribe((s) -> log.debug("Do {} on '{}' for {}", method, uri, application));
    }
}
