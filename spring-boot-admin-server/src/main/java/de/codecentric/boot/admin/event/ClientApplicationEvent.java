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
package de.codecentric.boot.admin.event;

import java.io.Serializable;

import de.codecentric.boot.admin.model.Application;

/**
 * Abstract Event regearding spring boot admin clients
 *
 * @author Johannes Edmeier
 */
public abstract class ClientApplicationEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Application application;

	private final long timestamp;
	private final String type;

	protected ClientApplicationEvent(Application application, String type) {
		this.application = application;
		this.timestamp = System.currentTimeMillis();
		this.type = type;
	}

	/**
	 * @return system time in milliseconds when the event happened.
	 */
	public final long getTimestamp() {
		return this.timestamp;
	}

	/**
	 * @return affected application.
	 */
	public Application getApplication() {
		return application;
	}

	/**
	 * @return event type (for JSON).
	 */
	public String getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((application == null) ? 0 : application.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ClientApplicationEvent other = (ClientApplicationEvent) obj;
		if (application == null) {
			if (other.application != null) {
				return false;
			}
		} else if (!application.equals(other.application)) {
			return false;
		}
		if (timestamp != other.timestamp) {
			return false;
		}
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}
}
