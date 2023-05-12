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

import java.time.Instant;

import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

/**
 * This event gets emitted when an instance changes its status.
 *
 * @author Johannes Edmeier
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class InstanceStatusChangedEvent extends InstanceEvent {

	public static final String TYPE = "STATUS_CHANGED";

	private static final long serialVersionUID = 1L;

	private final StatusInfo statusInfo;

	public InstanceStatusChangedEvent(InstanceId instance, long version, StatusInfo statusInfo) {
		this(instance, version, Instant.now(), statusInfo);
	}

	public InstanceStatusChangedEvent(InstanceId instance, long version, Instant timestamp, StatusInfo statusInfo) {
		super(instance, version, TYPE, timestamp);
		this.statusInfo = statusInfo;
	}

	public StatusInfo getStatusInfo() {
		return statusInfo;
	}

}
