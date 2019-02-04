/*
 * Copyright 2014-2019 the original author or authors.
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

package de.codecentric.boot.admin.server.cloud.discovery;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
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
 * Listener for Heartbeats events to publish all services to the instance registry.
 *
 * @author Johannes Edmeier
 */
public class InstanceDiscoveryListener {
    private static final Logger log = LoggerFactory.getLogger(InstanceDiscoveryListener.class);
    private static final String SOURCE = "discovery";
    private final DiscoveryClient discoveryClient;
    private final InstanceRegistry registry;
    private final InstanceRepository repository;
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

    public InstanceDiscoveryListener(DiscoveryClient discoveryClient,
                                     InstanceRegistry registry,
                                     InstanceRepository repository) {
        this.discoveryClient = discoveryClient;
        this.registry = registry;
        this.repository = repository;
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        discover();
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
        Flux.fromIterable(discoveryClient.getServices())
            .filter(this::shouldRegisterService)
            .flatMapIterable(discoveryClient::getInstances)
            .flatMap(this::registerInstance)
            .collect(Collectors.toSet())
            .flatMap(this::removeStaleInstances)
            .subscribe(v -> { }, ex -> log.error("Unexpected error.", ex));
    }

    protected Mono<Void> removeStaleInstances(Set<InstanceId> registeredInstanceIds) {
        return repository.findAll()
                         .filter(Instance::isRegistered)
                         .filter(instance -> SOURCE.equals(instance.getRegistration().getSource()))
                         .map(Instance::getId)
                         .filter(id -> !registeredInstanceIds.contains(id))
                         .doOnNext(id -> log.info("Instance ({}) missing in DiscoveryClient services ", id))
                         .flatMap(registry::deregister)
                         .then();
    }

    protected boolean shouldRegisterService(final String serviceId) {
        boolean shouldRegister = matchesPattern(serviceId, services) && !matchesPattern(serviceId, ignoredServices);
        if (!shouldRegister) {
            log.debug("Ignoring discovered service {}", serviceId);
        }
        return shouldRegister;
    }

    protected boolean matchesPattern(String serviceId, Set<String> patterns) {
        return patterns.stream().anyMatch(pattern -> PatternMatchUtils.simpleMatch(pattern, serviceId));
    }

    protected Mono<InstanceId> registerInstance(ServiceInstance instance) {
        try {
            Registration registration = converter.convert(instance).toBuilder().source(SOURCE).build();
            log.debug("Registering discovered instance {}", registration);
            return registry.register(registration);
        } catch (Exception ex) {
            log.error("Couldn't register instance for service ({})", toString(instance), ex);
        }
        return Mono.empty();
    }

    protected String toString(ServiceInstance instance) {
        String httpScheme = instance.isSecure() ? "https" : "http";
        return String.format("serviceId=%s, instanceId=%s, url= %s://%s:%d",
            instance.getServiceId(), instance.getInstanceId(),
            instance.getScheme() != null ? instance.getScheme() : httpScheme,
            instance.getHost(),
            instance.getPort()
        );
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
