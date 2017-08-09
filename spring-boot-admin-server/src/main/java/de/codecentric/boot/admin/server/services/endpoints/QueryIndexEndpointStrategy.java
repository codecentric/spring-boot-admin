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
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.ApplicationOperations;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.boot.actuate.endpoint.mvc.ActuatorMediaTypes;
import org.springframework.http.HttpMethod;

public class QueryIndexEndpointStrategy implements EndpointDetectionStrategy {
    private final ApplicationOperations applicationOps;

    public QueryIndexEndpointStrategy(ApplicationOperations applicationOps) {
        this.applicationOps = applicationOps;
    }

    @Override
    public Mono<Endpoints> detectEndpoints(Application application) {
        Registration registration = application.getRegistration();
        if (Objects.equals(registration.getServiceUrl(), registration.getManagementUrl())) {
            return Mono.empty();
        }

        return applicationOps.exchange(HttpMethod.GET, application, URI.create(registration.getManagementUrl()))
                             .filter(response -> response.statusCode().is2xxSuccessful() &&
                                                 response.headers()
                                                         .contentType()
                                                         .map(ActuatorMediaTypes.APPLICATION_ACTUATOR_V2_JSON::isCompatibleWith)
                                                         .orElse(false))
                             .flatMap(r -> r.bodyToMono(Response.class))
                             .flatMap(this::convert);
    }

    private Mono<Endpoints> convert(Response response) {
        List<Endpoint> endpoints = response.get_links()
                                           .entrySet()
                                           .stream()
                                           .filter(e -> !e.getKey().equals("self"))
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
        private Map<String, EndpointRef> _links;

        @Data
        static class EndpointRef {
            private String href;
        }
    }
}
