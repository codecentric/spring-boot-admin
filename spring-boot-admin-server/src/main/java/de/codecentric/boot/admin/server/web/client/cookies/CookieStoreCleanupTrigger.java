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

package de.codecentric.boot.admin.server.web.client.cookies;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.services.AbstractEventHandler;

/**
 * Triggers cleanup of {@link de.codecentric.boot.admin.server.domain.entities.Instance}
 * specific data in {@link PerInstanceCookieStore} on receiving an
 * {@link InstanceDeregisteredEvent}.
 */
public class CookieStoreCleanupTrigger extends AbstractEventHandler<InstanceDeregisteredEvent> {

	private final PerInstanceCookieStore cookieStore;

	/**
	 * Creates a trigger to cleanup the cookie store on deregistering of an
	 * {@link de.codecentric.boot.admin.server.domain.entities.Instance}.
	 * @param publisher publisher of {@link InstanceEvent}s events
	 * @param cookieStore the store to inform about deregistration of an
	 * {@link de.codecentric.boot.admin.server.domain.entities.Instance}
	 */
	public CookieStoreCleanupTrigger(final Publisher<InstanceEvent> publisher,
			final PerInstanceCookieStore cookieStore) {
		super(publisher, InstanceDeregisteredEvent.class);

		this.cookieStore = cookieStore;
	}

	@Override
	protected Publisher<Void> handle(final Flux<InstanceDeregisteredEvent> publisher) {
		return publisher.flatMap((event) -> {
			cleanupCookieStore(event);
			return Mono.empty();
		});
	}

	private void cleanupCookieStore(final InstanceDeregisteredEvent event) {
		cookieStore.cleanupInstance(event.getInstance());
	}

}
