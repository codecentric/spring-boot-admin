package de.codecentric.boot.admin.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a certain status a certain time.
 *
 * @author Johannes Stelzer
 */
public class StatusInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String status;
	private final long timestamp;

	protected StatusInfo(String status, long timestamp) {
		this.status = status.toUpperCase();
		this.timestamp = timestamp;
	}

	public static StatusInfo valueOf(String statusCode) {
		return new StatusInfo(statusCode, System.currentTimeMillis());
	}

	@JsonCreator
	public static StatusInfo valueOf(@JsonProperty("status") String statusCode,
			@JsonProperty("timestamp") long timestamp) {
		return new StatusInfo(statusCode, timestamp);
	}

	public static StatusInfo ofUnknown() {
		return valueOf("UNKNOWN");
	}

	public static StatusInfo ofUp() {
		return valueOf("UP");
	}

	public static StatusInfo ofDown() {
		return valueOf("DOWN");
	}

	public static StatusInfo ofOffline() {
		return valueOf("OFFLINE");
	}

	public String getStatus() {
		return status;
	}

	public long getTimestamp() {
		return timestamp;
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