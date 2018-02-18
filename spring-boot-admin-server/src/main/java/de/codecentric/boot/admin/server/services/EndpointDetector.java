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
import de.codecentric.boot.admin.server.services.endpoints.EndpointDetectionStrategy;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @author Johannes Edmeier
 */
public class EndpointDetector {
    private static final Logger log = LoggerFactory.getLogger(EndpointDetector.class);
    private final InstanceRepository repository;
    private final EndpointDetectionStrategy strategy;

    public EndpointDetector(InstanceRepository repository, EndpointDetectionStrategy strategy) {
        this.repository = repository;
        this.strategy = strategy;
    }

    public Mono<Void> detectEndpoints(InstanceId id) {
        return repository.computeIfPresent(id, (key, instance) -> this.doDetectEndpoints(instance)).then();
    }

    private Mono<Instance> doDetectEndpoints(Instance instance) {
        if (!StringUtils.hasText(instance.getRegistration().getManagementUrl()) ||
            instance.getStatusInfo().isOffline() ||
            instance.getStatusInfo().isUnknown()) {
            return Mono.empty();
        }
        log.debug("Detect endpoints for {}", instance);
        return strategy.detectEndpoints(instance).map(instance::withEndpoints);
    }
}
