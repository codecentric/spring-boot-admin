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
package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.registry.store.ApplicationStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Notifier that just writes to a logger.
 *
 * @author Johannes Edmeier
 */
public class LoggingNotifier extends AbstractStatusChangeNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingNotifier.class);

    public LoggingNotifier(ApplicationStore store) {
        super(store);
    }

    @Override
    protected void doNotify(ClientApplicationEvent event, Application application) throws Exception {
        if (event instanceof ClientApplicationStatusChangedEvent) {
            LOGGER.info("Application {} ({}) is {}", application.getRegistration().getName(), event.getApplication(),
                    ((ClientApplicationStatusChangedEvent) event).getStatusInfo().getStatus());
        } else {
            LOGGER.info("Application {} ({}) {}", application.getRegistration().getName(), event.getApplication(),
                    event.getType());
        }
    }

}
