/*
 * Copyright 2013-2014 the original author or authors.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;

/**
 * Notifier that just writes to a logger.
 *
 * @author Johannes Edmeier
 */
public class LoggingNotifier extends AbstractStatusChangeNotifier {
	private static Logger LOGGER = LoggerFactory.getLogger(LoggingNotifier.class);

	@Override
	protected void doNotify(ClientApplicationEvent event) throws Exception {
		if (event instanceof ClientApplicationStatusChangedEvent) {
			LOGGER.info("Application {} ({}) is {}", event.getApplication().getName(),
					event.getApplication().getId(), ((ClientApplicationStatusChangedEvent) event).getTo().getStatus());
		} else {
			LOGGER.info("Application {} ({}) {}", event.getApplication().getName(),
					event.getApplication().getId(), event.getType());
		}
	}

}
