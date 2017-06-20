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
package de.codecentric.boot.admin.server.registry;

import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.StatusInfo;
import de.codecentric.boot.admin.server.registry.store.ApplicationStore;
import de.codecentric.boot.admin.server.web.client.ApplicationOperations;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;

/**
 * The StatusUpdater is responsible for updating the status of all or a single application querying
 * the healthUrl.
 *
 * @author Johannes Edmeier
 */
public class StatusUpdater implements ApplicationEventPublisherAware {
    private static final Logger log = LoggerFactory.getLogger(StatusUpdater.class);
    private final ApplicationStore store;
    private final ApplicationOperations applicationOps;
    private ApplicationEventPublisher publisher;
    private long statusLifetime = 10_000L;
    private Map<ApplicationId, Long> lastQueried = new HashMap<>();

    public StatusUpdater(ApplicationStore store, ApplicationOperations applicationOps) {
        this.store = store;
        this.applicationOps = applicationOps;
    }

    public void updateStatusForAllApplications() {
        long now = System.currentTimeMillis();
        for (Application application : store.findAll()) {
            if (now - statusLifetime > lastQueried.getOrDefault(application.getId(), 0L)) {
                updateStatus(application);
            }
        }
    }

    public void updateStatus(ApplicationId id) {
        Application application = store.find(id);
        if (application != null) {
            updateStatus(application);
        }
    }

    private void updateStatus(Application application) {
        StatusInfo oldStatus = application.getStatusInfo();
        queryStatus(application).filter(newStatus -> !newStatus.getStatus().equals(oldStatus.getStatus()))
                                .doOnNext(newStatus -> {
                                    Application newState = Application.copyOf(application)
                                                                      .statusInfo(newStatus)
                                                                      .build();
                                    store.save(newState);
                                    publisher.publishEvent(
                                            new ClientApplicationStatusChangedEvent(application.getId(), newStatus));
                                })
                                .subscribe();
    }

    protected Mono<StatusInfo> queryStatus(Application application) {
        log.debug("Querying status for {}", application);
        lastQueried.put(application.getId(), System.currentTimeMillis());
        return applicationOps.getHealth(application)
                             .log(log.getName(), Level.FINEST)
                             .map(this::convertStatusInfo)
                             .doOnError(ex -> logError(application, ex))
                             .onErrorResume(ex -> Mono.just(convertStatusInfo(ex)));
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

    public void setStatusLifetime(long statusLifetime) {
        this.statusLifetime = statusLifetime;
    }

    public long getStatusLifetime() {
        return statusLifetime;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }
}
