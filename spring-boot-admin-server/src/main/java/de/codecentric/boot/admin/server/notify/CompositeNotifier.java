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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;

/**
 * A notifier delegating notifications to all specified notifiers.
 *
 * @author Sebastian Meiser
 */
public class CompositeNotifier implements Notifier {

	private static final Logger log = LoggerFactory.getLogger(CompositeNotifier.class);

	private final Iterable<Notifier> delegates;

	public CompositeNotifier(Iterable<Notifier> delegates) {
		Assert.notNull(delegates, "'delegates' must not be null!");
		this.delegates = delegates;
	}

	@Override
	public Mono<Void> notify(InstanceEvent event) {
		return Flux.fromIterable(delegates).flatMap((d) -> d.notify(event).onErrorResume((error) -> {
			log.warn("Unexpected exception while triggering notifications. Notification might not be sent.", error);
			return Mono.empty();
		})).then();
	}

}
