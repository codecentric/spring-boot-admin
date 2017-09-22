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

package de.codecentric.boot.admin.server.web;

import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEndpointsDetectedEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.services.ResubscribingEventHandler;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.PathRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import static org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory.REGEXP_KEY;
import static org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory.REPLACEMENT_KEY;
import static org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory.PATTERN_KEY;
import static org.springframework.cloud.gateway.support.NameUtils.normalizeFilterName;
import static org.springframework.cloud.gateway.support.NameUtils.normalizePredicateName;

public class InstanceRouteDefinitionLocator extends ResubscribingEventHandler<InstanceEvent> implements RouteDefinitionLocator, ApplicationEventPublisherAware {
    private final Map<InstanceId, List<RouteDefinition>> routes = new ConcurrentHashMap<>();
    private ApplicationEventPublisher applicationEventPublisher;

    public InstanceRouteDefinitionLocator(Publisher<InstanceEvent> publisher) {
        super(publisher, InstanceEvent.class);
    }

    @Override
    protected Publisher<?> handle(Flux<InstanceEvent> publisher) {
        return publisher.subscribeOn(Schedulers.newSingle("instance-route-definition-locator"))
                        .doOnNext(this::updateRoutes);
    }

    private void updateRoutes(InstanceEvent instanceEvent) {
        if (instanceEvent instanceof InstanceEndpointsDetectedEvent) {
            addRoutes((InstanceEndpointsDetectedEvent) instanceEvent);
            applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        } else if (instanceEvent instanceof InstanceDeregisteredEvent) {
            removeRoutes(instanceEvent);
            applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        }
    }

    private void addRoutes(InstanceEndpointsDetectedEvent event) {
        InstanceId instanceId = event.getInstance();
        List<RouteDefinition> instanceRoutes = new ArrayList<>();

        for (Endpoint endpoint : event.getEndpoints()) {
            RouteDefinition routeDefinition = new RouteDefinition();
            routeDefinition.setId(instanceId.toString() + "-" + endpoint.getId());
            URI endpointUri = URI.create(endpoint.getUrl());
            routeDefinition.setUri(endpointUri);
            String pathPrefix = "/instances/" + instanceId + "/" + endpoint.getId();

            PredicateDefinition predicate = new PredicateDefinition();
            predicate.setName(normalizePredicateName(PathRoutePredicateFactory.class));
            predicate.addArg(PATTERN_KEY, pathPrefix + "/**");
            routeDefinition.getPredicates().add(predicate);

            FilterDefinition filter = new FilterDefinition();
            filter.setName(normalizeFilterName(RewritePathGatewayFilterFactory.class));
            String regex = pathPrefix + "/(?<remaining>.*)";
            String replacement = endpointUri.getPath() + "/${remaining}";
            filter.addArg(REGEXP_KEY, regex);
            filter.addArg(REPLACEMENT_KEY, replacement);
            routeDefinition.getFilters().add(filter);

            instanceRoutes.add(routeDefinition);
        }

        routes.put(instanceId, instanceRoutes);
    }

    private void removeRoutes(InstanceEvent instanceEvent) {
        routes.remove(instanceEvent.getInstance());
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routes.values()).flatMapIterable(Function.identity());
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
