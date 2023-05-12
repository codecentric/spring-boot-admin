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
import de.codecentric.boot.admin.server.domain.values.Registration;

/**
 * This event gets emitted when an instance is registered.
 *
 * @author Johannes Edmeier
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class InstanceRegisteredEvent extends InstanceEvent {

	public static final String TYPE = "REGISTERED";

	private static final long serialVersionUID = 1L;

	private final Registration registration;

	public InstanceRegisteredEvent(InstanceId instance, long version, Registration registration) {
		this(instance, version, Instant.now(), registration);
	}

	public InstanceRegisteredEvent(InstanceId instance, long version, Instant timestamp, Registration registration) {
		super(instance, version, TYPE, timestamp);
		this.registration = registration;
	}

}
