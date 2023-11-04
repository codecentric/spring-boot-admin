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

package de.codecentric.boot.admin.server.cloud.discovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.codecentric.boot.admin.server.services.InstanceFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.services.HashingInstanceUrlIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceRegistry;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InstanceDiscoveryListenerTest {

	private InstanceDiscoveryListener listener;

	private DiscoveryClient discovery;

	private InstanceRegistry registry;

	@BeforeEach
	public void setup() {
		this.discovery = mock(DiscoveryClient.class);
		InstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());
		this.registry = spy(new InstanceRegistry(repository, new HashingInstanceUrlIdGenerator(), new InstanceFilter()));
		this.listener = new InstanceDiscoveryListener(this.discovery, this.registry, repository);
	}

	@Test
	public void should_discover_instances_when_application_is_ready() {
		when(this.discovery.getServices()).thenReturn(Collections.singletonList("service"));
		when(this.discovery.getInstances("service")).thenReturn(
				Collections.singletonList(new DefaultServiceInstance("test-1", "service", "localhost", 80, false)));

		this.listener.onApplicationReady(null);

		StepVerifier.create(this.registry.getInstances())
				.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service")).verifyComplete();
	}

	@Test
	public void should_not_register_instance_when_serviceId_is_ignored() {
		when(this.discovery.getServices()).thenReturn(singletonList("service"));
		when(this.discovery.getInstances("service"))
				.thenReturn(singletonList(new DefaultServiceInstance("test-1", "service", "localhost", 80, false)));

		this.listener.setIgnoredServices(singleton("service"));
		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances()).verifyComplete();
	}

	@Test
	public void should_not_register_instance_when_instanceMetadata_is_ignored() {
		when(this.discovery.getServices()).thenReturn(singletonList("service"));
		when(this.discovery.getInstances("service")).thenReturn(singletonList(new DefaultServiceInstance("test-1",
				"service", "localhost", 80, false, Collections.singletonMap("monitoring", "false"))));

		this.listener.setIgnoredInstancesMetadata(Collections.singletonMap("monitoring", "false"));
		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances()).verifyComplete();
	}

	@Test
	public void should_register_instance_when_serviceId_is_not_ignored() {
		when(this.discovery.getServices()).thenReturn(singletonList("service"));
		when(this.discovery.getInstances("service"))
				.thenReturn(singletonList(new DefaultServiceInstance("test-1", "service", "localhost", 80, false)));

		this.listener.setServices(singleton("notService2"));
		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances()).verifyComplete();
	}

	@Test
	public void should_register_instance_when_instanceMetadata_is_not_ignored() {
		when(this.discovery.getServices()).thenReturn(singletonList("service"));
		when(this.discovery.getInstances("service")).thenReturn(singletonList(new DefaultServiceInstance("test-1",
				"service", "localhost", 80, false, Collections.singletonMap("monitoring", "true"))));

		this.listener.setInstancesMetadata(Collections.singletonMap("monitoring", "false"));
		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances()).verifyComplete();
	}

	@Test
	public void should_not_register_instance_when_serviceId_matches_ignored_pattern() {
		when(this.discovery.getServices()).thenReturn(asList("service", "rabbit-1", "rabbit-2"));
		when(this.discovery.getInstances("service"))
				.thenReturn(singletonList(new DefaultServiceInstance("test-1", "service", "localhost", 80, false)));

		this.listener.setIgnoredServices(singleton("rabbit-*"));
		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances())
				.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service")).verifyComplete();
	}

	@Test
	public void should_not_register_instance_when_instanceMetadata_matches_ignored_metadata() {
		when(this.discovery.getServices()).thenReturn(asList("service", "rabbit-1", "rabbit-2"));
		when(this.discovery.getInstances("service")).thenReturn(singletonList(new DefaultServiceInstance("test-1",
				"service", "localhost", 80, false, Collections.singletonMap("monitoring", "true"))));
		when(this.discovery.getInstances("rabbit-1"))
				.thenReturn(singletonList(new DefaultServiceInstance("rabbit-test-1", "rabbit-1", "localhost", 80,
						false, Collections.singletonMap("monitoring", "false"))));
		when(this.discovery.getInstances("rabbit-2"))
				.thenReturn(singletonList(new DefaultServiceInstance("rabbit-test-1", "rabbit-2", "localhost", 80,
						false, Collections.singletonMap("monitoring", "false"))));

		this.listener.setIgnoredInstancesMetadata(Collections.singletonMap("monitoring", "false"));
		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances())
				.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service")).verifyComplete();
	}

	@Test
	public void should_register_instances_when_serviceId_matches_wanted_pattern() {
		when(this.discovery.getServices()).thenReturn(asList("service", "rabbit-1", "rabbit-2"));
		when(this.discovery.getInstances("service"))
				.thenReturn(singletonList(new DefaultServiceInstance("test-1", "service", "localhost", 80, false)));

		this.listener.setServices(singleton("ser*"));
		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances())
				.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service")).verifyComplete();
	}

	@Test
	public void should_register_instances_when_instanceMetadata_matches_wanted_metadata() {
		when(this.discovery.getServices()).thenReturn(asList("service", "rabbit-1", "rabbit-2"));
		when(this.discovery.getInstances("service")).thenReturn(singletonList(new DefaultServiceInstance("test-1",
				"service", "localhost", 80, false, Collections.singletonMap("monitoring", "true"))));
		when(this.discovery.getInstances("rabbit-1"))
				.thenReturn(singletonList(new DefaultServiceInstance("rabbit-test-1", "rabbit-1", "localhost", 80,
						false, Collections.singletonMap("monitoring", "false"))));
		when(this.discovery.getInstances("rabbit-2"))
				.thenReturn(singletonList(new DefaultServiceInstance("rabbit-test-1", "rabbit-2", "localhost", 80,
						false, Collections.singletonMap("monitoring", "false"))));

		this.listener.setInstancesMetadata(Collections.singletonMap("monitoring", "true"));
		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances())
				.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service")).verifyComplete();
	}

	@Test
	public void should_register_instances_when_serviceId_matches_wanted_pattern_and_ignored_pattern() {
		when(this.discovery.getServices()).thenReturn(asList("service-1", "service", "rabbit-1", "rabbit-2"));
		when(this.discovery.getInstances("service"))
				.thenReturn(singletonList(new DefaultServiceInstance("test-1", "service", "localhost", 80, false)));
		when(this.discovery.getInstances("service-1"))
				.thenReturn(singletonList(new DefaultServiceInstance("test-1", "service-1", "localhost", 80, false)));

		this.listener.setServices(singleton("ser*"));
		this.listener.setIgnoredServices(singleton("service-*"));
		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances())
				.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service")).verifyComplete();
	}

	@Test
	public void should_not_register_instances_when_instanceMetadata_matches_wanted_metadata_and_ignored_metadata() {
		when(this.discovery.getServices()).thenReturn(asList("service", "service-1"));
		when(this.discovery.getInstances("service")).thenReturn(singletonList(
				new DefaultServiceInstance("test-1", "service", "localhost", 80, false, new HashMap<String, String>() {
					{
						put("monitoring", "true");
						put("management", "true");
					}
				})));
		when(this.discovery.getInstances("service-1")).thenReturn(singletonList(new DefaultServiceInstance("test-1",
				"service-1", "localhost", 80, false, new HashMap<String, String>() {
					{
						put("monitoring", "true");
						put("management", "false");
					}
				})));

		this.listener.setInstancesMetadata(Collections.singletonMap("monitoring", "true"));
		this.listener.setIgnoredInstancesMetadata(Collections.singletonMap("management", "true"));
		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances())
				.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service-1")).verifyComplete();
	}

	@Test
	public void should_register_instance_when_new_service_instance_is_discovered() {
		when(this.discovery.getServices()).thenReturn(singletonList("service"));
		when(this.discovery.getInstances("service"))
				.thenReturn(singletonList(new DefaultServiceInstance("test-1", "service", "localhost", 80, false)));

		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances()).assertNext((application) -> {
			Registration registration = application.getRegistration();
			assertThat(registration.getHealthUrl()).isEqualTo("http://localhost:80/actuator/health");
			assertThat(registration.getManagementUrl()).isEqualTo("http://localhost:80/actuator");
			assertThat(registration.getServiceUrl()).isEqualTo("http://localhost:80");
			assertThat(registration.getName()).isEqualTo("service");
		}).verifyComplete();
	}

	@Test
	public void should_only_discover_new_instances_when_new_heartbeat_is_emitted() {
		Object heartbeat = new Object();
		this.listener.onParentHeartbeat(new ParentHeartbeatEvent(new Object(), heartbeat));

		when(this.discovery.getServices()).thenReturn(singletonList("service"));
		when(this.discovery.getInstances("service"))
			.thenReturn(singletonList(new DefaultServiceInstance("test-1", "service", "localhost", 80, false)));

		this.listener.onApplicationEvent(new HeartbeatEvent(new Object(), heartbeat));
		StepVerifier.create(this.registry.getInstances()).verifyComplete();

		this.listener.onApplicationEvent(new HeartbeatEvent(new Object(), new Object()));
		StepVerifier.create(this.registry.getInstances())
			.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
			.verifyComplete();
	}

	@Test
	public void should_remove_instances_when_they_are_no_longer_available_in_discovery() {
		StepVerifier.create(this.registry.register(Registration.create("ignored", "http://health").build()))
			.consumeNextWith((id) -> {
			})
			.verifyComplete();
		StepVerifier
			.create(this.registry
				.register(Registration.create("different-source", "http://health2").source("http-api").build()))
			.consumeNextWith((id) -> {
			})
			.verifyComplete();
		this.listener.setIgnoredServices(singleton("ignored"));

		List<ServiceInstance> instances = new ArrayList<>();
		instances.add(new DefaultServiceInstance("test-1", "service", "localhost", 80, false));
		instances.add(new DefaultServiceInstance("test-1", "service", "example.net", 80, false));

		when(this.discovery.getServices()).thenReturn(singletonList("service"));
		when(this.discovery.getInstances("service")).thenReturn(instances);

		this.listener.onApplicationEvent(new HeartbeatEvent(new Object(), new Object()));

		StepVerifier.create(this.registry.getInstances("service"))
			.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
			.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
			.verifyComplete();

		StepVerifier.create(this.registry.getInstances("ignored"))
			.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("ignored"))
			.verifyComplete();

		StepVerifier.create(this.registry.getInstances("different-source"))
			.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("different-source"))
			.verifyComplete();

		instances.remove(0);

		this.listener.onApplicationEvent(new HeartbeatEvent(new Object(), new Object()));

		StepVerifier.create(this.registry.getInstances("service"))
			.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
			.verifyComplete();

		StepVerifier.create(this.registry.getInstances("ignored"))
			.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("ignored"))
			.verifyComplete();

		StepVerifier.create(this.registry.getInstances("different-source"))
			.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("different-source"))
			.verifyComplete();

		// shouldn't deregister a second time
		this.listener.onApplicationEvent(new HeartbeatEvent(new Object(), new Object()));
		verify(this.registry, times(1)).deregister(any(InstanceId.class));
	}

	@Test
	public void should_not_throw_error_when_conversion_fails_and_proceed_with_next_instance() {
		when(this.discovery.getServices()).thenReturn(singletonList("service"));
		when(this.discovery.getInstances("service"))
				.thenReturn(asList(new DefaultServiceInstance("test-1", "service", "localhost", 80, false),
						new DefaultServiceInstance("error-1", "error", "localhost", 80, false)));
		this.listener.setConverter((instance) -> {
			if (instance.getServiceId().equals("error")) {
				throw new IllegalStateException("Test-Error");
			}
			else {
				return new DefaultServiceInstanceConverter().convert(instance);
			}
		});

		this.listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

		StepVerifier.create(this.registry.getInstances())
				.assertNext((a) -> assertThat(a.getRegistration().getName()).isEqualTo("service")).verifyComplete();
	}

}
