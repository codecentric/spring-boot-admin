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

import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.web.AdminController;
import de.codecentric.boot.admin.server.web.ApplicationsController;
import de.codecentric.boot.admin.server.web.InstancesController;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

@Configuration
public class AdminServerWebConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public InstancesController instancesController(InstanceRegistry instanceRegistry,
                                                   InstanceEventStore eventStore,
                                                   InstanceWebClient instanceWebClient) {

        return new InstancesController(instanceRegistry, eventStore, instanceWebClient);
    }


    @Bean
    @ConditionalOnMissingBean
    public InstanceWebClient instanceWebClient(HttpHeadersProvider httpHeadersProvider,
                                               AdminServerProperties adminServerProperties) {
        return new InstanceWebClient(httpHeadersProvider, adminServerProperties.getMonitor().getConnectTimeout(),
                adminServerProperties.getMonitor().getReadTimeout());
    }


    @Bean
    @ConditionalOnMissingBean
    public ApplicationsController applicationsController(InstanceRegistry instanceRegistry,
                                                         InstanceEventPublisher eventPublisher) {
        return new ApplicationsController(instanceRegistry, eventPublisher);
    }


    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    public static class ReactiveConfiguration implements WebFluxConfigurer {
        @Bean
        public RequestMappingHandlerMapping adminHandlerMapping(RequestedContentTypeResolver webFluxContentTypeResolver) {
            RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping() {
                @Override
                protected boolean isHandler(Class<?> beanType) {
                    return AnnotatedElementUtils.hasAnnotation(beanType, AdminController.class);
                }
            };
            mapping.setOrder(0);
            mapping.setContentTypeResolver(webFluxContentTypeResolver);
            return mapping;
        }

    }
}
