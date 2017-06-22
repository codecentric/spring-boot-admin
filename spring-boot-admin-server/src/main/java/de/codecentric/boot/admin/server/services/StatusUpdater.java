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
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.OptimisticLockingException;
import de.codecentric.boot.admin.server.web.client.ApplicationOperations;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import java.io.Serializable;
import java.time.Duration;
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
    private final ApplicationRepository repository;
    private final ApplicationOperations applicationOps;

    public StatusUpdater(ApplicationRepository repository, ApplicationOperations applicationOps) {
        this.repository = repository;
        this.applicationOps = applicationOps;
    }

    public Mono<Void> updateStatus(ApplicationId id) {
        return repository.find(id)
                         .filter(Application::isRegistered)
                         .flatMap(this::doUpdateStatus)
                         .flatMap(repository::save)
                         .retryWhen(Retry.anyOf(OptimisticLockingException.class)
                                         .fixedBackoff(Duration.ofMillis(50L))
                                         .retryMax(10)
                                         .doOnRetry(ctx -> log.debug("Retrying after OptimisticLockingException",
                                                 ctx.exception())));
    }

    protected Mono<Application> doUpdateStatus(Application application) {
        log.debug("Update status for {}", application);
        return applicationOps.getHealth(application)
                             .log(log.getName(), Level.FINEST)
                             .map(this::convertStatusInfo)
                             .doOnError(ex -> logError(application, ex))
                             .onErrorResume(ex -> Mono.just(convertStatusInfo(ex)))
                             .map(application::withStatusInfo);
    }

    protected StatusInfo convertStatusInfo(ResponseEntity<Map<String, Serializable>> response) {
        if (response.hasBody() && response.getBody().get("status") instanceof String) {
            return StatusInfo.valueOf((String) response.getBody().get("status"), response.getBody());
        }
        if (response.getStatusCode().is2xxSuccessful()) {
            return StatusInfo.ofUp();
        }
        Map<String, Serializable> details = new HashMap<>();
        details.put("status", response.getStatusCodeValue());
        details.put("error", response.getStatusCode().getReasonPhrase());
        if (response.hasBody()) {
            details.putAll(response.getBody());
        }
        return StatusInfo.ofDown(details);
    }

    protected void logError(Application application, Throwable ex) {
        if ("OFFLINE".equals(application.getStatusInfo().getStatus())) {
            log.debug("Couldn't retrieve status for {}", application, ex);
        } else {
            log.info("Couldn't retrieve status for {}", application, ex);
        }
    }

    protected StatusInfo convertStatusInfo(Throwable ex) {
        Map<String, Serializable> details = new HashMap<>();
        details.put("message", ex.getMessage());
        details.put("exception", ex.getClass().getName());
        return StatusInfo.ofOffline(details);
    }

}
