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

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegistrationUpdatedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.BuildVersion;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.domain.values.Tags;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;

/**
 * The aggregate representing a registered application instance.
 *
 * @author Johannes Edmeier
 */
@lombok.Data
@lombok.EqualsAndHashCode(exclude = { "unsavedEvents", "statusTimestamp" })
@lombok.ToString(exclude = "unsavedEvents")
public final class Instance implements Serializable {

	private final InstanceId id;

	private final long version;

	@Nullable
	private final Registration registration;

	private final boolean registered;

	private final StatusInfo statusInfo;

	private final Instant statusTimestamp;

	private final Info info;

	private final List<InstanceEvent> unsavedEvents;

	private final Endpoints endpoints;

	@Nullable
	private final BuildVersion buildVersion;

	private final Tags tags;

	private Instance(InstanceId id) {
		this(id, -1L, null, false, StatusInfo.ofUnknown(), Instant.EPOCH, Info.empty(), Endpoints.empty(), null,
				Tags.empty(), emptyList());
	}

	private Instance(InstanceId id, long version, @Nullable Registration registration, boolean registered,
			StatusInfo statusInfo, Instant statusTimestamp, Info info, Endpoints endpoints,
			@Nullable BuildVersion buildVersion, Tags tags, List<InstanceEvent> unsavedEvents) {
		Assert.notNull(id, "'id' must not be null");
		Assert.notNull(endpoints, "'endpoints' must not be null");
		Assert.notNull(info, "'info' must not be null");
		Assert.notNull(statusInfo, "'statusInfo' must not be null");
		this.id = id;
		this.version = version;
		this.registration = registration;
		this.registered = registered;
		this.statusInfo = statusInfo;
		this.statusTimestamp = statusTimestamp;
		this.info = info;
		this.endpoints = (registered && (registration != null))
				? endpoints.withEndpoint(Endpoint.HEALTH, registration.getHealthUrl()) : endpoints;
		this.unsavedEvents = unsavedEvents;
		this.buildVersion = buildVersion;
		this.tags = tags;
	}

	public static Instance create(InstanceId id) {
		Assert.notNull(id, "'id' must not be null");
		return new Instance(id);
	}

	public Instance register(Registration registration) {
		Assert.notNull(registration, "'registration' must not be null");
		if (!this.isRegistered()) {
			return this.apply(new InstanceRegisteredEvent(this.id, this.nextVersion(), registration), true);
		}

		if (!Objects.equals(this.registration, registration)) {
			return this.apply(new InstanceRegistrationUpdatedEvent(this.id, this.nextVersion(), registration), true);
		}

		return this;
	}

	public Instance deregister() {
		if (this.isRegistered()) {
			return this.apply(new InstanceDeregisteredEvent(this.id, this.nextVersion()), true);
		}
		return this;
	}

	public Instance withInfo(Info info) {
		Assert.notNull(info, "'info' must not be null");
		if (Objects.equals(this.info, info)) {
			return this;
		}
		return this.apply(new InstanceInfoChangedEvent(this.id, this.nextVersion(), info), true);
	}

	public Instance withStatusInfo(StatusInfo statusInfo) {
		Assert.notNull(statusInfo, "'statusInfo' must not be null");
		if (Objects.equals(this.statusInfo.getStatus(), statusInfo.getStatus())) {
			return this;
		}
		return this.apply(new InstanceStatusChangedEvent(this.id, this.nextVersion(), statusInfo), true);
	}

	public Instance withEndpoints(Endpoints endpoints) {
		Assert.notNull(endpoints, "'endpoints' must not be null");
		Endpoints endpointsWithHealth = (this.registration != null)
				? endpoints.withEndpoint(Endpoint.HEALTH, this.registration.getHealthUrl()) : endpoints;
		if (Objects.equals(this.endpoints, endpointsWithHealth)) {
			return this;
		}
		return this.apply(new InstanceEndpointsDetectedEvent(this.id, this.nextVersion(), endpoints), true);
	}

	public boolean isRegistered() {
		return this.registered;
	}

	public Registration getRegistration() {
		if (this.registration == null) {
			throw new IllegalStateException("Application '" + this.id + "' has no valid registration.");
		}
		return this.registration;
	}

	List<InstanceEvent> getUnsavedEvents() {
		return unmodifiableList(this.unsavedEvents);
	}

