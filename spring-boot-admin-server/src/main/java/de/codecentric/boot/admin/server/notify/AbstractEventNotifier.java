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

package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Notifier which allows disabling and filtering of events.
 *
 * @author Johannes Edmeier
 */
public abstract class AbstractEventNotifier implements Notifier {
    private final InstanceRepository repository;
    /**
     * Enables the notification.
     */
    private boolean enabled = true;

    protected AbstractEventNotifier(InstanceRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Void> notify(InstanceEvent event) {
        if (!enabled) {
            return Mono.empty();
        }

        return repository.find(event.getInstance())
                         .filter(instance -> shouldNotify(event, instance))
                         .flatMap(instance -> doNotify(event, instance))
                         .doOnError(ex -> getLogger().error("Couldn't notify for event {} ", event, ex))
                         .then();
    }

    protected boolean shouldNotify(InstanceEvent event, Instance instance) {
        return true;
    }

    protected abstract Mono<Void> doNotify(InstanceEvent event, Instance instance);

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
