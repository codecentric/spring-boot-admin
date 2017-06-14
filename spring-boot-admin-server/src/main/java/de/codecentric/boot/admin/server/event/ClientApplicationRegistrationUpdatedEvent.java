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

package de.codecentric.boot.admin.server.event;

import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.Registration;

/**
 * This event gets emitted when an application is registered.
 *
 * @author Johannes Stelzer
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
public class ClientApplicationRegistrationUpdatedEvent extends ClientApplicationEvent {
    private static final long serialVersionUID = 1L;
    private final Registration registration;

    public ClientApplicationRegistrationUpdatedEvent(Application application, Registration registration) {
        super(application, "REGISTRATION_UPDATED");
        this.registration = registration;
    }
}

