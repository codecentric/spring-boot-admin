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

import de.codecentric.boot.admin.server.event.ClientApplicationInfoChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.Info;
import de.codecentric.boot.admin.server.registry.store.ApplicationStore;
import de.codecentric.boot.admin.server.web.client.ApplicationOperations;

import java.io.Serializable;
import java.util.Map;
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
public class InfoUpdater implements ApplicationEventPublisherAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfoUpdater.class);

    private final ApplicationStore store;
    private final ApplicationOperations applicationOps;
    private ApplicationEventPublisher publisher;

    public InfoUpdater(ApplicationStore store, ApplicationOperations applicationOps) {
        this.store = store;
        this.applicationOps = applicationOps;
    }

    public void updateInfo(Application application) {
        if (application.getStatusInfo().isOffline() || application.getStatusInfo().isUnknown()) {
            return;
        }

        Info info = queryInfo(application);
        if (!info.equals(application.getInfo())) {
            Application newState = Application.copyOf(application).info(info).build();
            store.save(newState);
            publisher.publishEvent(new ClientApplicationInfoChangedEvent(newState, info));
        }
    }

    protected Info queryInfo(Application application) {
        try {
            ResponseEntity<Map<String, Serializable>> response = applicationOps.getInfo(application);
            if (response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return convertInfo(response);
            } else {
                LOGGER.info("Couldn't retrieve info for {}: {} - {}", application, response.getStatusCode(),
                        response.getBody());
                return Info.empty();
            }
        } catch (Exception ex) {
            LOGGER.warn("Couldn't retrieve info for {}", application, ex);
            return convertInfo(ex);
        }
    }

    protected Info convertInfo(ResponseEntity<Map<String, Serializable>> response) {
        return Info.from(response.getBody());
    }

    protected Info convertInfo(Exception ex) {
        return Info.empty();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }
}
