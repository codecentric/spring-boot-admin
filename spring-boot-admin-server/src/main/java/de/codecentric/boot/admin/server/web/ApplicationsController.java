/*
 * Copyright 2014-2018 the original author or authors.
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

package de.codecentric.boot.admin.server.web;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.utils.ComparableVersion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * REST controller for controlling registration of managed instances.
 */
@AdminController
@ResponseBody
public class ApplicationsController {
    private static final Logger log = LoggerFactory.getLogger(ApplicationsController.class);
    private static final ServerSentEvent<?> PING = ServerSentEvent.builder().comment("ping").build();
    private static final Flux<ServerSentEvent<?>> PING_FLUX = Flux.interval(Duration.ZERO, Duration.ofSeconds(10L))
                                                                  .map(tick -> PING);
    private final InstanceRegistry registry;
    private final InstanceEventPublisher eventPublisher;

    public ApplicationsController(InstanceRegistry registry, InstanceEventPublisher eventPublisher) {
        this.registry = registry;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping(path = "/applications", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Application> applications() {
        return registry.getInstances()
                       .filter(Instance::isRegistered)
                       .groupBy(instance -> instance.getRegistration().getName())
                       .flatMap(grouped -> toApplication(grouped.key(), grouped));
    }

    @GetMapping(path = "/applications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Application>> applicationsStream() {
        return Flux.from(eventPublisher)
                   .flatMap(event -> registry.getInstance(event.getInstance()))
                   .map(this::getApplicationForInstance)
                   .flatMap(group -> toApplication(group.getT1(), group.getT2()))
                   .map(application -> ServerSentEvent.builder(application).build())
                   .mergeWith(ping());
    }

    @DeleteMapping(path = "/applications/{name}")
    public Mono<ResponseEntity<Void>> unregister(@PathVariable("name") String name) {
        log.debug("Unregister application with name '{}'", name);
        return registry.getInstances(name)
                       .flatMap(instance -> registry.deregister(instance.getId()))
                       .collectList()
                       .map(deregistered -> !deregistered.isEmpty() ?
                           ResponseEntity.noContent().build() :
                           ResponseEntity.notFound().build());
    }

    protected Tuple2<String, Flux<Instance>> getApplicationForInstance(Instance instance) {
        String name = instance.getRegistration().getName();
        return Tuples.of(name, registry.getInstances(name).filter(Instance::isRegistered));
    }

    protected Mono<Application> toApplication(String name, Flux<Instance> instances) {
        return instances.collectList().map(instanceList -> {
            Application group = new Application(name);
            group.setInstances(instanceList);
            group.setVersion(getVersion(instanceList));
            Tuple2<String, Instant> status = getStatus(instanceList);
            group.setStatus(status.getT1());
            group.setStatusTimestamp(status.getT2());
            return group;
        });
    }

    protected String getVersion(List<Instance> instances) {
        List<String> versions = instances.stream()
                                         .map(instance -> instance.getInfo().getVersion())
                                         .filter(StringUtils::hasText)
                                         .distinct()
                                         .sorted(ComparableVersion.ascending())
                                         .collect(toList());
        if (versions.isEmpty()) {
            return "";
        } else if (versions.size() == 1) {
            return versions.get(0);
        } else {
            return versions.get(0) + " - " + versions.get(versions.size() - 1);
        }
    }

    protected Tuple2<String, Instant> getStatus(List<Instance> instances) {
        //TODO: Correct is just a second readmodel for groups
        Map<String, Instant> statusWithTime = instances.stream()
                                                       .collect(toMap(instance -> instance.getStatusInfo().getStatus(),
                                                           Instance::getStatusTimestamp, this::getMax));
        if (statusWithTime.size() == 1) {
            Map.Entry<String, Instant> e = statusWithTime.entrySet().iterator().next();
            return Tuples.of(e.getKey(), e.getValue());
        }

        if (statusWithTime.containsKey(StatusInfo.STATUS_UP)) {
            Instant oldestNonUp = statusWithTime.entrySet()
                                                .stream()
                                                .filter(e -> !StatusInfo.STATUS_UP.equals(e.getKey()))
                                                .map(Map.Entry::getValue)
                                                .min(naturalOrder())
                                                .orElse(Instant.EPOCH);
            Instant latest = getMax(oldestNonUp, statusWithTime.getOrDefault(StatusInfo.STATUS_UP, Instant.EPOCH));
            return Tuples.of(StatusInfo.STATUS_RESTRICTED, latest);
        }

        return statusWithTime.entrySet()
                             .stream()
                             .min(Map.Entry.comparingByKey(StatusInfo.severity()))
                             .map(e -> Tuples.of(e.getKey(), e.getValue()))
                             .orElse(Tuples.of(StatusInfo.STATUS_UNKNOWN, Instant.EPOCH));
    }

    private Instant getMax(Instant t1, Instant t2) {
        return t1.compareTo(t2) >= 0 ? t1 : t2;
    }

    @SuppressWarnings("unchecked")
    private static <T> Flux<ServerSentEvent<T>> ping() {
        return (Flux<ServerSentEvent<T>>) (Flux) PING_FLUX;
    }

    @lombok.Data
    public static class Application {
        private final String name;
        private String version;
        private String status;
        private Instant statusTimestamp;
        private List<Instance> instances;
    }
}
