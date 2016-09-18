/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.notify;

import de.codecentric.boot.admin.event.ClientApplicationEvent;

/**
 * A notifier delegating notifications to all specified notifiers.
 *
 * @author Sebastian Meiser
 */
public class CompositeNotifier implements Notifier {
	private final Iterable<Notifier> notifiers;

	public CompositeNotifier(Iterable<Notifier> notifiers) {
		this.notifiers = notifiers;
	}

	@Override
	public void notify(ClientApplicationEvent event) {
		for (Notifier notifier : notifiers) {
			notifier.notify(event);
		}
	}
}
