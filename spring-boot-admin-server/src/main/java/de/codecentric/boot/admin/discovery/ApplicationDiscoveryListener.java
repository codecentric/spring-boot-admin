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
package de.codecentric.boot.admin.discovery;

import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.registry.ApplicationRegistry;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.cloud.client.discovery.event.HeartbeatMonitor;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.event.ParentHeartbeatEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.PatternMatchUtils;

/**
 * Listener for Heartbeats events to publish all services to the application registry.
 *
 * @author Johannes Edmeier
 */
public class ApplicationDiscoveryListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationDiscoveryListener.class);
    private static final String SOURCE = "discovery";
    private final DiscoveryClient discoveryClient;
    private final ApplicationRegistry registry;
    private final HeartbeatMonitor monitor = new HeartbeatMonitor();
    private ServiceInstanceConverter converter = new DefaultServiceInstanceConverter();

    /**
     * Set of serviceIds to be ignored and not to be registered as application. Supports simple
     * patterns (e.g. "foo*", "*foo", "foo*bar").
     */
    private Set<String> ignoredServices = new HashSet<>();

    /**
     * Set of serviceIds that has to match to be registered as application. Supports simple
     * patterns (e.g. "foo*", "*foo", "foo*bar"). Default value is everything
     */
    private Set<String> services = new HashSet<>(Collections.singletonList("*"));

    public ApplicationDiscoveryListener(DiscoveryClient discoveryClient, ApplicationRegistry registry) {
        this.discoveryClient = discoveryClient;
        this.registry = registry;
    }

    @EventListener
    public void onInstanceRegistered(InstanceRegisteredEvent<?> event) {
        discover();
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        discover();
    }

    @EventListener
    public void onParentHeartbeat(ParentHeartbeatEvent event) {
        discoverIfNeeded(event.getValue());
    }

    @EventListener
    public void onHeartbeat(HeartbeatEvent event) {
        discoverIfNeeded(event.getValue());
    }

    private void discoverIfNeeded(Object value) {
        if (this.monitor.update(value)) {
            discover();
        }
    }

    protected void discover() {
        final Set<String> staleApplicationIds = getAllApplicationIdsFromRegistry();
        for (String serviceId : discoveryClient.getServices()) {
            if (!ignoreService(serviceId) && registerService(serviceId)) {
                for (ServiceInstance instance : discoveryClient.getInstances(serviceId)) {
                    String applicationId = register(instance);
                    staleApplicationIds.remove(applicationId);
                }
            } else {
                LOGGER.debug("Ignoring discovered service {}", serviceId);
            }
        }
        for (String staleApplicationId : staleApplicationIds) {
            LOGGER.info("Application ({}) missing in DiscoveryClient services ", staleApplicationId);
            registry.deregister(staleApplicationId);
        }
    }

    protected boolean ignoreService(final String serviceId) {
        return checkPatternIsMatching(serviceId, ignoredServices);
    }

    protected boolean registerService(final String serviceId) {
        return checkPatternIsMatching(serviceId, services);
    }

    protected boolean checkPatternIsMatching(String serviceId, Set<String> patterns) {
        for (String pattern : patterns) {
            if (PatternMatchUtils.simpleMatch(pattern, serviceId)) {
                return true;
            }
        }
        return false;
    }

    protected final Set<String> getAllApplicationIdsFromRegistry() {
        Set<String> result = new HashSet<>();
        for (Application application : registry.getApplications()) {
            if (!ignoreService(application.getName()) &&
                registerService(application.getName()) &&
                SOURCE.equals(application.getSource())) {
                result.add(application.getId());
            }
        }
        return result;
    }

    protected String register(ServiceInstance instance) {
        try {
            Application application = converter.convert(instance);
            application = Application.copyOf(application).withSource(SOURCE).build();
            if (application != null) {
                LOGGER.debug("Registering discovered application {}", application);
                return registry.register(application).getId();
            } else {
                LOGGER.warn("No application for service {} registered", instance);
            }
        } catch (Exception ex) {
            LOGGER.error("Couldn't register application for service {}", instance, ex);
        }
        return null;
    }

    public void setConverter(ServiceInstanceConverter converter) {
        this.converter = converter;
    }

    public void setIgnoredServices(Set<String> ignoredServices) {
        this.ignoredServices = ignoredServices;
    }

    public Set<String> getIgnoredServices() {
        return ignoredServices;
    }

    public Set<String> getServices() {
        return services;
    }

    public void setServices(Set<String> services) {
        this.services = services;
    }
}
