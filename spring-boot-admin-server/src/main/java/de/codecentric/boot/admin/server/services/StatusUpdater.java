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
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;

import static java.util.Collections.emptyMap;

/**
 * The StatusUpdater is responsible for updating the status of all or a single application querying
 * the healthUrl.
 *
 * @author Johannes Edmeier
 */
public class StatusUpdater {
    private static final Logger log = LoggerFactory.getLogger(StatusUpdater.class);
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    private final InstanceRepository repository;
    private final InstanceWebClient instanceWebClient;

    public StatusUpdater(InstanceRepository repository, InstanceWebClient instanceWebClient) {
        this.repository = repository;
        this.instanceWebClient = instanceWebClient;
    }

    public Mono<Void> updateStatus(InstanceId id) {
        return repository.computeIfPresent(id, (key, instance) -> this.doUpdateStatus(instance));

    }

    protected Mono<Instance> doUpdateStatus(Instance instance) {
        if (!instance.isRegistered()) {
            return Mono.empty();
        }

        log.debug("Update status for {}", instance);
        return instanceWebClient.instance(instance)
                                .get()
                                .uri(Endpoint.HEALTH)
                                .exchange()
                                .log(log.getName(), Level.FINEST)
                                .flatMap(this::convertStatusInfo)
                                .doOnError(ex -> logError(instance, ex))
                                .onErrorResume(this::handleError)
                                .map(instance::withStatusInfo);
    }

    protected Mono<StatusInfo> convertStatusInfo(ClientResponse response) {
        if (response.headers().contentType().map(MediaType.APPLICATION_JSON::includes).orElse(false)) {
            return response.bodyToMono(RESPONSE_TYPE).map(body -> {
                if (body.get("status") instanceof String) {
                    return StatusInfo.from(body);
                }
                return getStatusInfoFromStatus(response, body);
            });
        }
        return Mono.just(this.getStatusInfoFromStatus(response, emptyMap()));
    }

    protected StatusInfo getStatusInfoFromStatus(ClientResponse response, Map<String, ?> body) {
        if (response.statusCode().is2xxSuccessful()) {
            return StatusInfo.ofUp();
        }
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("status", response.statusCode().value());
        details.put("error", response.statusCode().getReasonPhrase());
        details.putAll(body);
        return StatusInfo.ofDown(details);
    }

    protected Mono<StatusInfo> handleError(Throwable ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("message", ex.getMessage());
        details.put("exception", ex.getClass().getName());
        return Mono.just(StatusInfo.ofOffline(details));
    }

    protected void logError(Instance instance, Throwable ex) {
        if (instance.getStatusInfo().isOffline()) {
            log.debug("Couldn't retrieve status for {}", instance, ex);
        } else {
            log.info("Couldn't retrieve status for {}", instance, ex);
        }
    }
}
