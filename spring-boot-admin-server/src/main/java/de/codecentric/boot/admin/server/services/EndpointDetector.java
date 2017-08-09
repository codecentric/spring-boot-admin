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
    private final ApplicationRepository repository;
    private final EndpointDetectionStrategy strategy;

    public EndpointDetector(ApplicationRepository repository, EndpointDetectionStrategy strategy) {
        this.repository = repository;
        this.strategy = strategy;
    }

    public Mono<Void> detectEndpoints(ApplicationId id) {
        return repository.computeIfPresent(id, (key, application) -> this.doDetectEndpoints(application));
    }

    private Mono<Application> doDetectEndpoints(Application application) {
        if (!StringUtils.hasText(application.getRegistration().getManagementUrl()) ||
            application.getStatusInfo().isOffline() ||
            application.getStatusInfo().isUnknown()) {
            return Mono.empty();
        }
        log.debug("Detect endpoints for {}", application);
        return strategy.detectEndpoints(application).map(application::withEndpoints);
    }
}
