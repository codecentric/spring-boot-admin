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

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.ApplicationRepository;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.services.ApplicationRegistry;
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
 * Listener for Heartbeats events to publish all services to the application registry.
 *
 * @author Johannes Edmeier
 */
public class ApplicationDiscoveryListener {
    private static final Logger log = LoggerFactory.getLogger(ApplicationDiscoveryListener.class);
    private static final String SOURCE = "discovery";
    private final DiscoveryClient discoveryClient;
    private final ApplicationRegistry registry;
    private final ApplicationRepository repository;
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

    public ApplicationDiscoveryListener(DiscoveryClient discoveryClient,
                                        ApplicationRegistry registry,
                                        ApplicationRepository repository) {
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
            .subscribe(v -> {}, ex -> log.error("Unexpected error.", ex));
    }

    protected Mono<Void> removeStaleInstances(Set<ApplicationId> registeredApplicationIds) {
        return repository.findAll()
                         .filter(application -> SOURCE.equals(application.getRegistration().getSource()))
                         .map(Application::getId)
                         .filter(id -> !registeredApplicationIds.contains(id))
                         .doOnNext(id -> log.info("Application ({}) missing in DiscoveryClient services ", id))
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

    protected Mono<ApplicationId> registerInstance(ServiceInstance instance) {
        try {
            Registration registration = converter.convert(instance).toBuilder().source(SOURCE).build();
            log.debug("Registering discovered application {}", registration);
            return registry.register(registration);
        } catch (Exception ex) {
            log.error("Couldn't register application for service {}", instance, ex);
        }
        return Mono.empty();
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
