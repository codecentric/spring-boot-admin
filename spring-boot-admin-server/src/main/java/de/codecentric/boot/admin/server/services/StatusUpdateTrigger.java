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

import java.time.Duration;

import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

public class StatusUpdateTrigger extends AbstractEventHandler<InstanceEvent> {

	private static final Logger log = LoggerFactory.getLogger(StatusUpdateTrigger.class);

	private final StatusUpdater statusUpdater;

	private final IntervalCheck intervalCheck;

	private final Publisher<InstanceId> existingInstanceIds;

	@Nullable private Disposable startupSubscription;

	public StatusUpdateTrigger(StatusUpdater statusUpdater, Publisher<InstanceEvent> publisher, Duration updateInterval,
			Duration statusLifetime, Duration maxBackoff) {
		this(statusUpdater, publisher, updateInterval, statusLifetime, maxBackoff, Flux.empty());
	}

	public StatusUpdateTrigger(StatusUpdater statusUpdater, Publisher<InstanceEvent> publisher, Duration updateInterval,
			Duration statusLifetime, Duration maxBackoff, Publisher<InstanceId> existingInstanceIds) {
		super(publisher, InstanceEvent.class);
		this.statusUpdater = statusUpdater;
		this.intervalCheck = new IntervalCheck("status", this::updateStatus, updateInterval, statusLifetime,
				maxBackoff);
		this.existingInstanceIds = existingInstanceIds;
	}

	@Override
	protected Publisher<Void> handle(Flux<InstanceEvent> publisher) {
		return publisher
			.filter((event) -> event instanceof InstanceRegisteredEvent
					|| event instanceof InstanceRegistrationUpdatedEvent)
			.flatMap((event) -> updateStatus(event.getInstance()));
	}

	protected Mono<Void> updateStatus(InstanceId instanceId) {
		return this.statusUpdater.timeout(this.intervalCheck.getInterval())
			.updateStatus(instanceId)
			.onErrorResume((e) -> {
				log.warn("Unexpected error while updating status for {}", instanceId, e);
				return Mono.empty();
			})
			.doFinally((s) -> this.intervalCheck.markAsChecked(instanceId));
	}

	@Override
	public void start() {
		super.start();
		this.intervalCheck.start();
		this.startupSubscription = Flux.from(this.existingInstanceIds).flatMap(this::updateStatus).subscribe();
	}

	@Override
	public void stop() {
		if (this.startupSubscription != null) {
			this.startupSubscription.dispose();
			this.startupSubscription = null;
		}
		super.stop();
		this.intervalCheck.stop();
	}

	public void setInterval(Duration updateInterval) {
		this.intervalCheck.setInterval(updateInterval);
	}

	public void setLifetime(Duration statusLifetime) {
		this.intervalCheck.setMinRetention(statusLifetime);
	}

}
