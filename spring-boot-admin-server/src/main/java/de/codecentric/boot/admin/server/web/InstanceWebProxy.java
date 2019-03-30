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

package de.codecentric.boot.admin.server.web;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import de.codecentric.boot.admin.server.web.client.exception.ResolveEndpointException;
import io.netty.handler.timeout.ReadTimeoutException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Forwards a request to a single instances endpoint and will respond with:
 * - 502 (Bad Gateway) when any error occurs during the request
 * - 503 (Service unavailable) when the instance is not found
 * - 504 (Gateway timeout) when the request exceeds the timeout
 *
 * @author Johannes Edmeier
 */
public class InstanceWebProxy {
    private static final Logger log = LoggerFactory.getLogger(InstanceWebProxy.class);
    private final InstanceRegistry registry;
    private final InstanceWebClient instanceWebClient;
    private final ExchangeStrategies strategies = ExchangeStrategies.withDefaults();

    public InstanceWebProxy(InstanceRegistry registry, InstanceWebClient instanceWebClient) {
        this.registry = registry;
        this.instanceWebClient = instanceWebClient;
    }

    public Mono<ClientResponse> forward(String instanceId,
                                        URI uri,
                                        HttpMethod method,
                                        HttpHeaders headers,
                                        Supplier<BodyInserter<?, ? super ClientHttpRequest>> bodyInserter) {
        log.trace("Proxy-Request for instance {} with URL '{}'", instanceId, uri);

        return this.registry.getInstance(InstanceId.of(instanceId))
                            .flatMap(instance -> this.forward(instance, uri, method, headers, bodyInserter))
                            .switchIfEmpty(Mono.fromSupplier(() -> ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE,
                                this.strategies
                            ).build()));
    }

    private Mono<ClientResponse> forward(Instance instance,
                                         URI uri,
                                         HttpMethod method,
                                         HttpHeaders headers,
                                         Supplier<BodyInserter<?, ? super ClientHttpRequest>> bodyInserter) {
        WebClient.RequestBodySpec bodySpec = this.instanceWebClient.instance(instance)
                                                                   .method(method)
                                                                   .uri(uri)
                                                                   .headers(h -> h.addAll(headers));

        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec;
        if (requiresBody(method)) {
            try {
                headersSpec = bodySpec.body(bodyInserter.get());
            } catch (Exception ex) {
                return Mono.error(ex);
            }
        }

        return headersSpec.exchange()
                          .onErrorResume(ex -> ex instanceof ReadTimeoutException || ex instanceof TimeoutException,
                              ex -> Mono.fromSupplier(() -> {
                                  log.trace("Timeout for Proxy-Request for instance {} with URL '{}'",
                                      instance.getId(),
                                      uri
                                  );
                                  return ClientResponse.create(HttpStatus.GATEWAY_TIMEOUT, this.strategies).build();
                              })
                          )
                          .onErrorResume(ResolveEndpointException.class, ex -> Mono.fromSupplier(() -> {
                              log.trace("No Endpoint found for Proxy-Request for instance {} with URL '{}'",
                                  instance.getId(),
                                  uri
                              );
                              return ClientResponse.create(HttpStatus.NOT_FOUND, this.strategies).build();
                          }))
                          .onErrorResume(IOException.class, ex -> Mono.fromSupplier(() -> {
                              log.trace("Proxy-Request for instance {} with URL '{}' errored",
                                  instance.getId(),
                                  uri,
                                  ex
                              );
                              return ClientResponse.create(HttpStatus.BAD_GATEWAY, this.strategies).build();
                          }));
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
