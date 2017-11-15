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

import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import de.codecentric.boot.admin.server.web.client.InstanceWebClientException;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import static java.util.stream.Collectors.toMap;

public class AbstractInstancesProxyController {
    protected static final String REQUEST_MAPPING_PATH = "/instances/{instanceId}/actuator/**";
    protected static final String[] HOP_BY_HOP_HEADERS = new String[]{"Host", "Connection", "Keep-Alive",
        "Proxy-Authenticate", "Proxy-Authorization", "TE", "Trailer", "Transfer-Encoding", "Upgrade",
        "X-Application-Context"};
    private static final Logger log = LoggerFactory.getLogger(AbstractInstancesProxyController.class);
    private final String realRequestMappingPath;
    private final InstanceRegistry registry;
    private final InstanceWebClient instanceWebClient;
    private final Set<String> ignoredHeaders;

    public AbstractInstancesProxyController(String adminContextPath,
                                            Set<String> ignoredHeaders,
                                            InstanceRegistry registry,
                                            InstanceWebClient instanceWebClient) {
        this.ignoredHeaders = Stream.concat(ignoredHeaders.stream(), Arrays.stream(HOP_BY_HOP_HEADERS))
                                    .map(String::toLowerCase)
                                    .collect(Collectors.toSet());
        this.registry = registry;
        this.instanceWebClient = instanceWebClient;
        this.realRequestMappingPath = adminContextPath + REQUEST_MAPPING_PATH;
    }

    protected Mono<ClientResponse> forward(String instanceId,
                                           URI uri,
                                           HttpMethod method,
                                           HttpHeaders headers,
                                           Supplier<BodyInserter<?, ? super ClientHttpRequest>> bodyInserter) {
        log.trace("Proxy-Request for instance {} / {}", instanceId, uri);

        WebClient.RequestBodySpec bodySpec = instanceWebClient.instance(registry.getInstance(InstanceId.of(instanceId)))
                                                              .method(method)
                                                              .uri(uri)
                                                              .headers(instanceHeaders -> instanceHeaders.addAll(
                                                                  filterHeaders(headers)));

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec;
        if (requiresBody(method)) {
            try {
                headersSpec = bodySpec.body(bodyInserter.get());
            } catch (Exception ex) {
                return Mono.error(ex);
            }
        }

        return headersSpec.exchange()
                          .onErrorMap(InstanceWebClientException.class,
                              error -> new ResponseStatusException(HttpStatus.BAD_REQUEST, null, error))
                          .onErrorMap(ConnectException.class,
                              error -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, null, error));
    }

    protected String getEndpointLocalPath(String pathWithinApplication) {
        return new AntPathMatcher().extractPathWithinPattern(this.realRequestMappingPath, pathWithinApplication);
    }

    protected HttpHeaders filterHeaders(HttpHeaders headers) {
        HttpHeaders filtered = new HttpHeaders();
        filtered.putAll(headers.entrySet()
                               .stream()
                               .filter(e -> this.includeHeader(e.getKey()))
                               .collect(toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return filtered;
    }

    private boolean includeHeader(String header) {
        return !ignoredHeaders.contains(header.toLowerCase());
    }

    private boolean requiresBody(HttpMethod method) {
        switch (method) {
            case PUT:
            case POST:
            case PATCH:
                return true;
            default:
                return false;
        }
    }

}
