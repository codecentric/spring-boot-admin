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

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST controller for controlling registration of managed instances.
 */
@AdminController
@ResponseBody
public class InstancesController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstancesController.class);
    private static final ServerSentEvent<?> PING = ServerSentEvent.builder().comment("ping").build();
    private static final Flux<ServerSentEvent<?>> PING_FLUX = Flux.interval(Duration.ZERO, Duration.ofSeconds(10L))
                                                                  .map(tick -> PING);
    private final InstanceRegistry registry;
    private final InstanceEventStore eventStore;

    public InstancesController(InstanceRegistry registry, InstanceEventStore eventStore) {
        this.registry = registry;
        this.eventStore = eventStore;
    }

    /**
     * Register an instance.
     *
     * @param registration registration info
     * @param builder      UriComponentsBuilder
     * @return The registered instance id;
     */
    @PostMapping(path = "/instances", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Map<String, InstanceId>>> register(@RequestBody Registration registration,
                                                                  UriComponentsBuilder builder) {
        Registration withSource = Registration.copyOf(registration).source("http-api").build();
        LOGGER.debug("Register instance {}", withSource);
        return registry.register(withSource).map(id -> {
            URI location = builder.replacePath("/instances/{id}").buildAndExpand(id).toUri();
            return ResponseEntity.created(location).body(Collections.singletonMap("id", id));
        });
    }

    /**
     * List all registered instances with name
     *
     * @param name the name to search for
     * @return application list
     */
    @GetMapping(path = "/instances", produces = MediaType.APPLICATION_JSON_VALUE, params = "name")
    public Flux<Instance> instances(@RequestParam("name") String name) {
        return registry.getInstances(name).filter(Instance::isRegistered);
    }

    /**
     * List all registered instances with name
     *
     * @return application list
     */
    @GetMapping(path = "/instances", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Instance> instances() {
        LOGGER.debug("Deliver all registered instances");
        return registry.getInstances().filter(Instance::isRegistered);
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

    @SuppressWarnings("unchecked")
    private static <T> Flux<ServerSentEvent<T>> ping() {
        return (Flux<ServerSentEvent<T>>) (Flux) PING_FLUX;
    }

}
