package de.codecentric.boot.admin.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

}