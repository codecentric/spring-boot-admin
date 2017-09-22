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

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.eventstore.InstanceEventPublisher;
import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.web.AdminController;
import de.codecentric.boot.admin.server.web.ApplicationsController;
import de.codecentric.boot.admin.server.web.FixedJackson2Decoder;
import de.codecentric.boot.admin.server.web.InstanceRouteDefinitionLocator;
import de.codecentric.boot.admin.server.web.InstancesController;

import org.reactivestreams.Publisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.codec.CodecCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class AdminServerWebConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public InstancesController instancesController(InstanceRegistry instanceRegistry, InstanceEventStore eventStore) {
        return new InstancesController(instanceRegistry, eventStore);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationsController applicationsController(InstanceRegistry instanceRegistry,
                                                         InstanceEventPublisher eventPublisher) {
        return new ApplicationsController(instanceRegistry, eventPublisher);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public InstanceRouteDefinitionLocator instanceRouteDefinitionLocator(Publisher<InstanceEvent> publisher) {
        return new InstanceRouteDefinitionLocator(publisher);
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

        //FIXME remove after resolution of SPR-15975
        private static final MimeType[] EMPTY_MIME_TYPES = {};

        //FIXME remove after resolution of SPR-15975
        @Bean
        public CodecCustomizer codecCustomizer(ObjectMapper objectMapper) {
            return (configurer) -> {
                CodecConfigurer.DefaultCodecs defaults = configurer.defaultCodecs();
                defaults.jackson2JsonDecoder(new FixedJackson2Decoder(objectMapper, EMPTY_MIME_TYPES));
                defaults.jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, EMPTY_MIME_TYPES));
            };
        }
    }
}
