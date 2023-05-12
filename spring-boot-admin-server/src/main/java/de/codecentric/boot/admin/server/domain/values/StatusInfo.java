/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.domain.values;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import static java.util.Arrays.asList;

/**
 * Instance status with details fetched from the info endpoint.
 *
 * @author Johannes Edmeier
 */
@lombok.Data
public final class StatusInfo implements Serializable {

	public static final String STATUS_UNKNOWN = "UNKNOWN";

	public static final String STATUS_OUT_OF_SERVICE = "OUT_OF_SERVICE";

	public static final String STATUS_UP = "UP";

	public static final String STATUS_DOWN = "DOWN";

	public static final String STATUS_OFFLINE = "OFFLINE";

	public static final String STATUS_RESTRICTED = "RESTRICTED";

	private static final List<String> STATUS_ORDER = asList(STATUS_DOWN, STATUS_OUT_OF_SERVICE, STATUS_OFFLINE,
			STATUS_UNKNOWN, STATUS_RESTRICTED, STATUS_UP);

	private final String status;

	private final Map<String, Object> details;

	private StatusInfo(String status, @Nullable Map<String, ?> details) {
		Assert.hasText(status, "'status' must not be empty.");
		this.status = status.toUpperCase();
		this.details = (details != null) ? new HashMap<>(details) : Collections.emptyMap();
	}

	public static StatusInfo valueOf(String statusCode, @Nullable Map<String, ?> details) {
		return new StatusInfo(statusCode, details);
	}

	public static StatusInfo valueOf(String statusCode) {
		return valueOf(statusCode, null);
	}

	public static StatusInfo ofUnknown() {
		return valueOf(STATUS_UNKNOWN, null);
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

	public static StatusInfo ofUp(@Nullable Map<String, Object> details) {
		return valueOf(STATUS_UP, details);
	}

	public static StatusInfo ofDown(@Nullable Map<String, Object> details) {
		return valueOf(STATUS_DOWN, details);
	}

	public static StatusInfo ofOffline(@Nullable Map<String, Object> details) {
		return valueOf(STATUS_OFFLINE, details);
	}

	public Map<String, Object> getDetails() {
		return Collections.unmodifiableMap(details);
	}

	public boolean isUp() {
		return STATUS_UP.equals(status);
	}

	public boolean isOffline() {
		return STATUS_OFFLINE.equals(status);
	}

	public boolean isDown() {
		return STATUS_DOWN.equals(status);
	}

	public boolean isUnknown() {
		return STATUS_UNKNOWN.equals(status);
	}

	public static Comparator<String> severity() {
		return Comparator.comparingInt(STATUS_ORDER::indexOf);
	}

	@SuppressWarnings("unchecked")
	public static StatusInfo from(Map<String, ?> body) {
		Map<String, ?> details = Collections.emptyMap();

		/*
		 * Key "details" is present when accessing Spring Boot Actuator Health using
		 * Accept-Header {@link org.springframework.boot.actuate.endpoint.ApiVersion#V2}.
		 */
		if (body.containsKey("details")) {
			details = (Map<String, ?>) body.get("details");
		}
		else if (body.containsKey("components")) {
			details = (Map<String, ?>) body.get("components");
		}

		return StatusInfo.valueOf((String) body.get("status"), details);
	}

}
