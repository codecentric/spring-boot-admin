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

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;

public class EndpointDetectionTrigger extends AbstractEventHandler<InstanceEvent> {

	private static final Logger log = LoggerFactory.getLogger(EndpointDetectionTrigger.class);

	private final EndpointDetector endpointDetector;

	public EndpointDetectionTrigger(EndpointDetector endpointDetector, Publisher<InstanceEvent> publisher) {
		super(publisher, InstanceEvent.class);
		this.endpointDetector = endpointDetector;
	}

	@Override
	protected Publisher<Void> handle(Flux<InstanceEvent> publisher) {
		return publisher
			.filter((event) -> event instanceof InstanceStatusChangedEvent
					|| event instanceof InstanceRegistrationUpdatedEvent)
			.flatMap(this::detectEndpoints);
	}

	protected Mono<Void> detectEndpoints(InstanceEvent event) {
		return this.endpointDetector.detectEndpoints(event.getInstance()).onErrorResume((e) -> {
			log.warn("Unexpected error while detecting endpoints for {}", event.getInstance(), e);
			return Mono.empty();
		});
	}

}
