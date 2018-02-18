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

package de.codecentric.boot.admin.server.services;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.util.Assert;

/**
 * Registry for all application instances that should be managed/administrated by the Spring Boot Admin
 * server. Backed by an InstanceRepository for persistence and an InstanceIdGenerator for id
 * generation.
 */
public class InstanceRegistry {
    private final InstanceRepository repository;
    private final InstanceIdGenerator generator;

    public InstanceRegistry(InstanceRepository repository, InstanceIdGenerator generator) {
        this.repository = repository;
        this.generator = generator;
    }

    /**
     * Register instance.
     *
     * @param registration instance to be registered.
     * @return the if of the registered instance.
     */
    public Mono<InstanceId> register(Registration registration) {
        Assert.notNull(registration, "'registration' must not be null");
        InstanceId id = generator.generateId(registration);
        Assert.notNull(id, "'id' must not be null");
        return repository.compute(id, (key, instance) -> {
            if (instance == null) {
                instance = Instance.create(key);
            }
            return Mono.just(instance.register(registration));
        }).map(Instance::getId);
    }

    /**
     * Get a list of all registered instances.
     *
     * @return List of all instances.
     */
    public Flux<Instance> getInstances() {
        return repository.findAll();
    }

    /**
     * Get a list of all registered application instances.
     *
     * @param name the name to search for.
     * @return List of instances for the given application
     */
    public Flux<Instance> getInstances(String name) {
        return repository.findByName(name);
    }

    /**
     * Get a specific instance
     *
     * @param id Id.
     * @return Instance.
     */
    public Mono<Instance> getInstance(InstanceId id) {
        return repository.find(id);
    }

    /**
     * Remove a specific instance from services
     *
     * @param id the instances id to unregister
     * @return the id of the unregistered instance
     */
    public Mono<InstanceId> deregister(InstanceId id) {
        return repository.computeIfPresent(id, (key, instance) -> Mono.just(instance.deregister()))
                         .map(Instance::getId);
    }
}
