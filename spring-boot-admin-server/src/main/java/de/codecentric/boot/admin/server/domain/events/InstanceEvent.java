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

import de.codecentric.boot.admin.server.domain.values.InstanceId;

import java.io.Serializable;
import java.time.Instant;
import org.springframework.util.Assert;

/**
 * Abstract Event regarding registered instances
 *
 * @author Johannes Edmeier
 */
@lombok.Data
public abstract class InstanceEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final InstanceId instance;
    private final long version;
    private final Instant timestamp;
    private final String type;

    protected InstanceEvent(InstanceId instance, long version, String type, Instant timestamp) {
        Assert.notNull(instance, "'instance' must not be null");
        Assert.notNull(timestamp, "'timestamp' must not be null");
        Assert.hasText(type, "'type' must not be empty");
        this.instance = instance;
        this.version = version;
        this.timestamp = timestamp;
        this.type = type;
    }
}
