/*
 * Copyright 2014-2025 the original author or authors.
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

package de.codecentric.boot.admin.server.services;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;

/**
 * Triggers cleanup of {@link de.codecentric.boot.admin.server.domain.entities.Instance}
 * specific data in {@link HealthGroupsCache} on receiving an
 * {@link InstanceDeregisteredEvent}.
 */
public class HealthGroupsCacheCleanupTrigger extends AbstractEventHandler<InstanceDeregisteredEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(HealthGroupsCacheCleanupTrigger.class);

	private final HealthGroupsCache healthGroupsCache;

	/**
	 * Creates a trigger to cleanup the health groups cache on deregistering of an
	 * {@link de.codecentric.boot.admin.server.domain.entities.Instance}.
	 * @param publisher publisher of {@link InstanceEvent}s events
	 * @param healthGroupsCache the cache to inform about deregistration of an
	 * {@link de.codecentric.boot.admin.server.domain.entities.Instance}
	 */
	public HealthGroupsCacheCleanupTrigger(final Publisher<InstanceEvent> publisher,
			final HealthGroupsCache healthGroupsCache) {
		super(publisher, InstanceDeregisteredEvent.class);

		this.healthGroupsCache = healthGroupsCache;
	}

	@Override
	protected Publisher<Void> handle(final Flux<InstanceDeregisteredEvent> publisher) {
		return publisher.flatMap((event) -> {
			this.healthGroupsCache.remove(event.getInstance());
			return Mono.empty();
		});
	}

}
