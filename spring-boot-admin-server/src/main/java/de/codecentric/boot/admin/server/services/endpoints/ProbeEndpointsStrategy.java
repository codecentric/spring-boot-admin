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

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.web.client.ApplicationOperations;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponentsBuilder;

public class ProbeEndpointsStrategy implements EndpointDetectionStrategy {
    private final Collection<EndpointDefinition> endpoints;
    private final ApplicationOperations applicationOps;

    public ProbeEndpointsStrategy(ApplicationOperations applicationOps, String[] endpoints) {
        Assert.notNull(endpoints, "'endpoints' must not be null.");
        Assert.noNullElements(endpoints, "'endpoints' must not contain null.");
        this.endpoints = Arrays.stream(endpoints).map(EndpointDefinition::create).collect(Collectors.toList());
        this.applicationOps = applicationOps;
    }

    @Override
    public Mono<Endpoints> detectEndpoints(Application application) {
        return Flux.fromIterable(endpoints)
                   .flatMap(endpoint -> detectEndpoint(application, endpoint))
                   .collectList()
                   .flatMap(list -> list.isEmpty() ? Mono.empty() : Mono.just(Endpoints.of(list)));
    }

    private Mono<Endpoint> detectEndpoint(Application application, EndpointDefinition endpoint) {
        URI uri = UriComponentsBuilder.fromUriString(application.getRegistration().getManagementUrl())
                                      .path("/")
                                      .path(endpoint.getPath())
                                      .build()
                                      .toUri();
        return applicationOps.exchange(HttpMethod.OPTIONS, application, uri)
                             .filter(response -> response.statusCode().is2xxSuccessful())
                             .map(r -> Endpoint.of(endpoint.getId(), uri.toString()));
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
                        idWithPath.substring(idxDelimiter + 1));
            }
        }
    }
}
