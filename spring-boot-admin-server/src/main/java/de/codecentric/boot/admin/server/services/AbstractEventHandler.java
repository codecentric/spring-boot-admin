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

package de.codecentric.boot.admin.server.services;

import java.util.logging.Level;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;

public abstract class AbstractEventHandler<T extends InstanceEvent> {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final Publisher<InstanceEvent> publisher;

	private final Class<T> eventType;

	@Nullable
	private Disposable subscription;

	@Nullable
	private Scheduler scheduler;

	protected AbstractEventHandler(Publisher<InstanceEvent> publisher, Class<T> eventType) {
		this.publisher = publisher;
		this.eventType = eventType;
	}

	public void start() {
		this.scheduler = this.createScheduler();
		this.subscription = Flux.from(this.publisher)
			.subscribeOn(this.scheduler)
			.log(this.log.getName(), Level.FINEST)
			.doOnSubscribe((s) -> this.log.debug("Subscribed to {} events", this.eventType))
			.ofType(this.eventType)
			.cast(this.eventType)
			.transform(this::handle)
			.onErrorContinue((throwable, o) -> this.log.warn("Unexpected error", throwable))
			.subscribe();
	}

	protected abstract Publisher<Void> handle(Flux<T> publisher);

	protected Scheduler createScheduler() {
		return Schedulers.newSingle(this.getClass().getSimpleName());
	}

	public void stop() {
		if (this.subscription != null) {
			this.subscription.dispose();
			this.subscription = null;
		}
		if (this.scheduler != null) {
			this.scheduler.dispose();
			this.scheduler = null;
		}
	}

}
