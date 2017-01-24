/*
 * Copyright 2016 the original author or authors.
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

import java.util.Arrays;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;

/**
 * Abstract Notifier for status change which allows filtering of certain status changes.
 *
 * @author Johannes Edmeier
 */
public abstract class AbstractStatusChangeNotifier extends AbstractEventNotifier {

	/**
	 * List of changes to ignore. Must be in Format OLD:NEW, for any status use * as wildcard, e.g.
	 * *:UP or OFFLINE:*
	 */
	private String[] ignoreChanges = { "UNKNOWN:UP" };

	@Override
	protected boolean shouldNotify(ClientApplicationEvent event) {
		if (event instanceof ClientApplicationStatusChangedEvent) {
			ClientApplicationStatusChangedEvent statusChange = (ClientApplicationStatusChangedEvent) event;
			String from = statusChange.getFrom().getStatus();
			String to = statusChange.getTo().getStatus();
			return Arrays.binarySearch(ignoreChanges, from + ":" + to) < 0
					&& Arrays.binarySearch(ignoreChanges, "*:" + to) < 0
					&& Arrays.binarySearch(ignoreChanges, from + ":*") < 0;
		}
		return false;
	}

	public void setIgnoreChanges(String[] ignoreChanges) {
		String[] copy = Arrays.copyOf(ignoreChanges, ignoreChanges.length);
		Arrays.sort(copy);
		this.ignoreChanges = copy;
	}

	public String[] getIgnoreChanges() {
		return ignoreChanges;
	}
}
