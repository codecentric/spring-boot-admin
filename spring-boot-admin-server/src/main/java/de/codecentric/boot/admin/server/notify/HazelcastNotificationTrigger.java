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

import java.util.concurrent.ConcurrentMap;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

public class HazelcastNotificationTrigger extends NotificationTrigger {

	private static final Logger log = LoggerFactory.getLogger(HazelcastNotificationTrigger.class);

	private final ConcurrentMap<InstanceId, Long> sentNotifications;

	public HazelcastNotificationTrigger(Notifier notifier, Publisher<InstanceEvent> events,
			ConcurrentMap<InstanceId, Long> sentNotifications) {
		super(notifier, events);
		this.sentNotifications = sentNotifications;
	}

	@Override
	protected Mono<Void> sendNotifications(InstanceEvent event) {
		while (true) {
			Long lastSentEvent = this.sentNotifications.getOrDefault(event.getInstance(), -1L);
			if (lastSentEvent >= event.getVersion()) {
				log.debug("Notifications already sent. Not triggering notifiers for {}", event);
				return Mono.empty();
			}

			if (lastSentEvent < 0) {
				if (this.sentNotifications.putIfAbsent(event.getInstance(), event.getVersion()) == null) {
					log.debug("Triggering notifiers for {}", event);
					return super.sendNotifications(event);
				}
			}
			else {
				if (this.sentNotifications.replace(event.getInstance(), lastSentEvent, event.getVersion())) {
					log.debug("Triggering notifiers for {}", event);
					return super.sendNotifications(event);
				}
			}
		}
	}

}
