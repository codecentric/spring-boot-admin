/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.domain.events;

import java.time.Instant;

import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * This event gets emitted when all instance's endpoints are discovered.
 *
 * @author Johannes Stelzer
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class InstanceEndpointsDetectedEvent extends InstanceEvent {
    private static final long serialVersionUID = 1L;
    private final Endpoints endpoints;

    public InstanceEndpointsDetectedEvent(InstanceId instance, long version, Instant timestamp, Endpoints endpoints) {
        super(instance, version, "ENDPOINTS_DETECTED", timestamp);
        this.endpoints = endpoints;
    }

    public InstanceEndpointsDetectedEvent(InstanceId instance, long version, Endpoints endpoints) {
        super(instance, version, "ENDPOINTS_DETECTED");
        this.endpoints = endpoints;
    }
}
