/*
 * Copyright 2014 the original author or authors.
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
package de.codecentric.boot.admin.journal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;

/**
 *
 * Journal-representation of ClientApplicationEvents. In opposite to the
 * ClientApplicationEvent.class this Class has no reference to the event-source.
 *
 * @author Johannes Stelzer
 *
 */
@JsonInclude(Include.NON_EMPTY)
public class JournaledEvent {

	public enum Type {
		REGISTRATION, DEREGISTRATION, STATUS_CHANGE
	}

	private final Type type;
	private final long timestamp;
	private final Application application;
	private final Map<String, Object> data;

	private JournaledEvent(Type type, long timestamp, Application application,
			Map<String, Object> data) {
		this.type = type;
		this.timestamp = timestamp;
		this.application = application;
		if (data != null) {
			this.data = Collections.unmodifiableMap(new HashMap<String, Object>(
					data));
		} else {
			this.data = Collections.emptyMap();
		}
	}

	public static JournaledEvent fromEvent(ClientApplicationEvent event) {
		long timestamp = event.getTimestamp();
		Application application = event.getApplication();

		if (event instanceof ClientApplicationRegisteredEvent) {
			return new JournaledEvent(Type.REGISTRATION, timestamp, application,
					null);

		} else if (event instanceof ClientApplicationDeregisteredEvent) {
			return new JournaledEvent(Type.DEREGISTRATION, timestamp,
					application, null);

		} else if (event instanceof ClientApplicationStatusChangedEvent) {
			ClientApplicationStatusChangedEvent changeEvent = (ClientApplicationStatusChangedEvent) event;
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("from", changeEvent.getFrom().getStatus());
			data.put("to", changeEvent.getTo().getStatus());

			return new JournaledEvent(Type.STATUS_CHANGE, timestamp, application,
					data);
		}

		throw new IllegalArgumentException(
				"Couldn't convert event to JournaledEvent: " + event.toString());
	}

	public Application getApplication() {
		return application;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "JournaledEvent [type=" + type + ", timestamp=" + timestamp
				+ ", application=" + application + ", data=" + data + "]";
	}

}