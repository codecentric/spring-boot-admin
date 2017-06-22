/*
 * Copyright 2014-2017 the original author or authors.
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

import de.codecentric.boot.admin.server.domain.values.ApplicationId;

import java.io.Serializable;

/**
 * Abstract Event regearding spring boot admin clients
 *
 * @author Johannes Edmeier
 */
@lombok.Data
public abstract class ClientApplicationEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ApplicationId application;
    private final long version;
    private final long timestamp;
    private final String type;

    protected ClientApplicationEvent(ApplicationId application, long version, String type) {
        this.application = application;
        this.version = version;
        this.timestamp = System.currentTimeMillis();
        this.type = type;
    }
}
