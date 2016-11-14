/*
 * Copyright 2013-2016 the original author or authors.
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
package de.codecentric.boot.admin.event;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.boot.actuate.health.Health;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

/**
 * This event gets emitted when an health event change is detected.
 * 
 * @author dbs
 * @since 1.4.4
 * @version 1.0.0
 */
public class ClientApplicationHealthStatusChangedEvent extends ClientApplicationStatusChangedEvent {

	private static final long serialVersionUID = 1L;
	/**
	 * array of indicators name
	 */
	protected String[] indicator;
	/**
	 * array of Health sattus
	 */
	protected Health[] health;

	public ClientApplicationHealthStatusChangedEvent(Application newState, StatusInfo from,
			StatusInfo to) {
		super(newState, from, to);
	}

	public ClientApplicationHealthStatusChangedEvent(Application newState, StatusInfo oldStatus,
			StatusInfo newStatus, Map<String, Health> healthMap) {
		this(newState, oldStatus, newStatus);
		Iterator<Entry<String, Health>> iter = healthMap.entrySet().iterator();
		if (iter.hasNext()) {
			int size = healthMap.size(), i = 0;
			indicator = new String[size];
			health = new Health[size];
			while (iter.hasNext()) {
				Entry<String, Health> entry = iter.next();
				indicator[i] = entry.getKey();
				health[i++] = entry.getValue();
			}
		}
	}

	/**
	 * @return the indicator
	 */
	public String[] getIndicator() {
		return indicator;
	}

	/**
	 * @return the health
	 */
	public Health[] getHealth() {
		return health;
	}
}
