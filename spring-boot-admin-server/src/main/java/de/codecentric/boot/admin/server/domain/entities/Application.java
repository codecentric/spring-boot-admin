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
package de.codecentric.boot.admin.server.domain.entities;

import de.codecentric.boot.admin.server.domain.events.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.util.Assert;

/**
 * The aggregate representing a registered application at the spring boot admin.
 *
 * @author Johannes Edmeier
 */
@lombok.Data
public class Application implements Serializable {
    private final ApplicationId id;
    private final long version;
    private final Registration registration;
    private final StatusInfo statusInfo;
    private final Info info;
    private final List<ClientApplicationEvent> unsavedEvents;
    private final Endpoints endpoints;

    private Application(ApplicationId id) {
        this(id, -1L, null, StatusInfo.ofUnknown(), Info.empty(), Endpoints.empty(), Collections.emptyList());
    }

    private Application(ApplicationId id,
                        long version,
                        Registration registration,
                        StatusInfo statusInfo,
                        Info info, Endpoints endpoints,
                        List<ClientApplicationEvent> unsavedEvents) {
        Assert.notNull(id, "'id' must not be null");
        this.id = id;
        this.version = version;
        this.registration = registration;
        this.statusInfo = statusInfo;
        this.info = info;
        this.endpoints = endpoints;
        this.unsavedEvents = unsavedEvents;
    }

    public static Application create(ApplicationId id) {
        Assert.notNull(id, "'id' must not be null");
        return new Application(id);
    }

    public Application register(Registration registration) {
        Assert.notNull(registration, "'registration' must not be null");
        if (Objects.equals(this.registration, registration)) {
            return this;
        }
        if (this.registration == null) {
            return this.apply(new ClientApplicationRegisteredEvent(this.id, this.nextVersion(), registration), true);
        } else {
            return this.apply(new ClientApplicationRegistrationUpdatedEvent(this.id, this.nextVersion(), registration),
                    true);
        }
    }

    public Application deregister() {
        if (isRegistered()) {
            return this.apply(new ClientApplicationDeregisteredEvent(this.id, this.nextVersion()), true);
        }
        return this;
    }

    public Application withInfo(Info info) {
        Assert.notNull(info, "'info' must not be null");
        if (Objects.equals(this.info, info)) {
            return this;
        }
        return this.apply(new ClientApplicationInfoChangedEvent(this.id, this.nextVersion(), info), true);
    }

    public Application withStatusInfo(StatusInfo statusInfo) {
        Assert.notNull(statusInfo, "'statusInfo' must not be null");
        if (Objects.equals(this.statusInfo.getStatus(), statusInfo.getStatus())) {
            return this;
        }
        return this.apply(new ClientApplicationStatusChangedEvent(this.id, this.nextVersion(), statusInfo), true);
    }

    public Application withEndpoints(Endpoints endpoints) {
        Assert.notNull(endpoints, "'endpoints' must not be null");
        if (Objects.equals(this.endpoints, endpoints)) {
            return this;
        }
        return this.apply(new ClientApplicationEndpointsDetectedEvent(this.id, this.nextVersion(), endpoints), true);
    }


    public boolean isRegistered() {
        return this.registration != null;
    }

    List<ClientApplicationEvent> getUnsavedEvents() {
        return Collections.unmodifiableList(unsavedEvents);
    }

    Application loadHistory(Collection<ClientApplicationEvent> events) {
        Application application = this;
        for (ClientApplicationEvent event : events) {
            application = application.apply(event, false);
        }
        return application;
    }

    Application apply(ClientApplicationEvent event, boolean isNewEvent) {
        Assert.notNull(event, "'event' must not be null");
        Assert.isTrue(this.id.equals(event.getApplication()), "'event' must be for the same application");
        Assert.isTrue(this.nextVersion() == event.getVersion(), "expected event version doesn't match");

        List<ClientApplicationEvent> unsavedEvents = appendToEvents(event, isNewEvent);
        if (event instanceof ClientApplicationRegisteredEvent) {
            Registration registration = ((ClientApplicationRegisteredEvent) event).getRegistration();
            return new Application(this.id, event.getVersion(), registration, this.statusInfo, this.info,
                    Endpoints.empty(), unsavedEvents);
        } else if (event instanceof ClientApplicationRegistrationUpdatedEvent) {
            return new Application(this.id, event.getVersion(),
                    ((ClientApplicationRegistrationUpdatedEvent) event).getRegistration(), this.statusInfo, this.info,
                    this.endpoints, unsavedEvents);
        } else if (event instanceof ClientApplicationStatusChangedEvent) {
            return new Application(this.id, event.getVersion(), this.registration,
                    ((ClientApplicationStatusChangedEvent) event).getStatusInfo(), this.info, this.endpoints,
                    unsavedEvents);
        } else if (event instanceof ClientApplicationEndpointsDetectedEvent) {
            return new Application(this.id, event.getVersion(), this.registration, this.statusInfo, this.info,
                    ((ClientApplicationEndpointsDetectedEvent) event).getEndpoints(), unsavedEvents);
        } else if (event instanceof ClientApplicationInfoChangedEvent) {
            return new Application(this.id, event.getVersion(), this.registration, this.statusInfo,
                    ((ClientApplicationInfoChangedEvent) event).getInfo(), this.endpoints, unsavedEvents);
        } else if (event instanceof ClientApplicationDeregisteredEvent) {
            return new Application(this.id, event.getVersion(), null, StatusInfo.ofUnknown(), Info.empty(),
                    Endpoints.empty(), unsavedEvents);
        }

        return this;
    }

    private long nextVersion() {
        return this.version + 1L;
    }

    private List<ClientApplicationEvent> appendToEvents(ClientApplicationEvent event, boolean isNewEvent) {
        if (!isNewEvent) {
            return this.unsavedEvents;
        }

        ArrayList<ClientApplicationEvent> events = new ArrayList<>(this.unsavedEvents.size() + 1);
        events.addAll(this.unsavedEvents);
        events.add(event);
        return events;
    }
}
