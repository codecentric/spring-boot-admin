/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.notify.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.notify.Notifier;

/**
 * Notifier that allows to filter certain events based on policies.
 *
 * @author Johannes Edmeier
 */
public class FilteringNotifier implements Notifier {
	private static final Logger LOGGER = LoggerFactory.getLogger(FilteringNotifier.class);
	private final ConcurrentMap<String, NotificationFilter> filters = new ConcurrentHashMap<>();
	private final Notifier delegate;
	private long lastCleanup;
	private long cleanupInterval = 10_000L;
	private AtomicLong counter = new AtomicLong();

	public FilteringNotifier(Notifier delegate) {
		this.delegate = delegate;
	}

	@Override
	public void notify(ClientApplicationEvent event) {
		if (!filter(event)) {
			delegate.notify(event);
		}
	}

	private boolean filter(ClientApplicationEvent event) {
		cleanUp();
		for (Entry<String, NotificationFilter> entry : getNotificationFilters().entrySet()) {
			if (entry.getValue().filter(event)) {
				LOGGER.debug("The event '{}' was suppressed by filter '{}'", event, entry);
				return true;
			}
		}
		return false;
	}

	private void cleanUp() {
		long now = System.currentTimeMillis();
		if (lastCleanup + cleanupInterval > now) {
			return;
		}
		lastCleanup = now;
		for (Entry<String, NotificationFilter> entry : getNotificationFilters().entrySet()) {
			if (entry.getValue() instanceof ExpiringNotificationFilter
					&& ((ExpiringNotificationFilter) entry.getValue()).isExpired()) {
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

	public void setCleanupInterval(long cleanupInterval) {
		this.cleanupInterval = cleanupInterval;
	}
}
