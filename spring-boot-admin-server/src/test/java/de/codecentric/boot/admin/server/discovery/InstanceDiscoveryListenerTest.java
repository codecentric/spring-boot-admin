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
package de.codecentric.boot.admin.server.discovery;

import de.codecentric.boot.admin.server.domain.entities.EventSourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.services.HashingInstanceUrlIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InstanceDiscoveryListenerTest {
    private InstanceDiscoveryListener listener;
    private DiscoveryClient discovery;
    private InstanceRegistry registry;

    @Before
    public void setup() {
        discovery = mock(DiscoveryClient.class);
        EventSourcingInstanceRepository repository = new EventSourcingInstanceRepository(new InMemoryEventStore());
        repository.start();
        registry = new InstanceRegistry(repository, new HashingInstanceUrlIdGenerator());
        listener = new InstanceDiscoveryListener(discovery, registry, repository);
    }

    @Test
    public void test_application_ready() {
        when(discovery.getServices()).thenReturn(Collections.singletonList("service"));
        when(discovery.getInstances("service")).thenReturn(
                Collections.singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.onApplicationReady(null);

        StepVerifier.create(registry.getInstances())
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
                    .verifyComplete();
    }


    @Test
    public void test_ignore() {
        when(discovery.getServices()).thenReturn(singletonList("service"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.setIgnoredServices(singleton("service"));
        listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

        StepVerifier.create(registry.getInstances()).verifyComplete();
    }

    @Test
    public void test_matching() {
        when(discovery.getServices()).thenReturn(singletonList("service"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.setServices(singleton("notService"));
        listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

        StepVerifier.create(registry.getInstances()).verifyComplete();
    }

    @Test
    public void test_ignore_pattern() {
        when(discovery.getServices()).thenReturn(asList("service", "rabbit-1", "rabbit-2"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.setIgnoredServices(singleton("rabbit-*"));
        listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

        StepVerifier.create(registry.getInstances())
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
                    .verifyComplete();
    }

    @Test
    public void test_matching_pattern() {
        when(discovery.getServices()).thenReturn(asList("service", "rabbit-1", "rabbit-2"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.setServices(singleton("ser*"));
        listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

        StepVerifier.create(registry.getInstances())
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
                    .verifyComplete();
    }

    @Test
    public void test_matching_and_ignore_pattern() {
        when(discovery.getServices()).thenReturn(asList("service-1", "service", "rabbit-1", "rabbit-2"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));
        when(discovery.getInstances("service-1")).thenReturn(
                singletonList(new DefaultServiceInstance("service-1", "localhost", 80, false)));

        listener.setServices(singleton("ser*"));
        listener.setIgnoredServices(singleton("service-*"));
        listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

        StepVerifier.create(registry.getInstances())
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
                    .verifyComplete();
    }

    @Test
    public void test_register_and_convert() {
        when(discovery.getServices()).thenReturn(singletonList("service"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

        StepVerifier.create(registry.getInstances()).assertNext(application -> {
            Registration registration = application.getRegistration();
            assertThat(registration.getHealthUrl()).isEqualTo("http://localhost:80/health");
            assertThat(registration.getManagementUrl()).isEqualTo("http://localhost:80/");
            assertThat(registration.getServiceUrl()).isEqualTo("http://localhost:80/");
            assertThat(registration.getName()).isEqualTo("service");
        }).verifyComplete();


    }

    @Test
    public void single_discovery_for_same_heartbeat() {
        Object heartbeat = new Object();
        listener.onParentHeartbeat(new ParentHeartbeatEvent(new Object(), heartbeat));

        when(discovery.getServices()).thenReturn(singletonList("service"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.onApplicationEvent(new HeartbeatEvent(new Object(), heartbeat));
        StepVerifier.create(registry.getInstances()).verifyComplete();

        listener.onApplicationEvent(new HeartbeatEvent(new Object(), new Object()));
        StepVerifier.create(registry.getInstances())
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
                    .verifyComplete();
    }

    @Test
    public void deregister_removed_app() {
        StepVerifier.create(registry.register(Registration.create("ignored", "http://health").build()))
                    .consumeNextWith((id) -> {})
                    .verifyComplete();
        StepVerifier.create(
                registry.register(Registration.create("different-source", "http://health2").source("http-api").build()))
                    .consumeNextWith((id) -> {})
                    .verifyComplete();
        listener.setIgnoredServices(singleton("ignored"));

        List<ServiceInstance> instances = new ArrayList<>();
        instances.add(new DefaultServiceInstance("service", "localhost", 80, false));
        instances.add(new DefaultServiceInstance("service", "example.net", 80, false));

        when(discovery.getServices()).thenReturn(singletonList("service"));
        when(discovery.getInstances("service")).thenReturn(instances);

        listener.onApplicationEvent(new HeartbeatEvent(new Object(), new Object()));

        StepVerifier.create(registry.getInstancesByApplication("service"))
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
                    .verifyComplete();

        StepVerifier.create(registry.getInstancesByApplication("ignored"))
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("ignored"))
                    .verifyComplete();

        StepVerifier.create(registry.getInstancesByApplication("different-source"))
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("different-source"))
                    .verifyComplete();


        instances.remove(0);

        listener.onApplicationEvent(new HeartbeatEvent(new Object(), new Object()));
        StepVerifier.create(registry.getInstancesByApplication("service"))
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("service"))
                    .verifyComplete();

        StepVerifier.create(registry.getInstancesByApplication("ignored"))
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("ignored"))
                    .verifyComplete();

        StepVerifier.create(registry.getInstancesByApplication("different-source"))
                    .assertNext(a -> assertThat(a.getRegistration().getName()).isEqualTo("different-source"))
                    .verifyComplete();
    }

}
