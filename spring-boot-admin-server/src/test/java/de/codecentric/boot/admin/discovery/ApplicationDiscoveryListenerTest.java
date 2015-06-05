/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.discovery;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;

public class ApplicationDiscoveryListenerTest {

	ApplicationDiscoveryListener listener;
	DiscoveryClient discovery;
	ApplicationRegistry registry;

	@Before
	public void setup() {
		registry = new ApplicationRegistry(new SimpleApplicationStore(),
				new HashingApplicationUrlIdGenerator());
		registry.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));
		discovery = mock(DiscoveryClient.class);
		listener = new ApplicationDiscoveryListener(discovery, registry);
	}

	@Test
	public void test_register_and_convert() {
		when(discovery.getServices()).thenReturn(Collections.singletonList("service"));
		when(discovery.getInstances("service")).thenReturn(
				Collections.<ServiceInstance> singletonList(new DefaultServiceInstance("service", "localhost", 80,
						false)));

		listener.onApplicationEvent(new InstanceRegisteredEvent<>(new Object(), null));

		assertEquals(1, registry.getApplications().size());
		Application application = registry.getApplications().iterator().next();

		assertEquals("http://localhost:80/health", application.getHealthUrl());
		assertEquals("http://localhost:80", application.getManagementUrl());
		assertEquals("http://localhost:80", application.getServiceUrl());
		assertEquals("service", application.getName());
	}

	@Test
	public void convert_mgmtContextPath() {
		when(discovery.getServices()).thenReturn(Collections.singletonList("service"));
		when(discovery.getInstances("service")).thenReturn(
				Collections.<ServiceInstance> singletonList(new DefaultServiceInstance("service", "localhost", 80,
						false)));

		listener.setManagementContextPath("/mgmt");
		listener.setServiceContextPath("/service");
		listener.setHealthEndpoint("alive");
		listener.onApplicationEvent(new InstanceRegisteredEvent<>(new Object(), null));

		assertEquals(1, registry.getApplications().size());
		Application application = registry.getApplications().iterator().next();

		assertEquals("http://localhost:80/mgmt/alive", application.getHealthUrl());
		assertEquals("http://localhost:80/mgmt", application.getManagementUrl());
		assertEquals("http://localhost:80/service", application.getServiceUrl());
		assertEquals("service", application.getName());
	}

	@Test
	public void single_discovery_for_same_heartbeat() {
		Object heartbeat = new Object();
		listener.onApplicationEvent(new ParentHeartbeatEvent(new Object(), heartbeat));

		when(discovery.getServices()).thenReturn(Collections.singletonList("service"));
		when(discovery.getInstances("service")).thenReturn(
				Collections.<ServiceInstance> singletonList(new DefaultServiceInstance("service", "localhost", 80,
						false)));

		listener.onApplicationEvent(new HeartbeatEvent(new Object(), heartbeat));
		assertEquals(0, registry.getApplications().size());

		listener.onApplicationEvent(new HeartbeatEvent(new Object(), new Object()));
		assertEquals(1, registry.getApplications().size());
	}

}
