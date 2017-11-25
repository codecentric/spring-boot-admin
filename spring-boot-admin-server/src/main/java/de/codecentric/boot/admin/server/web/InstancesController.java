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
package de.codecentric.boot.admin.server.web;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import de.codecentric.boot.admin.server.web.client.InstanceWebClientException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import static org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;

/**
 * REST controller for controlling registration of managed instances.
 */
@AdminController
@ResponseBody
public class InstancesController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstancesController.class);
    private static final ServerSentEvent<?> PING = ServerSentEvent.builder().comment("ping").build();
    private static final Flux<ServerSentEvent<?>> PING_FLUX = Flux.interval(Duration.ZERO, Duration.ofSeconds(10L))
                                                                  .map(tick -> (ServerSentEvent<?>) PING);
    private final InstanceRegistry registry;
    private final InstanceEventStore eventStore;
    private final InstanceWebClient instanceWebClient;

    public InstancesController(InstanceRegistry registry,
                               InstanceEventStore eventStore,
                               InstanceWebClient instanceWebClient) {
        this.registry = registry;
        this.eventStore = eventStore;
        this.instanceWebClient = instanceWebClient;
    }

    /**
     * Register an instance.
     *
     * @param registration registration info
     * @return The registered instance id;
     */
    @PostMapping(path = "/instances", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, InstanceId>>> register(@RequestBody Registration registration,
                                                                  UriComponentsBuilder builder) {
        Registration withSource = Registration.copyOf(registration).source("http-api").build();
        LOGGER.debug("Register instance {}", withSource);
        return registry.register(withSource).map(id -> {
            URI location = builder.path("/{id}").buildAndExpand(id).toUri();
            return ResponseEntity.created(location).body(Collections.singletonMap("id", id));
        });
    }

    /**
     * List all registered instances with name
     *
     * @param name the name to search for
     * @return application list
     */
    @GetMapping(path = "/instances", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Instance> instances(@RequestParam(value = "name", required = false) String name) {
        LOGGER.debug("Deliver registered instances with name={}", name);
        if (name == null || name.isEmpty()) {
            return registry.getInstances().filter(Instance::isRegistered);
        } else {
            return registry.getInstances(name).filter(Instance::isRegistered);
        }
    }

    /**
     * Get a single instance.
     *
     * @param id The application identifier.
     * @return The registered application.
     */
    @GetMapping(path = "/instances/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Instance>> instance(@PathVariable String id) {
        LOGGER.debug("Deliver registered instance with ID '{}'", id);
        return registry.getInstance(InstanceId.of(id))
                       .filter(Instance::isRegistered)
                       .map(ResponseEntity::ok)
                       .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Unregister an instance
     *
     * @param id The instance id.
     * @return response indicating the success
     */
    @DeleteMapping(path = "/instances/{id}")
    public Mono<ResponseEntity<Void>> unregister(@PathVariable String id) {
        LOGGER.debug("Unregister instance with ID '{}'", id);
        return registry.deregister(InstanceId.of(id))
                       .map(v -> ResponseEntity.noContent().<Void>build())
                       .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping(path = "/instances/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<InstanceEvent> events() {
        return eventStore.findAll();
    }

    @GetMapping(path = "/instances/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<InstanceEvent>> eventStream() {
        return Flux.from(eventStore).map(event -> ServerSentEvent.builder(event).build()).mergeWith(ping());
    }

    @GetMapping(path = "/instances/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Instance>> instanceStream(@PathVariable String id) {
        return Flux.from(eventStore)
                   .filter(event -> event.getInstance().equals(InstanceId.of(id)))
                   .flatMap(event -> registry.getInstance(event.getInstance()))
                   .map(event -> ServerSentEvent.builder(event).build())
                   .mergeWith(ping());
    }

    private static final String[] HOP_BY_HOP_HEADERS = new String[]{"Connection", "Keep-Alive", "Proxy-Authenticate",
            "Proxy-Authorization", "TE", "Trailer", "Transfer-Encoding", "Upgrade"};

    @RequestMapping(path = "/instances/{instanceId}/**")
    public Mono<Void> endpointProxy(@PathVariable("instanceId") String instanceId, ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String endpointLocalPath = request.getPath().pathWithinApplication().subPath(4).value();

        URI uri = UriComponentsBuilder.fromPath(endpointLocalPath)
                                      .queryParams(request.getQueryParams())
                                      .build()
                                      .toUri();

        RequestBodySpec bodySpec = instanceWebClient.instance(registry.getInstance(InstanceId.of(instanceId)))
                                                    .method(request.getMethod())
                                                    .uri(uri)
                                                    .headers(headers -> {
                                                        headers.addAll(request.getHeaders());
                                                        headers.remove(HttpHeaders.HOST);
                                                        Arrays.stream(HOP_BY_HOP_HEADERS).forEach(headers::remove);
                                                    });

        RequestHeadersSpec<?> headersSpec = bodySpec;
        if (requiresBody(request.getMethod())) {
            headersSpec = bodySpec.body(BodyInserters.fromDataBuffers(request.getBody()));
        }

        return headersSpec.exchange()
                          .flatMap(clientResponse -> {
                              ServerHttpResponse response = exchange.getResponse();
                              response.getHeaders().putAll(clientResponse.headers().asHttpHeaders());
                              response.setStatusCode(clientResponse.statusCode());
                              return response.writeWith(clientResponse.body(BodyExtractors.toDataBuffers()));
                          })
                          .onErrorMap(InstanceWebClientException.class,
                                  error -> new ResponseStatusException(HttpStatus.BAD_REQUEST, null, error))
                          .onErrorMap(ConnectException.class,
                                  error -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, null, error));
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

    @SuppressWarnings("unchecked")
    private static <T> Flux<ServerSentEvent<T>> ping() {
        return (Flux<ServerSentEvent<T>>) (Flux) PING_FLUX;
    }

}
