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
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryIndexEndpointStrategy implements EndpointDetectionStrategy {
    private static final Logger log = LoggerFactory.getLogger(QueryIndexEndpointStrategy.class);
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
            log.debug("Querying actuator-index for instance {} omitted.", instance.getId());
            return Mono.empty();
        }

        return this.instanceWebClient.instance(instance)
                                     .get()
                                     .uri(managementUrl)
                                     .exchange()
                                     .flatMap(this.convert(instance, managementUrl))
                                     .onErrorResume(e -> {
                                         log.warn(
                                             "Querying actuator-index for instance {} on '{}' failed: {}",
                                             instance.getId(),
                                             managementUrl,
                                             e.getMessage()
                                         );
                                         log.debug(
                                             "Querying actuator-index for instance {} on '{}' failed.",
                                             instance.getId(),
                                             managementUrl,
                                             e
                                         );
                                         return Mono.empty();
                                     });
    }

    protected Function<ClientResponse, Mono<Endpoints>> convert(Instance instance, String managementUrl) {
        return response -> {
            if (!response.statusCode().is2xxSuccessful()) {
                log.debug(
                    "Querying actuator-index for instance {} on '{}' failed with status {}.",
                    instance.getId(),
                    managementUrl,
                    response.rawStatusCode()
                );
                return response.bodyToMono(Void.class).then(Mono.empty());
            }

            if (!response.headers().contentType().map(actuatorMediaType::isCompatibleWith).orElse(false)) {
                log.debug(
                    "Querying actuator-index for instance {} on '{}' failed with incompatible Content-Type '{}'.",
                    instance.getId(),
                    managementUrl,
                    response.headers().contentType().map(Objects::toString).orElse("(missing)")
                );
                return response.bodyToMono(Void.class).then(Mono.empty());
            }

            log.debug("Querying actuator-index for instance {} on '{}' successful.", instance.getId(), managementUrl);
            return response.bodyToMono(Response.class)
                           .flatMap(this::convertResponse)
                           .map(this.alignWithManagementUrl(instance.getId(), managementUrl));
        };
    }

    protected Function<Endpoints, Endpoints> alignWithManagementUrl(InstanceId instanceId, String managementUrl) {
        return endpoints -> {
            if (!managementUrl.startsWith("https:")) {
                return endpoints;
            }
            if (endpoints.stream().noneMatch(e -> e.getUrl().startsWith("http:"))) {
                return endpoints;
            }
            log.warn(
                "Endpoints for instance {} queried from {} are falsely using http. Rewritten to https. Consider configuring this instance to use 'server.use-forward-headers=true'.",
                instanceId,
                managementUrl
            );

            return Endpoints.of(endpoints.stream()
                                         .map(e -> Endpoint.of(e.getId(), e.getUrl().replaceFirst("http:", "https:")))
                                         .collect(Collectors.toList()));
        };
    }

    protected Mono<Endpoints> convertResponse(Response response) {
        List<Endpoint> endpoints = response.getLinks()
                                           .entrySet()
                                           .stream()
                                           .filter(e -> !e.getKey().equals("self") && !e.getValue().isTemplated())
                                           .map(e -> Endpoint.of(e.getKey(), e.getValue().getHref()))
                                           .collect(Collectors.toList());
        return endpoints.isEmpty() ? Mono.empty() : Mono.just(Endpoints.of(endpoints));
    }

    @Data
    protected static class Response {
        @JsonProperty("_links")
        private Map<String, EndpointRef> links = new HashMap<>();

        @Data
        protected static class EndpointRef {
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
