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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;

/**
 * Evicts stale {@link de.codecentric.boot.admin.server.domain.entities.Instance} specific
 * data from the {@link HealthGroupsCache} whenever an {@link InstanceDeregisteredEvent}
 * is received.
 * <p>
 * Since Spring Boot Admin's {@link InstanceEvent}s are propagated through a reactive
 * {@link Publisher} rather than the Spring {@code ApplicationContext} event mechanism,
 * this listener subscribes to that stream directly. Cache eviction is a lightweight,
 * in-memory operation reacting to a rarely occurring event, so it runs on the publishing
 * thread.
 */
public class HealthGroupsCacheCleanupListener {

	private final Publisher<InstanceEvent> publisher;

	private final HealthGroupsCache healthGroupsCache;

	@Nullable
	private Disposable subscription;

	/**
	 * Creates a listener evicting health groups cache entries on deregistration of an
	 * {@link de.codecentric.boot.admin.server.domain.entities.Instance}.
	 * @param publisher publisher of {@link InstanceEvent}s
	 * @param healthGroupsCache the cache to evict entries from on deregistration of an
	 * {@link de.codecentric.boot.admin.server.domain.entities.Instance}
	 */
	public HealthGroupsCacheCleanupListener(final Publisher<InstanceEvent> publisher,
			final HealthGroupsCache healthGroupsCache) {
		this.publisher = publisher;
		this.healthGroupsCache = healthGroupsCache;
	}

	@PostConstruct
	public void start() {
		this.subscription = Flux.from(this.publisher).ofType(InstanceDeregisteredEvent.class)
				.doOnNext((event) -> this.healthGroupsCache.remove(event.getInstance())).subscribe();
	}

	@PreDestroy
	public void stop() {
		if (this.subscription != null) {
			this.subscription.dispose();
			this.subscription = null;
		}
	}

}
