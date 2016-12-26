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
package de.codecentric.boot.admin.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a certain status a certain time.
 *
 * @author Johannes Stelzer
 */
public class StatusInfo implements Serializable {
	private static final long serialVersionUID = 2L;

	private final String status;
	private final long timestamp;
	private final Map<String, ? extends Serializable> details;

	protected StatusInfo(String status, long timestamp,
			Map<String, ? extends Serializable> details) {
		this.status = status.toUpperCase();
		this.timestamp = timestamp;
		this.details = details != null ? Collections.unmodifiableMap(new HashMap<>(details))
				: Collections.<String, Serializable>emptyMap();
	}

	public static StatusInfo valueOf(String statusCode,
			Map<String, ? extends Serializable> details) {
		return new StatusInfo(statusCode, System.currentTimeMillis(), details);
	}

	public static StatusInfo valueOf(String statusCode) {
		return valueOf(statusCode, null);
	}

	public static StatusInfo ofUnknown() {
		return valueOf("UNKNOWN", null);
	}

	public static StatusInfo ofUp() {
		return ofUp(null);
	}

	public static StatusInfo ofDown() {
		return ofDown(null);
	}

	public static StatusInfo ofOffline() {
		return ofOffline(null);
	}

	public static StatusInfo ofUp(Map<String, ? extends Serializable> details) {
		return valueOf("UP", details);
	}

	public static StatusInfo ofDown(Map<String, ? extends Serializable> details) {
		return valueOf("DOWN", details);
	}

	public static StatusInfo ofOffline(Map<String, ? extends Serializable> details) {
		return valueOf("OFFLINE", details);
	}

	public String getStatus() {
		return status;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public Map<String, ? extends Serializable> getDetails() {
		return details;
	}

	@JsonIgnore
	public boolean isUp() {
		return "UP".equals(status);
	}

	@JsonIgnore
	public boolean isOffline() {
		return "OFFLINE".equals(status);
	}

	@JsonIgnore
	public boolean isDown() {
		return "DOWN".equals(status);
	}

	@JsonIgnore
	public boolean isUnknown() {
		return "UNKNOWN".equals(status);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		StatusInfo other = (StatusInfo) obj;
		if (status == null) {
			if (other.status != null) {
				return false;
			}
		} else if (!status.equals(other.status)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "StatusInfo [status=" + status + ", timestamp=" + timestamp + "]";
	}

}