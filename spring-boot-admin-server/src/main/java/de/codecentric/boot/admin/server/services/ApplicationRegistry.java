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

package de.codecentric.boot.admin.server.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.BuildVersion;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;

import static de.codecentric.boot.admin.server.domain.values.StatusInfo.STATUS_UNKNOWN;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Registry for all applications that should be managed/administrated by the Spring Boot
 * Admin server. Backed by an InstanceRegistry for persistence and an
 * InstanceEventPublisher for events
 *
 * @author Dean de Bree
 */
public class ApplicationRegistry {

	private final InstanceRegistry instanceRegistry;

	private final InstanceEventPublisher instanceEventPublisher;

	public ApplicationRegistry(InstanceRegistry instanceRegistry, InstanceEventPublisher instanceEventPublisher) {
		this.instanceRegistry = instanceRegistry;
		this.instanceEventPublisher = instanceEventPublisher;
	}

	/**
	 * Get a list of all registered applications.
	 * @return flux of all the applications.
	 */
	public Flux<Application> getApplications() {
		return this.instanceRegistry.getInstances()
			.filter(Instance::isRegistered)
			.groupBy((instance) -> instance.getRegistration().getName())
			.flatMap((grouped) -> toApplication(grouped.key(), grouped), Integer.MAX_VALUE);
	}

	/**
	 * Get a specific application instance.
	 * @param name the name of the application to find.
	 * @return a Mono with the application or an empty Mono if not found.
	 */
	public Mono<Application> getApplication(String name) {
		return this.toApplication(name, this.instanceRegistry.getInstances(name).filter(Instance::isRegistered))
			.filter((a) -> !a.getInstances().isEmpty());
	}

	public Flux<Application> getApplicationStream() {
		return Flux.from(this.instanceEventPublisher)
			.flatMap((event) -> this.instanceRegistry.getInstance(event.getInstance()))
			.map(this::getApplicationForInstance)
			.flatMap((group) -> toApplication(group.getT1(), group.getT2()));
	}

	public Flux<InstanceId> deregister(String name) {
		return this.instanceRegistry.getInstances(name)
			.flatMap((instance) -> this.instanceRegistry.deregister(instance.getId()));
	}

	protected Tuple2<String, Flux<Instance>> getApplicationForInstance(Instance instance) {
		String name = instance.getRegistration().getName();
		return Tuples.of(name, this.instanceRegistry.getInstances(name).filter(Instance::isRegistered));
	}

	protected Mono<Application> toApplication(String name, Flux<Instance> instances) {
		return instances.collectList().map((instanceList) -> {
			Tuple2<String, Instant> status = getStatus(instanceList);
			return Application.create(name)
				.instances(instanceList)
				.buildVersion(getBuildVersion(instanceList))
				.status(status.getT1())
				.statusTimestamp(status.getT2())
				.build();
		});
	}

	@Nullable
	protected BuildVersion getBuildVersion(List<Instance> instances) {
		List<BuildVersion> versions = instances.stream()
			.map(Instance::getBuildVersion)
			.filter(Objects::nonNull)
			.distinct()
			.sorted()
			.collect(toList());
		if (versions.isEmpty()) {
			return null;
		}
		else if (versions.size() == 1) {
			return versions.get(0);
		}
		else {
			return BuildVersion.valueOf(versions.get(0) + " ... " + versions.get(versions.size() - 1));
		}
	}

	protected Tuple2<String, Instant> getStatus(List<Instance> instances) {
		// TODO: Correct is just a second readmodel for groups
		Map<String, Instant> statusWithTime = instances.stream()
			.collect(toMap((instance) -> instance.getStatusInfo().getStatus(), Instance::getStatusTimestamp,
					this::getMax));
		if (statusWithTime.size() == 1) {
			Map.Entry<String, Instant> e = statusWithTime.entrySet().iterator().next();
			return Tuples.of(e.getKey(), e.getValue());
		}

		if (statusWithTime.containsKey(StatusInfo.STATUS_UP)) {
			Instant oldestNonUp = statusWithTime.entrySet()
				.stream()
				.filter((e) -> !StatusInfo.STATUS_UP.equals(e.getKey()))
				.map(Map.Entry::getValue)
				.min(naturalOrder())
				.orElse(Instant.EPOCH);
			Instant latest = getMax(oldestNonUp, statusWithTime.getOrDefault(StatusInfo.STATUS_UP, Instant.EPOCH));
			return Tuples.of(StatusInfo.STATUS_RESTRICTED, latest);
		}

		return statusWithTime.entrySet()
			.stream()
			.min(Map.Entry.comparingByKey(StatusInfo.severity()))
			.map((e) -> Tuples.of(e.getKey(), e.getValue()))
			.orElse(Tuples.of(STATUS_UNKNOWN, Instant.EPOCH));
	}

	protected Instant getMax(Instant t1, Instant t2) {
		return (t1.compareTo(t2) >= 0) ? t1 : t2;
	}

}
