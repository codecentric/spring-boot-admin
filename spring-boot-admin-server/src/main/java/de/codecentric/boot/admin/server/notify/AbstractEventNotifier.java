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
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.registry.store.ApplicationStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Notifier which allows disabling and filtering of events.
 *
 * @author Johannes Edmeier
 */
public abstract class AbstractEventNotifier implements Notifier {
    private final ApplicationStore store;
    /**
     * Enables the notification.
     */
    private boolean enabled = true;

    protected AbstractEventNotifier(ApplicationStore store) {
        this.store = store;
    }

    @Override
    public void notify(ClientApplicationEvent event) {
        Application application = store.find(event.getApplication());
        if (enabled && shouldNotify(event, application)) {
            try {
                doNotify(event, application);
            } catch (Exception ex) {
                getLogger().error("Couldn't notify for event {} ", event, ex);
            }
        }
    }

    protected boolean shouldNotify(ClientApplicationEvent event, Application application) {
        return true;
    }

    protected abstract void doNotify(ClientApplicationEvent event, Application application) throws Exception;

    private Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
