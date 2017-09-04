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

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import java.util.Collections;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InstanceTest {

    @Test
    public void invariants() {
        assertThatThrownBy(() -> Instance.create(null)).isInstanceOf(IllegalArgumentException.class)
                                                       .hasMessage("'id' must not be null");

        assertThatThrownBy(() -> Instance.create(InstanceId.of("id")).register(null)).isInstanceOf(
                IllegalArgumentException.class).hasMessage("'registration' must not be null");

        assertThatThrownBy(() -> Instance.create(InstanceId.of("id")).withInfo(null)).isInstanceOf(
                IllegalArgumentException.class).hasMessage("'info' must not be null");

        assertThatThrownBy(() -> Instance.create(InstanceId.of("id")).withStatusInfo(null)).isInstanceOf(
                IllegalArgumentException.class).hasMessage("'statusInfo' must not be null");

        assertThatThrownBy(() -> Instance.create(InstanceId.of("id")).withEndpoints(null)).isInstanceOf(
                IllegalArgumentException.class).hasMessage("'endpoints' must not be null");
    }

    @Test
    public void shoud_track_unsaved_events() {
        Registration registration = Registration.create("foo", "http://health").build();
        Registration registration2 = Registration.create("foo2", "http://health").build();
        Info info = Info.from(Collections.singletonMap("foo", "bar"));
        Instance instance = Instance.create(InstanceId.of("id"));

        assertThat(instance.isRegistered()).isFalse();
        assertThat(instance.getRegistration()).isNull();
        assertThat(instance.getInfo()).isEqualTo(Info.empty());
        assertThat(instance.getStatusInfo()).isEqualTo(StatusInfo.ofUnknown());
        assertThat(instance.getUnsavedEvents()).isEmpty();

        instance = instance.register(registration).register(registration);
        assertThat(instance.getRegistration()).isEqualTo(registration);
        assertThat(instance.isRegistered()).isTrue();
        assertThat(instance.getVersion()).isEqualTo(0L);

        instance = instance.register(registration2);
        assertThat(instance.getRegistration()).isEqualTo(registration2);
        assertThat(instance.isRegistered()).isTrue();
        assertThat(instance.getVersion()).isEqualTo(1L);

        instance = instance.withStatusInfo(StatusInfo.ofUp()).withStatusInfo(StatusInfo.ofUp());
        assertThat(instance.getStatusInfo()).isEqualTo(StatusInfo.ofUp());
        assertThat(instance.getVersion()).isEqualTo(2L);

        instance = instance.withInfo(info).withInfo(info);
        assertThat(instance.getInfo()).isEqualTo(info);
        assertThat(instance.getVersion()).isEqualTo(3L);

        instance = instance.deregister().deregister();
        assertThat(instance.isRegistered()).isFalse();
        assertThat(instance.getInfo()).isEqualTo(Info.empty());
        assertThat(instance.getStatusInfo()).isEqualTo(StatusInfo.ofUnknown());
        assertThat(instance.getVersion()).isEqualTo(4L);

        assertThat(instance.getUnsavedEvents().stream().map(InstanceEvent::getType)).containsExactly("REGISTERED",
                "REGISTRATION_UPDATED", "STATUS_CHANGED", "INFO_CHANGED", "DEREGISTERED");
    }

    @Test
    public void should_yield_same_status_from_replaying() {
        Registration registration = Registration.create("foo", "http://health").build();
        Registration registration2 = Registration.create("foo2", "http://health").build();
        Instance instance = Instance.create(InstanceId.of("id"))
                                    .register(registration)
                                    .register(registration2)
                                    .withEndpoints(Endpoints.single("info", "info"))
                                    .withStatusInfo(StatusInfo.ofUp())
                                    .withInfo(Info.from(Collections.singletonMap("foo", "bar")));

        Instance loaded = Instance.create(InstanceId.of("id")).loadHistory(instance.getUnsavedEvents());
        assertThat(loaded.getUnsavedEvents()).isEmpty();
        assertThat(loaded.getRegistration()).isEqualTo(registration2);
        assertThat(loaded.isRegistered()).isTrue();
        assertThat(loaded.getStatusInfo()).isEqualTo(StatusInfo.ofUp());
        assertThat(loaded.getStatusTimestamp()).isEqualTo(instance.getStatusTimestamp());
        assertThat(loaded.getInfo()).isEqualTo(Info.from(Collections.singletonMap("foo", "bar")));
        assertThat(loaded.getEndpoints()).isEqualTo(Endpoints.single("info", "info"));
        assertThat(loaded.getVersion()).isEqualTo(4L);

        Instance deregisteredInstance = instance.deregister();
        loaded = Instance.create(InstanceId.of("id")).loadHistory(deregisteredInstance.getUnsavedEvents());
        assertThat(loaded.getUnsavedEvents()).isEmpty();
        assertThat(loaded.isRegistered()).isFalse();
        assertThat(loaded.getInfo()).isEqualTo(Info.empty());
        assertThat(loaded.getStatusInfo()).isEqualTo(StatusInfo.ofUnknown());
        assertThat(loaded.getStatusTimestamp()).isEqualTo(deregisteredInstance.getStatusTimestamp());
        assertThat(loaded.getEndpoints()).isEqualTo(Endpoints.empty());
        assertThat(loaded.getVersion()).isEqualTo(5L);
    }

    @Test
    public void should_throw_when_applied_wrong_event() {
        Instance instance = Instance.create(InstanceId.of("id"));
        assertThatThrownBy(() -> instance.apply(null, true)).isInstanceOf(IllegalArgumentException.class)
                                                            .hasMessage("'event' must not be null");

        assertThatThrownBy(
                () -> instance.apply(new InstanceDeregisteredEvent(InstanceId.of("wrong"), 0L), true)).isInstanceOf(
                IllegalArgumentException.class).hasMessage("'event' must refer the same instance");

        assertThatThrownBy(
                () -> instance.apply(new InstanceDeregisteredEvent(InstanceId.of("id"), 1L), true)).isInstanceOf(
                IllegalArgumentException.class).hasMessage("expected event version doesn't match");
    }

}