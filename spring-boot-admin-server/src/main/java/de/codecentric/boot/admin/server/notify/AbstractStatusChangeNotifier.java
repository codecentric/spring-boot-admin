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

import de.codecentric.boot.admin.server.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.registry.store.ApplicationStore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Notifier for status change which allows filtering of certain status changes.
 *
 * @author Johannes Edmeier
 */
public abstract class AbstractStatusChangeNotifier extends AbstractEventNotifier {
    private final Map<ApplicationId, String> lastStatuses = new HashMap<>();
    /**
     * List of changes to ignore. Must be in Format OLD:NEW, for any status use * as wildcard, e.g.
     * *:UP or OFFLINE:*
     */
    private String[] ignoreChanges = {"UNKNOWN:UP"};

    public AbstractStatusChangeNotifier(ApplicationStore store) {
        super(store);
    }

    @Override
    public void notify(ClientApplicationEvent event) {
        super.notify(event);
        updateLastStatus(event);
    }

    @Override
    protected boolean shouldNotify(ClientApplicationEvent event, Application application) {
        if (event instanceof ClientApplicationStatusChangedEvent) {
            ClientApplicationStatusChangedEvent statusChange = (ClientApplicationStatusChangedEvent) event;
            String from = getLastStatus(event.getApplication());
            String to = statusChange.getStatusInfo().getStatus();
            return Arrays.binarySearch(ignoreChanges, from + ":" + to) < 0 &&
                   Arrays.binarySearch(ignoreChanges, "*:" + to) < 0 &&
                   Arrays.binarySearch(ignoreChanges, from + ":*") < 0;
        }
        return false;
    }

    protected final String getLastStatus(ApplicationId applicationId) {
        return lastStatuses.getOrDefault(applicationId, "UNKNOWN");
    }

    protected void updateLastStatus(ClientApplicationEvent event) {
        if (event instanceof ClientApplicationDeregisteredEvent) {
            lastStatuses.remove(event.getApplication());
        }
        if (event instanceof ClientApplicationStatusChangedEvent) {
            lastStatuses.put(event.getApplication(),
                    ((ClientApplicationStatusChangedEvent) event).getStatusInfo().getStatus());
        }
    }

    public void setIgnoreChanges(String[] ignoreChanges) {
        String[] copy = Arrays.copyOf(ignoreChanges, ignoreChanges.length);
        Arrays.sort(copy);
        this.ignoreChanges = copy;
    }

    public String[] getIgnoreChanges() {
        return ignoreChanges;
    }
}
