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

import java.util.HashSet;
import java.util.Set;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;
import org.springframework.context.event.EventListener;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;

/**
 * Listener for Heartbeats events to publish all services to the application registry.
 *
 * @author Johannes Edmeier
 */
public class ApplicationDiscoveryListener {
	private final DiscoveryClient discoveryClient;
	private final ApplicationRegistry registry;
	private final HeartbeatMonitor monitor = new HeartbeatMonitor();
	private ServiceInstanceConverter converter = new DefaultServiceInstanceConverter();

	/**
	 * Set of serviceIds to be ignored and not to be registered as application.
	 */
	private Set<String> ignoredServices = new HashSet<>();

	public ApplicationDiscoveryListener(DiscoveryClient discoveryClient,
			ApplicationRegistry registry) {
		this.discoveryClient = discoveryClient;
		this.registry = registry;
	}

	@EventListener
	public void onInstanceRegistered(InstanceRegisteredEvent<?> event) {
		discover();
	}

	@EventListener
	public void onParentHeartbeat(ParentHeartbeatEvent event) {
		discoverIfNeeded(event.getValue());
	}

	@EventListener
	public void onApplicationEvent(HeartbeatEvent event) {
		discoverIfNeeded(event.getValue());
	}

	private void discoverIfNeeded(Object value) {
		if (this.monitor.update(value)) {
			discover();
		}
	}

	protected void discover() {
		for (String serviceId : discoveryClient.getServices()) {
			for (ServiceInstance instance : discoveryClient.getInstances(serviceId)) {
				if (!ignoredServices.contains(serviceId)) {
					registry.register(convert(instance));
				}
			}
		}
	}

	protected Application convert(ServiceInstance instance) {
		return converter.convert(instance);
	}

	public void setConverter(ServiceInstanceConverter converter) {
		this.converter = converter;
	}

	public void setIgnoredServices(Set<String> ignoredServices) {
		this.ignoredServices = ignoredServices;
	}
}