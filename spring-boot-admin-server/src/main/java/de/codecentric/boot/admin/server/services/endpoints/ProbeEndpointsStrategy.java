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
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.stream.Collectors.groupingBy;

public class ProbeEndpointsStrategy implements EndpointDetectionStrategy {
    private static final Logger log = LoggerFactory.getLogger(ProbeEndpointsStrategy.class);
    private final Collection<EndpointDefinition> endpoints;
    private final InstanceWebClient instanceWebClient;

    public ProbeEndpointsStrategy(InstanceWebClient instanceWebClient, String[] endpoints) {
        Assert.notNull(endpoints, "'endpoints' must not be null.");
        Assert.noNullElements(endpoints, "'endpoints' must not contain null.");
        this.endpoints = Arrays.stream(endpoints).map(EndpointDefinition::create).collect(Collectors.toList());
        this.instanceWebClient = instanceWebClient;
    }

    @Override
    public Mono<Endpoints> detectEndpoints(Instance instance) {
        return Flux.fromIterable(endpoints)
                   .flatMap(endpoint -> detectEndpoint(instance, endpoint))
                   .collectList()
                   .flatMap(this::convert);
    }

    private Mono<Endpoint> detectEndpoint(Instance instance, EndpointDefinition endpoint) {
        URI uri = UriComponentsBuilder.fromUriString(instance.getRegistration().getManagementUrl())
                                      .path("/")
                                      .path(endpoint.getPath())
                                      .build()
                                      .toUri();
        return instanceWebClient.instance(instance).options().uri(uri).exchange().flatMap(this.convert(endpoint, uri));
    }

    private Function<ClientResponse, Mono<Endpoint>> convert(EndpointDefinition endpoint, URI uri) {
        return response -> response.bodyToMono(Void.class)
                                   .then(response.statusCode()
                                                 .is2xxSuccessful() ? Mono.just(Endpoint.of(endpoint.getId(),
                                       uri.toString()
                                   )) : Mono.empty());
    }


    private Mono<Endpoints> convert(List<Endpoint> endpoints) {
        if (endpoints.isEmpty()) {
            return Mono.empty();
        }

        Map<String, List<Endpoint>> endpointsById = endpoints.stream().collect(groupingBy(Endpoint::getId));
        List<Endpoint> result = endpointsById.values().stream().map(endpointList -> {
            if (endpointList.size() > 1) {
                log.warn("Duplicate endpoints for id '{}' detected. Omitting: {}",
                    endpointList.get(0).getId(),
                    endpointList.subList(1, endpointList.size())
                );
            }
            return endpointList.get(0);
        }).collect(Collectors.toList());
        return Mono.just(Endpoints.of(result));
    }

    @Data
    private static class EndpointDefinition {
        private final String id;
        private final String path;

        private static EndpointDefinition create(String idWithPath) {
            int idxDelimiter = idWithPath.indexOf(':');
            if (idxDelimiter < 0) {
                return new EndpointDefinition(idWithPath, idWithPath);
            } else {
                return new EndpointDefinition(idWithPath.substring(0, idxDelimiter),
                    idWithPath.substring(idxDelimiter + 1)
                );
            }
        }
    }
}
