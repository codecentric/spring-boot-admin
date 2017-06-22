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
package de.codecentric.boot.admin.server.services;

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.ApplicationRepository;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.OptimisticLockingException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Registry for all applications that should be managed/administrated by the Spring Boot Admin
 * application. Backed by an ApplicationRepository for persistence and an ApplicationIdGenerator for id
 * generation.
 */
public class ApplicationRegistry {
    private static final Logger log = LoggerFactory.getLogger(ApplicationRegistry.class);
    private final ApplicationRepository repository;
    private final ApplicationIdGenerator generator;

    public ApplicationRegistry(ApplicationRepository repository, ApplicationIdGenerator generator) {
        this.repository = repository;
        this.generator = generator;
    }

    /**
     * Register application.
     *
     * @param registration application to be registered.
     * @return the if of the registered application.
     */
    public Mono<ApplicationId> register(Registration registration) {
        Assert.notNull(registration, "'registration' must not be null");
        ApplicationId id = generator.generateId(registration);
        Assert.notNull(id, "'id' must not be null");

        return getApplication(id).defaultIfEmpty(Application.create(id))
                                 .map(application -> application.register(registration))
                                 .flatMap(repository::save)
                                 .retryWhen(Retry.anyOf(OptimisticLockingException.class)
                                                 .fixedBackoff(Duration.ofMillis(50L))
                                                 .retryMax(10)
                                                 .doOnRetry(
                                                         ctx -> log.debug("Retrying after OptimisticLockingException",
                                                                 ctx.exception())))
                                 .then(Mono.just(id));
    }

    /**
     * Get a list of all registered applications.
     *
     * @return List of all applications.
     */
    public Flux<Application> getApplications() {
        return repository.findAll();
    }

    /**
     * Get a list of all registered applications.
     *
     * @param name the name to search for.
     * @return List of applications with the given name.
     */
    public Flux<Application> getApplicationsByName(String name) {
        return repository.findByName(name);
    }

    /**
     * Get a specific application inside the services.
     *
     * @param id Id.
     * @return Application.
     */
    public Mono<Application> getApplication(ApplicationId id) {
        return repository.find(id);
    }

    /**
     * Remove a specific application from services
     *
     * @param id the applications id to unregister
     * @return the id of the unregistered application
     */
    public Mono<ApplicationId> deregister(ApplicationId id) {
        return repository.find(id)
                         .map(Application::deregister)
                         .flatMap(repository::save)
                         .retryWhen(Retry.anyOf(OptimisticLockingException.class)
                                         .fixedBackoff(Duration.ofMillis(50L))
                                         .retryMax(10)
                                         .doOnRetry(ctx -> log.debug("Retrying after OptimisticLockingException",
                                                 ctx.exception())))
                         .then(Mono.just(id));
    }
}
