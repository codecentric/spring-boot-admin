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
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.eventstore.OptimisticLockingException;
import de.codecentric.boot.admin.server.web.client.ApplicationOperations;
import reactor.core.publisher.Mono;
import reactor.retry.Retry;

import java.io.Serializable;
import java.time.Duration;
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
    private final ApplicationRepository repository;
    private final ApplicationOperations applicationOps;

    public InfoUpdater(ApplicationRepository repository, ApplicationOperations applicationOps) {
        this.repository = repository;
        this.applicationOps = applicationOps;
    }

    public Mono<Void> updateInfo(ApplicationId id) {
        return repository.find(id)
                         .flatMap(this::doUpdateInfo)
                         .flatMap(repository::save)
                         .retryWhen(Retry.anyOf(OptimisticLockingException.class)
                                         .fixedBackoff(Duration.ofMillis(50L))
                                         .retryMax(10)
                                         .doOnRetry(ctx -> log.debug("Retrying after OptimisticLockingException",
                                                 ctx.exception())));
    }

    private Mono<Application> doUpdateInfo(Application application) {
        if (application.getStatusInfo().isOffline() || application.getStatusInfo().isUnknown()) {
            return Mono.empty();
        }

        log.debug("Update info for {}", application);
        return applicationOps.getInfo(application)
                             .log(log.getName(), Level.FINEST)
                             .map(response -> convertInfo(application, response))
                             .onErrorResume(ex -> Mono.just(convertInfo(application, ex)))
                             .map(application::withInfo);
    }

    protected Info convertInfo(Application application, ResponseEntity<Map<String, Serializable>> response) {
        if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
            return Info.from(response.getBody());
        } else {
            log.info("Couldn't retrieve info for {}: {} - {}", application, response.getStatusCode(),
                    response.getBody());
            return Info.empty();
        }
    }

    protected Info convertInfo(Application application, Throwable ex) {
        log.warn("Couldn't retrieve info for {}", application, ex);
        return Info.empty();
    }
}
