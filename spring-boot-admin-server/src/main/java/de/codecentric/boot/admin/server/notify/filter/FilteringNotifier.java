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
package de.codecentric.boot.admin.server.notify.filter;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.notify.AbstractEventNotifier;
import de.codecentric.boot.admin.server.notify.Notifier;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * Notifier that allows to filter certain events based on policies.
 *
 * @author Johannes Edmeier
 */
public class FilteringNotifier extends AbstractEventNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilteringNotifier.class);
    private final ConcurrentMap<String, NotificationFilter> filters = new ConcurrentHashMap<>();
    private final Notifier delegate;
    private Instant lastCleanup = Instant.EPOCH;
    private Duration cleanupInterval = Duration.ofSeconds(10);
    private AtomicLong counter = new AtomicLong();

    public FilteringNotifier(Notifier delegate, InstanceRepository repository) {
        super(repository);
        Assert.notNull(delegate, "'delegate' must not be null!");
        this.delegate = delegate;
    }

    @Override
    protected boolean shouldNotify(InstanceEvent event, Instance instance) {
        return !filter(event, instance);
    }

    @Override
    public Mono<Void> doNotify(InstanceEvent event, Instance instance) {
        if (!filter(event, instance)) {
            return delegate.notify(event);
        } else {
            return Mono.empty();
        }
    }

    private boolean filter(InstanceEvent event, Instance instance) {
        cleanUp();
        for (Entry<String, NotificationFilter> entry : getNotificationFilters().entrySet()) {
            if (entry.getValue().filter(event, instance)) {
                LOGGER.debug("The event '{}' was suppressed by filter '{}'", event, entry);
                return true;
            }
        }
        return false;
    }

    private void cleanUp() {
        Instant now = Instant.now();
        if (lastCleanup.plus(cleanupInterval).isAfter(now)) {
            return;
        }
        lastCleanup = now;
        for (Entry<String, NotificationFilter> entry : getNotificationFilters().entrySet()) {
            if (entry.getValue() instanceof ExpiringNotificationFilter &&
                ((ExpiringNotificationFilter) entry.getValue()).isExpired()) {
                LOGGER.debug("Expired filter '{}' removed", entry);
                filters.remove(entry.getKey());
            }
        }
    }

    public String addFilter(NotificationFilter filter) {
        String id = "F" + counter.incrementAndGet();
        LOGGER.debug("Added filter '{}' with id '{}'", filter, id);
        filters.put(id, filter);
        return id;
    }

    public NotificationFilter removeFilter(String id) {
        LOGGER.debug("Removed filter with id '{}'", id);
        return filters.remove(id);
    }

    public Map<String, NotificationFilter> getNotificationFilters() {
        synchronized (filters) {
            return Collections.unmodifiableMap(new HashMap<>(filters));
        }
    }

    public void setCleanupInterval(Duration cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }
}
