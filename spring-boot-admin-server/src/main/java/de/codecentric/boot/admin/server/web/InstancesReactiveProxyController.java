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
package de.codecentric.boot.admin.server.web;

import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Http Handler for proxied requests
 */
@AdminController
public class InstancesReactiveProxyController extends AbstractInstancesProxyController {
    public InstancesReactiveProxyController(InstanceRegistry registry, InstanceWebClient instanceWebClient) {
        super(registry, instanceWebClient);
    }

    @RequestMapping(path = REQUEST_MAPPING_PATH)
    public Mono<Void> endpointProxy(@PathVariable("instanceId") String instanceId,
                                    ServerHttpRequest request,
                                    ServerHttpResponse response) {
        String endpointLocalPath = getEndpointLocalPath(request.getPath().pathWithinApplication().value());
        URI uri = UriComponentsBuilder.fromPath(endpointLocalPath).query(request.getURI().getRawQuery()).build(true)
                                      .toUri();

        return super.forward(instanceId, uri, request.getMethod(), request.getHeaders(),
                () -> BodyInserters.fromDataBuffers(request.getBody())).flatMap(clientResponse -> {
            response.setStatusCode(clientResponse.statusCode());
            response.getHeaders().addAll(clientResponse.headers().asHttpHeaders());
            Arrays.stream(HOP_BY_HOP_HEADERS).forEach(response.getHeaders()::remove);
            return response.writeWith(clientResponse.body(BodyExtractors.toDataBuffers()));
        });
    }
}
