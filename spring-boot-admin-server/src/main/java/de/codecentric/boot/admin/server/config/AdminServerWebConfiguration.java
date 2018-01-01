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
package de.codecentric.boot.admin.server.config;

import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.utils.jackson.RegistrationBeanSerializerModifier;
import de.codecentric.boot.admin.server.utils.jackson.RegistrationDeserializer;
import de.codecentric.boot.admin.server.utils.jackson.SanitizingMapSerializer;
import de.codecentric.boot.admin.server.web.AdminController;
import de.codecentric.boot.admin.server.web.ApplicationsController;
import de.codecentric.boot.admin.server.web.InstancesController;
import de.codecentric.boot.admin.server.web.InstancesReactiveProxyController;
import de.codecentric.boot.admin.server.web.InstancesServletProxyController;
import de.codecentric.boot.admin.server.web.client.HttpHeadersProvider;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class AdminServerWebConfiguration {
    @Bean
    public SimpleModule adminJacksonModule(AdminServerProperties adminServerProperties) {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Registration.class, new RegistrationDeserializer());
        module.setSerializerModifier(new RegistrationBeanSerializerModifier(
                new SanitizingMapSerializer(adminServerProperties.getMetadataKeysToSanitize())));
        return module;
    }

    @Bean
    @ConditionalOnMissingBean
    public InstancesController instancesController(InstanceRegistry instanceRegistry, InstanceEventStore eventStore) {
        return new InstancesController(instanceRegistry, eventStore);
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
    public static class ReactiveRestApiConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public InstancesReactiveProxyController instancesProxyController(InstanceRegistry instanceRegistry,
                                                                         InstanceWebClient instanceWebClient) {
            return new InstancesReactiveProxyController(instanceRegistry, instanceWebClient);
        }

        @Bean
        public org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping adminHandlerMapping(
                RequestedContentTypeResolver webFluxContentTypeResolver) {
            org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping mapping = new org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping() {
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

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public static class ServletRestApiConfirguation {
        @Bean
        @ConditionalOnMissingBean
        public InstancesServletProxyController instancesProxyController(InstanceRegistry instanceRegistry,
                                                                        InstanceWebClient instanceWebClient) {
            return new InstancesServletProxyController(instanceRegistry, instanceWebClient);
        }

        @Bean
        public org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping adminHandlerMapping(
                ContentNegotiationManager contentNegotiationManager) {
            org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping mapping = new org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping() {
                @Override
                protected boolean isHandler(Class<?> beanType) {
                    return AnnotatedElementUtils.hasAnnotation(beanType, AdminController.class);
                }
            };
            mapping.setOrder(0);
            mapping.setContentNegotiationManager(contentNegotiationManager);
            return mapping;
        }
    }

}
