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
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.web.client.InstanceOperations;
import reactor.core.publisher.Mono;

import java.util.HashMap;
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
public class StatusUpdater {
    private static final Logger log = LoggerFactory.getLogger(StatusUpdater.class);
    private final InstanceRepository repository;
    private final InstanceOperations instanceOps;

    public StatusUpdater(InstanceRepository repository, InstanceOperations instanceOps) {
        this.repository = repository;
        this.instanceOps = instanceOps;
    }

    public Mono<Void> updateStatus(InstanceId id) {
        return repository.computeIfPresent(id, (key, instance) -> this.doUpdateStatus(instance));

    }

    protected Mono<Instance> doUpdateStatus(Instance instance) {
        if (!instance.isRegistered()) {
            return Mono.empty();
        }

        log.debug("Update status for {}", instance);
        return instanceOps.getHealth(instance)
                          .log(log.getName(), Level.FINEST)
                          .map(this::convertStatusInfo)
                          .doOnError(ex -> logError(instance, ex))
                          .onErrorResume(ex -> Mono.just(convertStatusInfo(ex)))
                          .map(instance::withStatusInfo);
    }

    @SuppressWarnings("unchecked")
    protected StatusInfo convertStatusInfo(ResponseEntity<Map<String, Object>> response) {
        if (response.hasBody() && response.getBody().get("status") instanceof String) {
            Map<String, Object> body = response.getBody();
            String status = (String) body.get("status");
            Map<String, Object> details = body;
            if (body.get("details") instanceof Map) {
                details = (Map<String, Object>) body.get("details");
            }
            return StatusInfo.valueOf(status, details);
        }

        if (response.getStatusCode().is2xxSuccessful()) {
            return StatusInfo.ofUp();
        }

        Map<String, Object> details = new HashMap<>();
        details.put("status", response.getStatusCodeValue());
        details.put("error", response.getStatusCode().getReasonPhrase());
        if (response.hasBody()) {
            details.putAll(response.getBody());
        }
        return StatusInfo.ofDown(details);
    }

    protected void logError(Instance instance, Throwable ex) {
        if ("OFFLINE".equals(instance.getStatusInfo().getStatus())) {
            log.debug("Couldn't retrieve status for {}", instance, ex);
        } else {
            log.info("Couldn't retrieve status for {}", instance, ex);
        }
    }

    protected StatusInfo convertStatusInfo(Throwable ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("message", ex.getMessage());
        details.put("exception", ex.getClass().getName());
        return StatusInfo.ofOffline(details);
    }

}
