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
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;

import static de.codecentric.boot.admin.server.utils.MediaType.ACTUATOR_V2_MEDIATYPE;

/**
 * The StatusUpdater is responsible for updating the status of all or a single application querying
 * the healthUrl.
 *
 * @author Johannes Edmeier
 */
public class InfoUpdater {
    private static final Logger log = LoggerFactory.getLogger(InfoUpdater.class);
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
    };
    private final InstanceRepository repository;
    private final InstanceWebClient instanceWebClient;

    public InfoUpdater(InstanceRepository repository, InstanceWebClient instanceWebClient) {
        this.repository = repository;
        this.instanceWebClient = instanceWebClient;
    }

    public Mono<Void> updateInfo(InstanceId id) {
        return repository.computeIfPresent(id, (key, instance) -> this.doUpdateInfo(instance)).then();
    }

    protected Mono<Instance> doUpdateInfo(Instance instance) {
        if (instance.getStatusInfo().isOffline() || instance.getStatusInfo().isUnknown()) {
            return Mono.empty();
        }
        if (!instance.getEndpoints().isPresent(Endpoint.INFO)) {
            return Mono.empty();
        }

        log.debug("Update info for {}", instance);
        return instanceWebClient.instance(instance)
                                .get()
                                .uri(Endpoint.INFO)
                                .exchange()
                                .log(log.getName(), Level.FINEST)
                                .flatMap(response -> convertInfo(instance, response))
                                .onErrorResume(ex -> Mono.just(convertInfo(instance, ex)))
                                .map(instance::withInfo);
    }

    protected Mono<Info> convertInfo(Instance instance, ClientResponse response) {
        if (response.statusCode().is2xxSuccessful() &&
            response.headers()
                    .contentType()
                    .map(mt -> mt.isCompatibleWith(MediaType.APPLICATION_JSON) ||
                               mt.isCompatibleWith(ACTUATOR_V2_MEDIATYPE))
                    .orElse(false)) {
            return response.bodyToMono(RESPONSE_TYPE).map(Info::from).defaultIfEmpty(Info.empty());
        }
        log.info("Couldn't retrieve info for {}: {}", instance, response.statusCode());
        return response.bodyToMono(Void.class).then(Mono.just(Info.empty()));
    }

    protected Info convertInfo(Instance instance, Throwable ex) {
        log.warn("Couldn't retrieve info for {}", instance, ex);
        return Info.empty();
    }
}
