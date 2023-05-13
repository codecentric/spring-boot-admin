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

package de.codecentric.boot.admin.server.domain.entities;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.values.BuildVersion;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

public class InstanceTest {

	@Test
	public void invariants() {
		assertThatThrownBy(() -> Instance.create(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'id' must not be null");

		assertThatThrownBy(() -> Instance.create(InstanceId.of("id")).register(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'registration' must not be null");

		assertThatThrownBy(() -> Instance.create(InstanceId.of("id")).withInfo(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'info' must not be null");

		assertThatThrownBy(() -> Instance.create(InstanceId.of("id")).withStatusInfo(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'statusInfo' must not be null");

		assertThatThrownBy(() -> Instance.create(InstanceId.of("id")).withEndpoints(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'endpoints' must not be null");
	}

	@Test
	public void should_track_unsaved_events() {
		Registration registration = Registration.create("foo", "http://health").build();
		Info info = Info.from(singletonMap("foo", "bar"));
		Instance newInstance = Instance.create(InstanceId.of("id"));

		assertThat(newInstance.isRegistered()).isFalse();
		assertThatThrownBy(newInstance::getRegistration).isInstanceOf(IllegalStateException.class);
		assertThat(newInstance.getInfo()).isEqualTo(Info.empty());
		assertThat(newInstance.getStatusInfo()).isEqualTo(StatusInfo.ofUnknown());
		assertThat(newInstance.getUnsavedEvents()).isEmpty();

		Instance instance = newInstance.register(registration).register(registration);
		assertThat(instance.getRegistration()).isEqualTo(registration);
		assertThat(instance.isRegistered()).isTrue();
		assertThat(instance.getVersion()).isEqualTo(0L);

		Registration registration2 = Registration.create("foo2", "http://health").build();
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
		assertThat(instance.getRegistration()).isEqualTo(registration2);
		assertThat(instance.getInfo()).isEqualTo(Info.empty());
		assertThat(instance.getStatusInfo()).isEqualTo(StatusInfo.ofUnknown());
		assertThat(instance.getVersion()).isEqualTo(4L);

		assertThat(instance.getUnsavedEvents().stream().map(InstanceEvent::getType)).containsExactly("REGISTERED",
				"REGISTRATION_UPDATED", "STATUS_CHANGED", "INFO_CHANGED", "DEREGISTERED");
	}

	@Test
	public void should_yield_same_status_from_replaying() {
		Registration registration = Registration.create("foo-instance", "http://health")
			.metadata("version", "1.0.0")
			.build();
		Instance instance = Instance.create(InstanceId.of("id"))
			.register(registration.toBuilder().clearMetadata().build())
			.register(registration)
			.withEndpoints(Endpoints.single("info", "info"))
			.withStatusInfo(StatusInfo.ofUp())
			.withInfo(Info.from(singletonMap("foo", "bar")));

		Instance loaded = Instance.create(InstanceId.of("id")).apply(instance.getUnsavedEvents());
		assertThat(loaded.getUnsavedEvents()).isEmpty();
		assertThat(loaded.getRegistration()).isEqualTo(registration);
		assertThat(loaded.isRegistered()).isTrue();
		assertThat(loaded.getStatusInfo()).isEqualTo(StatusInfo.ofUp());
		assertThat(loaded.getStatusTimestamp()).isEqualTo(instance.getStatusTimestamp());
		assertThat(loaded.getInfo()).isEqualTo(Info.from(singletonMap("foo", "bar")));
		assertThat(loaded.getEndpoints())
			.isEqualTo(Endpoints.single("info", "info").withEndpoint("health", "http://health"));
		assertThat(loaded.getVersion()).isEqualTo(4L);
		assertThat(loaded.getBuildVersion()).isEqualTo(BuildVersion.valueOf("1.0.0"));

		Instance deregisteredInstance = instance.deregister();
		loaded = Instance.create(InstanceId.of("id")).apply(deregisteredInstance.getUnsavedEvents());
		assertThat(loaded.getUnsavedEvents()).isEmpty();
		assertThat(loaded.isRegistered()).isFalse();
		assertThat(loaded.getInfo()).isEqualTo(Info.empty());
		assertThat(loaded.getStatusInfo()).isEqualTo(StatusInfo.ofUnknown());
		assertThat(loaded.getStatusTimestamp()).isEqualTo(deregisteredInstance.getStatusTimestamp());
		assertThat(loaded.getEndpoints()).isEqualTo(Endpoints.empty());
		assertThat(loaded.getVersion()).isEqualTo(5L);
		assertThat(loaded.getBuildVersion()).isEqualTo(null);
	}

	@Test
	public void should_throw_when_applied_wrong_event() {
		Instance instance = Instance.create(InstanceId.of("id"));
		assertThatThrownBy(() -> instance.apply((InstanceEvent) null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'event' must not be null");

		assertThatThrownBy(() -> instance.apply(new InstanceDeregisteredEvent(InstanceId.of("wrong"), 0L)))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'event' must refer the same instance");

		assertThatThrownBy(() -> instance.apply(new InstanceDeregisteredEvent(InstanceId.of("id"), 1L))
			.apply(new InstanceDeregisteredEvent(InstanceId.of("id"), 1L))).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Event 1 must be greater or equal to 2");
	}

	@Test
	public void should_update_buildVersion() {
		Instance instance = Instance.create(InstanceId.of("id"));

		assertThat(instance.getBuildVersion()).isNull();

		Registration registration = Registration.create("foo-instance", "http://health")
			.metadata("version", "1.0.0")
			.build();
		instance = instance.register(registration).withInfo(Info.empty());
		assertThat(instance.getBuildVersion()).isEqualTo(BuildVersion.valueOf("1.0.0"));

		instance = instance.register(registration.toBuilder().clearMetadata().build());
		assertThat(instance.getBuildVersion()).isNull();

		instance = instance.withInfo(Info.from(singletonMap("build", singletonMap("version", "2.1.1"))));
		assertThat(instance.getBuildVersion()).isEqualTo(BuildVersion.valueOf("2.1.1"));

		instance = instance.deregister();
		assertThat(instance.getBuildVersion()).isNull();
	}

	@Test
	public void should_extract_tags() {
		Instance instance = Instance.create(InstanceId.of("id"));

		assertThat(instance.getTags().getValues()).isEmpty();

		Registration registration = Registration.create("foo-instance", "http://health")
			.metadata("tags.environment", "test")
			.metadata("tags.region", "EU")
			.build();

		instance = instance.register(registration);
		assertThat(instance.getTags().getValues()).containsExactly(entry("environment", "test"), entry("region", "EU"));

		instance = instance.withInfo(Info.from(singletonMap("tags", singletonMap("region", "US-East"))));
		assertThat(instance.getTags().getValues()).containsExactly(entry("environment", "test"),
				entry("region", "US-East"));

		instance = instance.deregister();
		assertThat(instance.getTags().getValues()).isEmpty();

		instance = instance.register(registration.toBuilder().clearMetadata().build());
		assertThat(instance.getTags().getValues()).isEmpty();
	}

	@Test
	public void shoud_rebuild_instance() {
		Instance instance = Instance.create(InstanceId.of("id"))
			.register(Registration.create("test", "http://test").build())
			.withInfo(Info.from(singletonMap("info", "remove")))
			.withInfo(Info.from(singletonMap("info", "test2")));

		List<InstanceEvent> relevantEvents = instance.getUnsavedEvents()
			.stream()
			.filter((e) -> !(e instanceof InstanceInfoChangedEvent
					&& ((InstanceInfoChangedEvent) e).getInfo().getValues().get("info").equals("remove")))
			.collect(Collectors.toList());

		Instance rebuilt = Instance.create(InstanceId.of("id")).apply(relevantEvents);

		assertThat(rebuilt).isEqualTo(instance);
	}

}
