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

package de.codecentric.boot.admin.server.web.reactive;

import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.web.AdminController;
import de.codecentric.boot.admin.server.web.HttpHeaderFilter;
import de.codecentric.boot.admin.server.web.InstanceWebProxy;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Set;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Http Handler for proxied requests
 */
@AdminController
public class InstancesProxyController {
    private static final String MAPPED_PATH = "/instances/{instanceId}/actuator/**";
    private final InstanceWebProxy instanceWebProxy;
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final String endpointPathPattern;
    private final HttpHeaderFilter httpHeadersFilter;

    public InstancesProxyController(String adminContextPath,
                                    Set<String> ignoredHeaders,
                                    InstanceRegistry registry,
                                    InstanceWebClient instanceWebClient) {
        this.endpointPathPattern = adminContextPath + MAPPED_PATH;
        this.instanceWebProxy = new InstanceWebProxy(registry, instanceWebClient);
        this.httpHeadersFilter = new HttpHeaderFilter(ignoredHeaders);
    }

    @RequestMapping(path = MAPPED_PATH, method = {RequestMethod.GET, RequestMethod.HEAD, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
    public Mono<Void> endpointProxy(@PathVariable("instanceId") String instanceId,
                                    ServerHttpRequest request,
                                    ServerHttpResponse response) {
        String endpointLocalPath = this.getEndpointLocalPath(request);
        URI uri = UriComponentsBuilder.fromPath(endpointLocalPath)
                                      .query(request.getURI().getRawQuery())
                                      .build(true)
                                      .toUri();

        return this.instanceWebProxy.forward(
            instanceId,
            uri,
            request.getMethod(),
            this.httpHeadersFilter.filterHeaders(request.getHeaders()),
            () -> BodyInserters.fromDataBuffers(request.getBody())
        ).flatMap(clientResponse -> {
            response.setStatusCode(clientResponse.statusCode());
            response.getHeaders()
                    .addAll(this.httpHeadersFilter.filterHeaders(clientResponse.headers().asHttpHeaders()));
            return response.writeAndFlushWith(clientResponse.body(BodyExtractors.toDataBuffers()).window(1));
        });
    }

    private String getEndpointLocalPath(ServerHttpRequest request) {
        String pathWithinApplication = request.getPath().pathWithinApplication().value();
        return this.pathMatcher.extractPathWithinPattern(this.endpointPathPattern, pathWithinApplication);
    }
}
