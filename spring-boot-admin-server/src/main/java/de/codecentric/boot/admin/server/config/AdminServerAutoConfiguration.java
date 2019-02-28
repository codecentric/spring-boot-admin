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

package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.SnapshottingInstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.EndpointDetectionTrigger;
import de.codecentric.boot.admin.server.services.EndpointDetector;
import de.codecentric.boot.admin.server.services.HashingInstanceUrlIdGenerator;
import de.codecentric.boot.admin.server.services.InfoUpdateTrigger;
import de.codecentric.boot.admin.server.services.InfoUpdater;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.services.StatusUpdateTrigger;
import de.codecentric.boot.admin.server.services.StatusUpdater;
import de.codecentric.boot.admin.server.services.endpoints.ChainingStrategy;
import de.codecentric.boot.admin.server.services.endpoints.ProbeEndpointsStrategy;
import de.codecentric.boot.admin.server.services.endpoints.QueryIndexEndpointStrategy;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConditionalOnBean(AdminServerMarkerConfiguration.Marker.class)
@EnableConfigurationProperties(AdminServerProperties.class)
@Import({AdminServerInstanceWebClientConfiguration.class, AdminServerWebConfiguration.class})
@AutoConfigureAfter({WebClientAutoConfiguration.class})
public class AdminServerAutoConfiguration {
    private final AdminServerProperties adminServerProperties;

    public AdminServerAutoConfiguration(AdminServerProperties adminServerProperties) {
        this.adminServerProperties = adminServerProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public InstanceRegistry instanceRegistry(InstanceRepository instanceRepository,
                                             InstanceIdGenerator instanceIdGenerator) {
        return new InstanceRegistry(instanceRepository, instanceIdGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public InstanceIdGenerator instanceIdGenerator() {
        return new HashingInstanceUrlIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public StatusUpdater statusUpdater(InstanceRepository instanceRepository,
                                       InstanceWebClient.Builder instanceWebClientBulder) {
        return new StatusUpdater(instanceRepository, instanceWebClientBulder.build());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public StatusUpdateTrigger statusUpdateTrigger(StatusUpdater statusUpdater, Publisher<InstanceEvent> events) {
        StatusUpdateTrigger trigger = new StatusUpdateTrigger(statusUpdater, events);
        trigger.setInterval(this.adminServerProperties.getMonitor().getStatusInterval());
        trigger.setLifetime(this.adminServerProperties.getMonitor().getStatusLifetime());
        return trigger;
    }

    @Bean
    @ConditionalOnMissingBean
    public EndpointDetector endpointDetector(InstanceRepository instanceRepository,
                                             InstanceWebClient.Builder instanceWebClientBuilder) {
        InstanceWebClient instanceWebClient = instanceWebClientBuilder.build();
        ChainingStrategy strategy = new ChainingStrategy(
            new QueryIndexEndpointStrategy(instanceWebClient),
            new ProbeEndpointsStrategy(instanceWebClient, this.adminServerProperties.getProbedEndpoints())
        );
        return new EndpointDetector(instanceRepository, strategy);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public EndpointDetectionTrigger endpointDetectionTrigger(EndpointDetector endpointDetector,
                                                             Publisher<InstanceEvent> events) {
        return new EndpointDetectionTrigger(endpointDetector, events);
    }

    @Bean
    @ConditionalOnMissingBean
    public InfoUpdater infoUpdater(InstanceRepository instanceRepository,
                                   InstanceWebClient.Builder instanceWebClientBuilder) {
        return new InfoUpdater(instanceRepository, instanceWebClientBuilder.build());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public InfoUpdateTrigger infoUpdateTrigger(InfoUpdater infoUpdater, Publisher<InstanceEvent> events) {
        InfoUpdateTrigger trigger = new InfoUpdateTrigger(infoUpdater, events);
        trigger.setInterval(this.adminServerProperties.getMonitor().getInfoInterval());
        trigger.setLifetime(this.adminServerProperties.getMonitor().getInfoLifetime());
        return trigger;
    }

    @Bean
    @ConditionalOnMissingBean(InstanceEventStore.class)
    public InMemoryEventStore eventStore() {
        return new InMemoryEventStore();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(InstanceRepository.class)
    public SnapshottingInstanceRepository instanceRepository(InstanceEventStore eventStore) {
        return new SnapshottingInstanceRepository(eventStore);
    }
}
