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
package de.codecentric.boot.admin.config;

import de.codecentric.boot.admin.journal.ApplicationEventJournal;
import de.codecentric.boot.admin.journal.store.JournaledEventStore;
import de.codecentric.boot.admin.journal.store.SimpleJournaledEventStore;
import de.codecentric.boot.admin.registry.ApplicationIdGenerator;
import de.codecentric.boot.admin.registry.ApplicationRegistry;
import de.codecentric.boot.admin.registry.HashingApplicationUrlIdGenerator;
import de.codecentric.boot.admin.registry.StatusUpdateApplicationListener;
import de.codecentric.boot.admin.registry.StatusUpdater;
import de.codecentric.boot.admin.registry.store.ApplicationStore;
import de.codecentric.boot.admin.registry.store.SimpleApplicationStore;
import de.codecentric.boot.admin.web.client.ApplicationOperations;
import de.codecentric.boot.admin.web.client.BasicAuthHttpHeaderProvider;
import de.codecentric.boot.admin.web.client.HttpHeadersProvider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.DefaultResponseErrorHandler;

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
    public ApplicationOperations applicationOperations(RestTemplateBuilder restTemplBuilder,
                                                       HttpHeadersProvider headersProvider) {
        RestTemplateBuilder builder = restTemplBuilder.messageConverters(new MappingJackson2HttpMessageConverter())
                                                      .errorHandler(new DefaultResponseErrorHandler() {
                                                          @Override
                                                          protected boolean hasError(HttpStatus statusCode) {
                                                              return false;
                                                          }
                                                      });
        builder = builder.setConnectTimeout(adminServerProperties.getMonitor().getConnectTimeout())
                         .setReadTimeout(adminServerProperties.getMonitor().getReadTimeout());
        return new ApplicationOperations(builder.build(), headersProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public StatusUpdater statusUpdater(ApplicationStore applicationStore, ApplicationOperations applicationOperations) {
        StatusUpdater statusUpdater = new StatusUpdater(applicationStore, applicationOperations);
        statusUpdater.setStatusLifetime(adminServerProperties.getMonitor().getStatusLifetime());
        return statusUpdater;
    }

    @Bean
    @Qualifier("updateTaskScheduler")
    public ThreadPoolTaskScheduler updateTaskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(1);
        taskScheduler.setRemoveOnCancelPolicy(true);
        taskScheduler.setThreadNamePrefix("updateTask");
        return taskScheduler;
    }

    @Bean
    @ConditionalOnMissingBean
    public StatusUpdateApplicationListener statusUpdateApplicationListener(StatusUpdater statusUpdater,
                                                                           @Qualifier("updateTaskScheduler") ThreadPoolTaskScheduler taskScheduler) {
        StatusUpdateApplicationListener listener = new StatusUpdateApplicationListener(statusUpdater, taskScheduler);
        listener.setUpdatePeriod(adminServerProperties.getMonitor().getPeriod());
        return listener;
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationEventJournal applicationEventJournal(JournaledEventStore journaledEventStore) {
        return new ApplicationEventJournal(journaledEventStore);
    }

    @Bean
    @ConditionalOnMissingBean
    public JournaledEventStore journaledEventStore() {
        return new SimpleJournaledEventStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationStore applicationStore() {
        return new SimpleApplicationStore();
    }

}
