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

package de.codecentric.boot.admin.server.domain.events;

import java.io.Serial;
import java.time.Instant;
import java.util.Map;

import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * This event gets emitted when an instance changes its status details.
 */
@lombok.Value
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class InstanceStatusDetailsChangedEvent extends InstanceEvent {

	public static final String TYPE = "STATUS_DETAILS_CHANGED";

	@Serial
	private static final long serialVersionUID = 1L;

	Map<String, Object> details;

	public InstanceStatusDetailsChangedEvent(InstanceId instance, long version, Map<String, Object> statusInfoDetails) {
		this(instance, version, Instant.now(), statusInfoDetails);
	}

	public InstanceStatusDetailsChangedEvent(InstanceId instance, long version, Instant timestamp,
			Map<String, Object> statusInfoDetails) {
		super(instance, version, TYPE, timestamp);
		this.details = statusInfoDetails;
	}

}
