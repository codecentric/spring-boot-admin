/*
 * Copyright 2014-2026 the original author or authors.
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

package de.codecentric.boot.admin.server.web.cache;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.services.AbstractEventHandler;

/**
 * Invalidates {@link ActuatorResponseCache} entries when instance lifecycle events occur.
 *
 * <p>
 * The following events trigger a full instance cache invalidation:
 * <ul>
 * <li>{@link InstanceDeregisteredEvent} – the instance is gone</li>
 * <li>{@link InstanceRegistrationUpdatedEvent} – management URL or metadata may have
 * changed</li>
 * <li>{@link InstanceEndpointsDetectedEvent} – available endpoints may have changed</li>
 * </ul>
 */
public class CacheInvalidationTrigger extends AbstractEventHandler<InstanceEvent> {

	private final ActuatorResponseCache responseCache;

	public CacheInvalidationTrigger(Publisher<InstanceEvent> publisher, ActuatorResponseCache responseCache) {
		super(publisher, InstanceEvent.class);
		this.responseCache = responseCache;
	}

	@Override
	protected Publisher<Void> handle(Flux<InstanceEvent> publisher) {
		return publisher.filter((event) -> event instanceof InstanceDeregisteredEvent
				|| event instanceof InstanceRegistrationUpdatedEvent || event instanceof InstanceEndpointsDetectedEvent)
			.flatMap((event) -> {
				this.responseCache.invalidateAllForInstance(event.getInstance());
				return Mono.empty();
			});
	}

}
