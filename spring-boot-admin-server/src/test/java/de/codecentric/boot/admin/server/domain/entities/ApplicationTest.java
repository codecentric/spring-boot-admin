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
import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import java.util.Collections;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ApplicationTest {

    @Test
    public void invariants() {
        assertThatThrownBy(() -> Application.create(null)).isInstanceOf(IllegalArgumentException.class)
                                                          .hasMessage("'id' must not be null");

        assertThatThrownBy(() -> Application.create(ApplicationId.of("id")).register(null)).isInstanceOf(
                IllegalArgumentException.class).hasMessage("'registration' must not be null");

        assertThatThrownBy(() -> Application.create(ApplicationId.of("id")).withInfo(null)).isInstanceOf(
                IllegalArgumentException.class).hasMessage("'info' must not be null");

        assertThatThrownBy(() -> Application.create(ApplicationId.of("id")).withStatusInfo(null)).isInstanceOf(
                IllegalArgumentException.class).hasMessage("'statusInfo' must not be null");

        assertThatThrownBy(() -> Application.create(ApplicationId.of("id")).withEndpoints(null)).isInstanceOf(
                IllegalArgumentException.class).hasMessage("'endpoints' must not be null");
    }

    @Test
    public void shoud_track_unsaved_events() {
        Registration registration = Registration.create("foo", "http://health").build();
        Registration registration2 = Registration.create("foo2", "http://health").build();
        Info info = Info.from(Collections.singletonMap("foo", "bar"));
        Application application = Application.create(ApplicationId.of("id"));

        assertThat(application.isRegistered()).isFalse();
        assertThat(application.getRegistration()).isNull();
        assertThat(application.getInfo()).isEqualTo(Info.empty());
        assertThat(application.getStatusInfo()).isEqualTo(StatusInfo.ofUnknown());
        assertThat(application.getUnsavedEvents()).isEmpty();

        application = application.register(registration).register(registration);
        assertThat(application.getRegistration()).isEqualTo(registration);
        assertThat(application.isRegistered()).isTrue();
        assertThat(application.getVersion()).isEqualTo(0L);

        application = application.register(registration2);
        assertThat(application.getRegistration()).isEqualTo(registration2);
        assertThat(application.isRegistered()).isTrue();
        assertThat(application.getVersion()).isEqualTo(1L);

        application = application.withStatusInfo(StatusInfo.ofUp()).withStatusInfo(StatusInfo.ofUp());
        assertThat(application.getStatusInfo()).isEqualTo(StatusInfo.ofUp());
        assertThat(application.getVersion()).isEqualTo(2L);

        application = application.withInfo(info).withInfo(info);
        assertThat(application.getInfo()).isEqualTo(info);
        assertThat(application.getVersion()).isEqualTo(3L);

        application = application.deregister().deregister();
        assertThat(application.isRegistered()).isFalse();
        assertThat(application.getRegistration()).isNull();
        assertThat(application.getInfo()).isEqualTo(Info.empty());
        assertThat(application.getStatusInfo()).isEqualTo(StatusInfo.ofUnknown());
        assertThat(application.getVersion()).isEqualTo(4L);

        assertThat(application.getUnsavedEvents().stream().map(ClientApplicationEvent::getType)).containsExactly(
                "REGISTERED", "REGISTRATION_UPDATED", "STATUS_CHANGED", "INFO_CHANGED", "DEREGISTERED");
    }

    @Test
    public void should_yield_same_status_from_replaying() {
        Registration registration = Registration.create("foo", "http://health").build();
        Registration registration2 = Registration.create("foo2", "http://health").build();
        Application application = Application.create(ApplicationId.of("id"))
                                             .register(registration)
                                             .register(registration2).withEndpoints(Endpoints.single("info", "info"))
                                             .withStatusInfo(StatusInfo.ofUp())
                                             .withInfo(Info.from(Collections.singletonMap("foo", "bar")));

        Application loaded = Application.create(ApplicationId.of("id")).loadHistory(application.getUnsavedEvents());
        assertThat(loaded.getUnsavedEvents()).isEmpty();
        assertThat(loaded.getRegistration()).isEqualTo(registration2);
        assertThat(loaded.isRegistered()).isTrue();
        assertThat(loaded.getStatusInfo()).isEqualTo(StatusInfo.ofUp());
        assertThat(loaded.getInfo()).isEqualTo(Info.from(Collections.singletonMap("foo", "bar")));
        assertThat(loaded.getEndpoints()).isEqualTo(Endpoints.single("info", "info"));
        assertThat(loaded.getVersion()).isEqualTo(4L);

        loaded = Application.create(ApplicationId.of("id")).loadHistory(application.deregister().getUnsavedEvents());
        assertThat(loaded.getUnsavedEvents()).isEmpty();
        assertThat(loaded.isRegistered()).isFalse();
        assertThat(loaded.getRegistration()).isNull();
        assertThat(loaded.getInfo()).isEqualTo(Info.empty());
        assertThat(loaded.getStatusInfo()).isEqualTo(StatusInfo.ofUnknown());
        assertThat(loaded.getEndpoints()).isEqualTo(Endpoints.empty());
        assertThat(loaded.getVersion()).isEqualTo(5L);
    }

    @Test
    public void should_throw_when_applied_wrong_event() {
        Application application = Application.create(ApplicationId.of("id"));
        assertThatThrownBy(() -> application.apply(null, true)).isInstanceOf(IllegalArgumentException.class)
                                                               .hasMessage("'event' must not be null");

        assertThatThrownBy(
                () -> application.apply(new ClientApplicationDeregisteredEvent(ApplicationId.of("wrong"), 0L),
                        true)).isInstanceOf(IllegalArgumentException.class)
                              .hasMessage("'event' must be for the same application");

        assertThatThrownBy(() -> application.apply(new ClientApplicationDeregisteredEvent(ApplicationId.of("id"), 1L),
                true)).isInstanceOf(IllegalArgumentException.class).hasMessage("expected event version doesn't match");
    }

}