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

package de.codecentric.boot.admin.server.utils.jackson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;

/**
 * Jackson Mixin class helps in serialize/deserialize {@link InstanceEvent}s.
 *
 * @author Stefan Rempfer
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
@JsonSubTypes({
		@JsonSubTypes.Type(value = InstanceEndpointsDetectedEvent.class, name = InstanceEndpointsDetectedEvent.TYPE),
		@JsonSubTypes.Type(value = InstanceRegistrationUpdatedEvent.class,
				name = InstanceRegistrationUpdatedEvent.TYPE),
		@JsonSubTypes.Type(value = InstanceInfoChangedEvent.class, name = InstanceInfoChangedEvent.TYPE),
		@JsonSubTypes.Type(value = InstanceDeregisteredEvent.class, name = InstanceDeregisteredEvent.TYPE),
		@JsonSubTypes.Type(value = InstanceRegisteredEvent.class, name = InstanceRegisteredEvent.TYPE),
		@JsonSubTypes.Type(value = InstanceStatusChangedEvent.class, name = InstanceStatusChangedEvent.TYPE) })
public abstract class InstanceEventMixin {

}
