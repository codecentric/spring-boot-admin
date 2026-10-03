/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.notify;

import java.util.Arrays;

import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * Abstract Notifier for status change which allows filtering of certain status changes.
 *
 * @author Johannes Edmeier
 */
public abstract class AbstractStatusChangeNotifier extends AbstractEventNotifier {

	private LastStatusStore lastStatusStore = new LastStatusStore();

	/**
	 * List of changes to ignore. Must be in Format OLD:NEW, for any status use * as
	 * wildcard, e.g. *:UP or OFFLINE:*
	 */
	private String[] ignoreChanges = { "UNKNOWN:UP" };

	public AbstractStatusChangeNotifier(InstanceRepository repository) {
		super(repository);
	}

	@Override
	public Mono<Void> notify(InstanceEvent event) {
		return super.notify(event).then(Mono.fromRunnable(() -> updateLastStatus(event)));
	}

	@Override
	protected boolean shouldNotify(InstanceEvent event, Instance instance) {
		if (event instanceof InstanceStatusChangedEvent statusChangedEvent) {
			String from = getLastStatus(event.getInstance());
			String to = statusChangedEvent.getStatusInfo().getStatus();
			return !from.equals(to) && Arrays.binarySearch(ignoreChanges, from + ":" + to) < 0
					&& Arrays.binarySearch(ignoreChanges, "*:" + to) < 0
					&& Arrays.binarySearch(ignoreChanges, from + ":*") < 0;
		}
		return false;
	}

	protected final String getLastStatus(InstanceId instanceId) {
		String status = this.lastStatusStore.get(instanceId);
		return (status != null) ? status : "UNKNOWN";
	}

	protected void updateLastStatus(InstanceEvent event) {
		if (event instanceof InstanceDeregisteredEvent) {
			this.lastStatusStore.remove(event.getInstance());
		}
		if (event instanceof InstanceStatusChangedEvent statusChangedEvent) {
			this.lastStatusStore.put(event.getInstance(), statusChangedEvent.getStatusInfo().getStatus());
		}
	}

	/**
	 * Replace the store holding the last known status of the instances, e.g. with one
	 * that is shared between the members of a cluster.
	 * @param lastStatusStore the store to use
	 * @since 4.1.4
	 */
	public void setLastStatusStore(LastStatusStore lastStatusStore) {
		Assert.notNull(lastStatusStore, "'lastStatusStore' must not be null");
		this.lastStatusStore = lastStatusStore;
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
