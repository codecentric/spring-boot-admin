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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.codecentric.boot.admin.event.ClientApplicationEvent;

/**
 * Abstract Notifier which allows disabling and filtering of events.
 *
 * @author Johannes Edmeier
 */
public abstract class AbstractEventNotifier implements Notifier {

	/**
	 * Enables the notification.
	 */
	private boolean enabled = true;

	@Override
	public void notify(ClientApplicationEvent event) {
		if (enabled && shouldNotify(event)) {
			try {
				doNotify(event);
			} catch (Exception ex) {
				getLogger().error("Couldn't notify for event {} ", event, ex);
			}
		}
	}

	protected boolean shouldNotify(ClientApplicationEvent event) {
		return true;
	}

	protected abstract void doNotify(ClientApplicationEvent event) throws Exception;

	private Logger getLogger() {
		return LoggerFactory.getLogger(this.getClass());
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