	Instance clearUnsavedEvents() {
		return new Instance(this.id, this.version, this.registration, this.registered, this.statusInfo,
				this.statusTimestamp, this.info, this.endpoints, this.buildVersion, this.tags, emptyList());
	}

	Instance apply(Collection<InstanceEvent> events) {
		Assert.notNull(events, "'events' must not be null");
		Instance instance = this;
		for (InstanceEvent event : events) {
			instance = instance.apply(event);
		}
		return instance;
	}

	Instance apply(InstanceEvent event) {
		return this.apply(event, false);
	}

	private Instance apply(InstanceEvent event, boolean isNewEvent) {
		Assert.notNull(event, "'event' must not be null");
		Assert.isTrue(this.id.equals(event.getInstance()), "'event' must refer the same instance");
		Assert.isTrue(event.getVersion() >= this.nextVersion(),
				() -> "Event " + event.getVersion() + " must be greater or equal to " + this.nextVersion());

		List<InstanceEvent> unsavedEvents = appendToEvents(event, isNewEvent);

		if (event instanceof InstanceRegisteredEvent) {
			Registration registration = ((InstanceRegisteredEvent) event).getRegistration();
			return new Instance(this.id, event.getVersion(), registration, true, StatusInfo.ofUnknown(),
					event.getTimestamp(), Info.empty(), Endpoints.empty(),
					updateBuildVersion(registration.getMetadata()), updateTags(registration.getMetadata()),
					unsavedEvents);

		}
		else if (event instanceof InstanceRegistrationUpdatedEvent) {
			Registration registration = ((InstanceRegistrationUpdatedEvent) event).getRegistration();
			return new Instance(this.id, event.getVersion(), registration, this.registered, this.statusInfo,
					this.statusTimestamp, this.info, this.endpoints,
					updateBuildVersion(registration.getMetadata(), this.info.getValues()),
					updateTags(registration.getMetadata(), this.info.getValues()), unsavedEvents);

		}
		else if (event instanceof InstanceStatusChangedEvent) {
			StatusInfo statusInfo = ((InstanceStatusChangedEvent) event).getStatusInfo();
			return new Instance(this.id, event.getVersion(), this.registration, this.registered, statusInfo,
					event.getTimestamp(), this.info, this.endpoints, this.buildVersion, this.tags, unsavedEvents);

		}
		else if (event instanceof InstanceEndpointsDetectedEvent) {
			Endpoints endpoints = ((InstanceEndpointsDetectedEvent) event).getEndpoints();
			return new Instance(this.id, event.getVersion(), this.registration, this.registered, this.statusInfo,
					this.statusTimestamp, this.info, endpoints, this.buildVersion, this.tags, unsavedEvents);

		}
		else if (event instanceof InstanceInfoChangedEvent) {
			Info info = ((InstanceInfoChangedEvent) event).getInfo();
			Map<String, ?> metaData = (this.registration != null) ? this.registration.getMetadata() : emptyMap();
			return new Instance(this.id, event.getVersion(), this.registration, this.registered, this.statusInfo,
					this.statusTimestamp, info, this.endpoints, updateBuildVersion(metaData, info.getValues()),
					updateTags(metaData, info.getValues()), unsavedEvents);

		}
		else if (event instanceof InstanceDeregisteredEvent) {
			return new Instance(this.id, event.getVersion(), this.registration, false, StatusInfo.ofUnknown(),
					event.getTimestamp(), Info.empty(), Endpoints.empty(), null, Tags.empty(), unsavedEvents);
		}

		return this;
	}

	private long nextVersion() {
		return this.version + 1L;
	}

	private List<InstanceEvent> appendToEvents(InstanceEvent event, boolean isNewEvent) {
		if (!isNewEvent) {
			return this.unsavedEvents;
		}
		ArrayList<InstanceEvent> events = new ArrayList<>(this.unsavedEvents.size() + 1);
		events.addAll(this.unsavedEvents);
		events.add(event);
		return events;
	}

	@Nullable
	@SafeVarargs
	private final BuildVersion updateBuildVersion(Map<String, ?>... sources) {
		return Arrays.stream(sources).map(BuildVersion::from).filter(Objects::nonNull).findFirst().orElse(null);
	}

	@SafeVarargs
	private final Tags updateTags(Map<String, ?>... sources) {
		return Arrays.stream(sources).map((source) -> Tags.from(source, "tags")).reduce(Tags.empty(), Tags::append);
	}

}
