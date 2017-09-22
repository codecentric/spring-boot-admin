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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
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
    private static final MediaType actuatorMediaType = MediaType.parseMediaType(ActuatorMediaType.V2_JSON);

    @SuppressWarnings("unchecked")
    private static final Class<Map<String, Object>> RESPONSE_TYPE_MAP = (Class<Map<String, Object>>) (Class<?>) Map.class;
    private final WebClient webClient;
    private final HttpHeadersProvider httpHeadersProvider;

    public InstanceOperations(WebClient webClient, HttpHeadersProvider httpHeadersProvider) {
        this.webClient = webClient;
        this.httpHeadersProvider = httpHeadersProvider;
    }

    public Mono<ResponseEntity<Map<String, Object>>> getHealth(Instance instance) {
        URI uri = UriComponentsBuilder.fromHttpUrl(instance.getRegistration().getHealthUrl()).build().toUri();
        return this.exchange(HttpMethod.GET, instance, uri).flatMap(r -> r.toEntity(RESPONSE_TYPE_MAP)).map(res -> {
            if (res.hasBody() && res.getBody().get("status") instanceof String) {
                return new ResponseEntity<>(convertHealthIfNecessary(res.getBody()), res.getHeaders(),
                        res.getStatusCode());
            } else {
                return res;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertHealthIfNecessary(Map<String, Object> body) {
        Map<String, Object> v1Details = body.entrySet()
                                            .stream()
                                            .map(e -> Tuples.of(e.getKey(), e.getValue()))
                                            .filter(t -> !t.getT1().equals("status") && !t.getT1().equals("details"))
                                            .map(t -> {
                                                if (t.getT2() instanceof Map) {
                                                    return Tuples.of(t.getT1(),
                                                            convertHealthIfNecessary((Map<String, Object>) t.getT2()));
                                                }
                                                return t;
                                            })
                                            .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2));
        if (v1Details.isEmpty()) {
            return body;
        }

        Object status = body.get("status");
        Map<String, Object> details;
        if (body.get("details") instanceof Map) {
            details = new HashMap<>((Map<String, Object>) body.get("details"));
        } else {
            details = new HashMap<>();
        }
        details.putAll(v1Details);

        Map<String, Object> converted = new HashMap<>();
        converted.put("status", status);
        converted.put("details", details);
        return converted;
    }

    public Mono<ResponseEntity<Map<String, Object>>> getInfo(Instance instance) {
        return getEndpoint(instance, Endpoint.INFO);
    }

    private Mono<ResponseEntity<Map<String, Object>>> getEndpoint(Instance instance, String endpointId) {
        URI uri = URI.create(instance.getEndpoints().get(endpointId).getUrl());
        return this.exchange(HttpMethod.GET, instance, uri).flatMap(r -> r.toEntity(RESPONSE_TYPE_MAP));
    }

    public Mono<ClientResponse> exchange(HttpMethod method, Instance instance, URI uri) {
        return webClient.method(method)
                        .uri(uri)
                        .accept(actuatorMediaType, MediaType.APPLICATION_JSON)
                        .headers(headers -> headers.putAll(httpHeadersProvider.getHeaders(instance)))
                        .exchange()
                        .doOnSubscribe((s) -> log.debug("Do {} on '{}' for {}", method, uri, instance));
    }
}
