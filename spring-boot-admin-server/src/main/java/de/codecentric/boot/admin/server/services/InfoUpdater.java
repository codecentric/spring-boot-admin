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

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.web.client.InstanceOperations;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

/**
 * The StatusUpdater is responsible for updating the status of all or a single application querying
 * the healthUrl.
 *
 * @author Johannes Edmeier
 */
public class InfoUpdater {
    private static final Logger log = LoggerFactory.getLogger(InfoUpdater.class);
    private final InstanceRepository repository;
    private final InstanceOperations instanceOps;

    public InfoUpdater(InstanceRepository repository, InstanceOperations instanceOps) {
        this.repository = repository;
        this.instanceOps = instanceOps;
    }

    public Mono<Void> updateInfo(InstanceId id) {
        return repository.computeIfPresent(id, (key, instance) -> this.doUpdateInfo(instance));


    }

    private Mono<Instance> doUpdateInfo(Instance instance) {
        if (instance.getStatusInfo().isOffline() || instance.getStatusInfo().isUnknown()) {
            return Mono.empty();
        }
        if (!instance.getEndpoints().isPresent(Endpoint.INFO)) {
            return Mono.empty();
        }

        log.debug("Update info for {}", instance);
        return instanceOps.getInfo(instance)
                          .log(log.getName(), Level.FINEST)
                          .map(response -> convertInfo(instance, response))
                          .onErrorResume(ex -> Mono.just(convertInfo(instance, ex)))
                          .map(instance::withInfo);
    }

    protected Info convertInfo(Instance instance, ResponseEntity<Map<String, Object>> response) {
        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            return Info.from(response.getBody());
        } else {
            log.info("Couldn't retrieve info for {}: {} - {}", instance, response.getStatusCode(), response.getBody());
            return Info.empty();
        }
    }

    protected Info convertInfo(Instance instance, Throwable ex) {
        log.warn("Couldn't retrieve info for {}", instance, ex);
        return Info.empty();
    }
}
