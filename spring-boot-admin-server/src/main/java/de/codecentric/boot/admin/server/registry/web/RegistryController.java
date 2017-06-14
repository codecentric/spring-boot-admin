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
package de.codecentric.boot.admin.server.registry.web;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.eventstore.ClientApplicationEventStore;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.registry.ApplicationRegistry;
import de.codecentric.boot.admin.server.web.AdminController;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST controller for controlling registration of managed applications.
 */
@AdminController
@ResponseBody
@RequestMapping("/api/applications")
public class RegistryController {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryController.class);
    private final ApplicationRegistry registry;
    private final ClientApplicationEventStore eventStore;

    public RegistryController(ApplicationRegistry registry, ClientApplicationEventStore eventStore) {
        this.registry = registry;
        this.eventStore = eventStore;
    }

    /**
     * Register an application within this admin application.
     *
     * @param registration registration info
     * @return The registered application.
     */
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Application> register(@RequestBody Registration registration, UriComponentsBuilder builder) {
        Registration withSource = Registration.copyOf(registration).source("http-api").build();
        LOGGER.debug("Register application {}", withSource);
        Application registeredApp = registry.register(withSource);
        URI location = builder.path("/api/applications/{id}").buildAndExpand(registeredApp.getId()).toUri();
        return ResponseEntity.created(location).body(registeredApp);
    }

    /**
     * List all registered applications with name
     *
     * @param name the name to search for
     * @return List
     */
    @RequestMapping(method = RequestMethod.GET)
    public Collection<Application> applications(@RequestParam(value = "name", required = false) String name) {
        LOGGER.debug("Deliver registered applications with name={}", name);
        if (name == null || name.isEmpty()) {
            return registry.getApplications();
        } else {
            return registry.getApplicationsByName(name);
        }
    }

    /**
     * Get a single application out of the registry.
     *
     * @param id The application identifier.
     * @return The registered application.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> get(@PathVariable String id) {
        LOGGER.debug("Deliver registered application with ID '{}'", id);
        Application application = registry.getApplication(ApplicationId.of(id));
        if (application != null) {
            return ResponseEntity.ok(application);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Unregister an application within this admin application.
     *
     * @param id The application id.
     * @return the unregistered application.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> unregister(@PathVariable String id) {
        LOGGER.debug("Unregister application with ID '{}'", id);
        Application application = registry.deregister(ApplicationId.of(id));
        if (application != null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(value = "/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<ClientApplicationEvent> events() {
        return eventStore.findAll();
    }

    @GetMapping(value = "/events", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<? extends ClientApplicationEvent> eventStream() {
        return Flux.from(eventStore);
    }

}
