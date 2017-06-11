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

import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.registry.ApplicationRegistry;
import de.codecentric.boot.admin.server.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.server.registry.store.SimpleApplicationStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationDiscoveryListenerTest {
    private ApplicationDiscoveryListener listener;
    private DiscoveryClient discovery;
    private ApplicationRegistry registry;

    @Before
    public void setup() {
        registry = new ApplicationRegistry(new SimpleApplicationStore(), new HashingApplicationUrlIdGenerator());
        registry.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));
        discovery = mock(DiscoveryClient.class);
        listener = new ApplicationDiscoveryListener(discovery, registry);
    }

    @Test
    public void test_ignore() {
        when(discovery.getServices()).thenReturn(singletonList("service"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.setIgnoredServices(singleton("service"));
        listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

        assertThat(registry.getApplications()).isEmpty();
    }

    @Test
    public void test_matching() {
        when(discovery.getServices()).thenReturn(singletonList("service"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.setServices(singleton("notService"));
        listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

        assertThat(registry.getApplications()).isEmpty();
    }

    @Test
    public void test_ignore_pattern() {
        when(discovery.getServices()).thenReturn(asList("service", "rabbit-1", "rabbit-2"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.setIgnoredServices(singleton("rabbit-*"));
        listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

        Collection<Application> applications = registry.getApplications();
        assertThat(applications).extracting(Application::getName).containsOnly("service");
    }

    @Test
    public void test_matching_pattern() {
        when(discovery.getServices()).thenReturn(asList("service", "rabbit-1", "rabbit-2"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.setServices(singleton("ser*"));
        listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

        Collection<Application> applications = registry.getApplications();
        assertThat(applications).extracting(Application::getName).containsOnly("service");
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

        Collection<Application> applications = registry.getApplications();
        assertThat(applications).extracting(Application::getName).containsOnly("service");
    }

    @Test
    public void test_register_and_convert() {
        when(discovery.getServices()).thenReturn(singletonList("service"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.onInstanceRegistered(new InstanceRegisteredEvent<>(new Object(), null));

        assertThat(registry.getApplications()).hasSize(1);
        Application application = registry.getApplications().iterator().next();

        assertThat(application.getHealthUrl()).isEqualTo("http://localhost:80/health");
        assertThat(application.getManagementUrl()).isEqualTo("http://localhost:80");
        assertThat(application.getServiceUrl()).isEqualTo("http://localhost:80");
        assertThat(application.getName()).isEqualTo("service");
    }

    @Test
    public void single_discovery_for_same_heartbeat() {
        Object heartbeat = new Object();
        listener.onParentHeartbeat(new ParentHeartbeatEvent(new Object(), heartbeat));

        when(discovery.getServices()).thenReturn(singletonList("service"));
        when(discovery.getInstances("service")).thenReturn(
                singletonList(new DefaultServiceInstance("service", "localhost", 80, false)));

        listener.onApplicationEvent(new HeartbeatEvent(new Object(), heartbeat));
        assertThat(registry.getApplications()).isEmpty();

        listener.onApplicationEvent(new HeartbeatEvent(new Object(), new Object()));
        assertThat(registry.getApplications()).hasSize(1);
    }

    @Test
    public void deregister_removed_app() {
        registry.register(Application.create("ignored").withHealthUrl("http://health").withId(ApplicationId.of("abcdef")).build());
        registry.register(Application.create("different-source")
                                     .withHealthUrl("http://health2")
                                     .withId(ApplicationId.of("abcdef"))
                                     .withSource("http-api")
                                     .build());
        listener.setIgnoredServices(singleton("ignored"));

        List<ServiceInstance> instances = new ArrayList<>();
        instances.add(new DefaultServiceInstance("service", "localhost", 80, false));
        instances.add(new DefaultServiceInstance("service", "example.net", 80, false));

        when(discovery.getServices()).thenReturn(singletonList("service"));
        when(discovery.getInstances("service")).thenReturn(instances);

        listener.onApplicationEvent(new HeartbeatEvent(new Object(), new Object()));
        assertThat(registry.getApplicationsByName("service")).hasSize(2);
        assertThat(registry.getApplicationsByName("ignored")).hasSize(1);
        assertThat(registry.getApplicationsByName("different-source")).hasSize(1);

        instances.remove(0);

        listener.onApplicationEvent(new HeartbeatEvent(new Object(), new Object()));
        assertThat(registry.getApplicationsByName("service")).hasSize(1);
        assertThat(registry.getApplicationsByName("ignored")).hasSize(1);
        assertThat(registry.getApplicationsByName("different-source")).hasSize(1);
    }

}
