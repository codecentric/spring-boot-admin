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

package de.codecentric.boot.admin.server.domain.entities;

import de.codecentric.boot.admin.server.domain.values.InstanceId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

/**
 * Responsible for storing instances.
 *
 * @author Johannes Edmeier
 */
public interface InstanceRepository {

    /**
     * Saves the Instance
     *
     * @param app Instance to save
     * @return the saved instance
     */
    Mono<Instance> save(Instance app);

    /**
     * @return all instances in the repository;
     */
    Flux<Instance> findAll();

    /**
     * @param id the instances id
     * @return the instance with the specified id;
     */
    Mono<Instance> find(InstanceId id);

    /**
     * @param name the instances name
     * @return all instance with the specified name;
     */
    Flux<Instance> findByName(String name);


    /**
     * Updates the instance associated with the id using the remapping function.
     * If there is no associated instance the function will be called with the id and null.
     *
     * @param id                Instance to update
     * @param remappingFunction function to apply
     * @return the saved istance
     */
    Mono<Instance> compute(InstanceId id, BiFunction<InstanceId, Instance, Mono<Instance>> remappingFunction);

    /**
     * Updates the instance associated with the id using the remapping function.
     * If there is no associated instance the function will not be called.
     *
     * @param id                Instance to update
     * @param remappingFunction function to apply
     * @return the saved istance
     */
    Mono<Instance> computeIfPresent(InstanceId id, BiFunction<InstanceId, Instance, Mono<Instance>> remappingFunction);
}
