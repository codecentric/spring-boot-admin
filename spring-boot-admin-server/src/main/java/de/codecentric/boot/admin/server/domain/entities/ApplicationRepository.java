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
package de.codecentric.boot.admin.server.domain.entities;

import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

/**
 * Responsible for storing applications.
 *
 * @author Johannes Edmeier
 */
public interface ApplicationRepository {

    /**
     * Saves the Application
     *
     * @param app Application to save
     */
    Mono<Void> save(Application app);

    /**
     * @return all Applications in the repository;
     */
    Flux<Application> findAll();

    /**
     * @param id the applications id
     * @return the Application with the specified id;
     */
    Mono<Application> find(ApplicationId id);

    /**
     * @param name the applications name
     * @return all Applications with the specified name;
     */
    Flux<Application> findByName(String name);


    /**
     * Updates the application associated with the id using the remapping function.
     * If there is no associated application the function will be called with the id and null.
     *
     * @param id                Application to update
     * @param remappingFunction function to apply
     */
    Mono<Void> compute(ApplicationId id, BiFunction<ApplicationId, Application, Mono<Application>> remappingFunction);

    /**
     * Updates the application associated with the id using the remapping function.
     * If there is no associated application the function will not be called.
     *
     * @param id                Application to update
     * @param remappingFunction function to apply
     */
    Mono<Void> computeIfPresent(ApplicationId id,
                                BiFunction<ApplicationId, Application, Mono<Application>> remappingFunction);
}