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
package de.codecentric.boot.admin.server.registry;

import de.codecentric.boot.admin.server.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.registry.store.ApplicationStore;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.Assert;

/**
 * Registry for all applications that should be managed/administrated by the Spring Boot Admin
 * application. Backed by an ApplicationStore for persistence and an ApplicationIdGenerator for id
 * generation.
 */
public class ApplicationRegistry implements ApplicationEventPublisherAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRegistry.class);

    private final ApplicationStore store;
    private final ApplicationIdGenerator generator;
    private ApplicationEventPublisher publisher;

    public ApplicationRegistry(ApplicationStore store, ApplicationIdGenerator generator) {
        this.store = store;
        this.generator = generator;
    }

    /**
     * Register application.
     *
     * @param registration application to be registered.
     * @return the registered application.
     */
    public Application register(Registration registration) {
        Assert.notNull(registration, "Application must not be null");
        ApplicationId applicationId = generator.generateId(registration);
        Assert.notNull(applicationId, "ID must not be null");

        Application existing = getApplication(applicationId);
        if (existing != null && existing.getRegistration().equals(registration)) {
            return existing;
        }

        Application.Builder builder = existing != null ?
                Application.copyOf(existing) :
                Application.builder().id(applicationId);

        Application application = builder.registration(registration).build();
        Application replaced = store.save(application);
        if (replaced == null) {
            LOGGER.info("New Application {} registered", application);
            publisher.publishEvent(new ClientApplicationRegisteredEvent(application, registration));
        } else {
            LOGGER.debug("Application {} refreshed", application);
            publisher.publishEvent(new ClientApplicationRegistrationUpdatedEvent(application, registration));
        }
        return application;
    }

    /**
     * Get a list of all registered applications.
     *
     * @return List of all applications.
     */
    public Collection<Application> getApplications() {
        return store.findAll();
    }

    /**
     * Get a list of all registered applications.
     *
     * @param name the name to search for.
     * @return List of applications with the given name.
     */
    public Collection<Application> getApplicationsByName(String name) {
        return store.findByName(name);
    }

    /**
     * Get a specific application inside the registry.
     *
     * @param id Id.
     * @return Application.
     */
    public Application getApplication(ApplicationId id) {
        return store.find(id);
    }

    /**
     * Remove a specific application from registry
     *
     * @param id the applications id to unregister
     * @return the unregistered Application
     */
    public Application deregister(ApplicationId id) {
        Application app = store.delete(id);
        if (app != null) {
            LOGGER.info("Application {} unregistered ", app);
            publisher.publishEvent(new ClientApplicationDeregisteredEvent(app));
        }
        return app;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        publisher = applicationEventPublisher;
    }
}
