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

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.services.AbstractEventHandler;

public class NotificationTrigger extends AbstractEventHandler<InstanceEvent> {

	private static final Logger log = LoggerFactory.getLogger(NotificationTrigger.class);

	private final Notifier notifier;

	public NotificationTrigger(Notifier notifier, Publisher<InstanceEvent> publisher) {
		super(publisher, InstanceEvent.class);
		this.notifier = notifier;
	}

	@Override
	protected Publisher<Void> handle(Flux<InstanceEvent> publisher) {
		return publisher.flatMap(this::sendNotifications);
	}

	protected Mono<Void> sendNotifications(InstanceEvent event) {
		return this.notifier.notify(event)
			.doOnError((e) -> log.warn("Couldn't notify for event {} ", event, e))
			.onErrorResume((e) -> Mono.empty());
	}

}
