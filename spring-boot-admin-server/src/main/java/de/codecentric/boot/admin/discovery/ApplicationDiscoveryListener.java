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
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;

/**
 * Listener for Heartbeats events to publish all services to the application registry.
 * @author Johannes Stelzer
 */
public class ApplicationDiscoveryListener implements ApplicationListener<ApplicationEvent> {

	private final DiscoveryClient discoveryClient;

	private final ApplicationRegistry registry;

	private final HeartbeatMonitor monitor = new HeartbeatMonitor();

	private String managementContextPath = "";

	private String serviceContextPath = "";

	private String healthEndpoint = "health";


	public ApplicationDiscoveryListener(DiscoveryClient discoveryClient, ApplicationRegistry registry) {
		this.discoveryClient = discoveryClient;
		this.registry = registry;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof InstanceRegisteredEvent) {
			discover();
		}
		else if (event instanceof ParentHeartbeatEvent) {
			ParentHeartbeatEvent e = (ParentHeartbeatEvent) event;
			discoverIfNeeded(e.getValue());
		}
		else if (event instanceof HeartbeatEvent) {
			HeartbeatEvent e = (HeartbeatEvent) event;
			discoverIfNeeded(e.getValue());
		}

	}

	private void discoverIfNeeded(Object value) {
		if (this.monitor.update(value)) {
			discover();
		}
	}

	public void discover() {
		for (String serviceId : discoveryClient.getServices()) {
			for (ServiceInstance instance : discoveryClient.getInstances(serviceId)) {
				registry.register(convert(instance));
			}
		}
	}

	private Application convert(ServiceInstance instance) {
		String managementUrl = instance.getUri()
				.resolve(managementContextPath)
				.toString();
		String serviceUrl = instance.getUri()
				.resolve(serviceContextPath)
				.toString();
		String healthUrl = managementUrl + "/" + healthEndpoint;

		return new Application(healthUrl, managementUrl, serviceUrl, instance.getServiceId());
	}

	public void setManagementContextPath(String managementContextPath) {
		this.managementContextPath = managementContextPath.startsWith("/") ? managementContextPath
				: "/" + managementContextPath;
	}

	public void setServiceContextPath(String serviceContextPath) {
		this.serviceContextPath = serviceContextPath.startsWith("/") ? serviceContextPath
				: "/" + serviceContextPath;
	}

	public void setHealthEndpoint(String healthEndpoint) {
		this.healthEndpoint = healthEndpoint;
	}
}