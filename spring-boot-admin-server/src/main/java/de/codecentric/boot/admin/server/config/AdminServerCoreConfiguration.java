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
package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.eventstore.ClientApplicationEventStore;
import de.codecentric.boot.admin.server.eventstore.SimpleEventStore;
import de.codecentric.boot.admin.server.registry.ApplicationIdGenerator;
import de.codecentric.boot.admin.server.registry.ApplicationRegistry;
import de.codecentric.boot.admin.server.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.server.registry.InfoUpdateTrigger;
import de.codecentric.boot.admin.server.registry.InfoUpdater;
import de.codecentric.boot.admin.server.registry.StatusUpdateTrigger;
import de.codecentric.boot.admin.server.registry.StatusUpdater;
import de.codecentric.boot.admin.server.registry.store.ApplicationStore;
import de.codecentric.boot.admin.server.registry.store.SimpleApplicationStore;
import de.codecentric.boot.admin.server.web.client.ApplicationOperations;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import io.netty.channel.ChannelOption;

import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(AdminServerProperties.class)
public class AdminServerCoreConfiguration {
    private final AdminServerProperties adminServerProperties;

    public AdminServerCoreConfiguration(AdminServerProperties adminServerProperties) {
        this.adminServerProperties = adminServerProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationRegistry applicationRegistry(ApplicationStore applicationStore,
                                                   ApplicationIdGenerator applicationIdGenerator) {
        return new ApplicationRegistry(applicationStore, applicationIdGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationIdGenerator applicationIdGenerator() {
        return new HashingApplicationUrlIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpHeadersProvider httpHeadersProvider() {
        return new BasicAuthHttpHeaderProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationOperations applicationOperations(HttpHeadersProvider headersProvider) {
        ReactorClientHttpConnector httpConnector = new ReactorClientHttpConnector(options -> {
            options.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                    adminServerProperties.getMonitor().getConnectTimeout());
            options.option(ChannelOption.SO_TIMEOUT, adminServerProperties.getMonitor().getReadTimeout());
        });

        WebClient webClient = WebClient.builder()
                                       .clientConnector(httpConnector)
                                       .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                                       .build();
        return new ApplicationOperations(webClient, headersProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public StatusUpdater statusUpdater(ApplicationStore applicationStore, ApplicationOperations applicationOperations) {
        StatusUpdater statusUpdater = new StatusUpdater(applicationStore, applicationOperations);
        statusUpdater.setStatusLifetime(adminServerProperties.getMonitor().getStatusLifetime());
        return statusUpdater;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public StatusUpdateTrigger statusUpdateTrigger(StatusUpdater statusUpdater,
                                                   Publisher<ClientApplicationEvent> events) {
        StatusUpdateTrigger trigger = new StatusUpdateTrigger(statusUpdater, events);
        trigger.setUpdateInterval(adminServerProperties.getMonitor().getPeriod());
        return trigger;
    }

    @Bean
    @ConditionalOnMissingBean
    public InfoUpdater infoUpdater(ApplicationStore applicationStore, ApplicationOperations applicationOperations) {
        return new InfoUpdater(applicationStore, applicationOperations);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public InfoUpdateTrigger infoUpdateTrigger(InfoUpdater infoUpdater, Publisher<ClientApplicationEvent> events) {
        return new InfoUpdateTrigger(infoUpdater, events);
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientApplicationEventStore eventStore() {
        return new SimpleEventStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationStore applicationStore() {
        return new SimpleApplicationStore();
    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener
    public void onClientApplicationEvent(ClientApplicationEvent event) {
        eventStore().store(event);
    }
}
