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
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryIndexEndpointStrategy implements EndpointDetectionStrategy {
    private final InstanceWebClient instanceWebClient;
    private static final MediaType actuatorMediaType = MediaType.parseMediaType(ActuatorMediaType.V2_JSON);

    public QueryIndexEndpointStrategy(InstanceWebClient instanceWebClient) {
        this.instanceWebClient = instanceWebClient;
    }

    @Override
    public Mono<Endpoints> detectEndpoints(Instance instance) {
        Registration registration = instance.getRegistration();
        String managementUrl = registration.getManagementUrl();
        if (managementUrl == null || Objects.equals(registration.getServiceUrl(), managementUrl)) {
            return Mono.empty();
        }

        return instanceWebClient.instance(instance)
                                .get()
                                .uri(managementUrl)
                                .exchange()
                                .flatMap(response -> {
                                    if (response.statusCode().is2xxSuccessful() &&
                                        response.headers()
                                                .contentType()
                                                .map(actuatorMediaType::isCompatibleWith)
                                                .orElse(false)) {
                                        return response.bodyToMono(Response.class);
                                    } else {
                                        return response.bodyToMono(Void.class).then(Mono.empty());
                                    }
                                })
                                .flatMap(this::convert);
    }

    private Mono<Endpoints> convert(Response response) {
        List<Endpoint> endpoints = response.getLinks()
                                           .entrySet()
                                           .stream()
                                           .filter(e -> !e.getKey().equals("self") && !e.getValue().isTemplated())
                                           .map(e -> Endpoint.of(e.getKey(), e.getValue().getHref()))
                                           .collect(Collectors.toList());
        if (endpoints.isEmpty()) {
            return Mono.empty();
        } else {
            return Mono.just(Endpoints.of(endpoints));
        }
    }

    @Data
    static class Response {
        @JsonProperty("_links")
        private Map<String, EndpointRef> links = new HashMap<>();

        @Data
        static class EndpointRef {
            private final String href;
            private final boolean templated;

            @JsonCreator
            EndpointRef(@JsonProperty("href") String href, @JsonProperty("templated") boolean templated) {
                this.href = href;
                this.templated = templated;
            }
        }
    }
}
