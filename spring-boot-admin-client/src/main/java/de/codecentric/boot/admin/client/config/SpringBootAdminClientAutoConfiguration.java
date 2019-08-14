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

package de.codecentric.boot.admin.client.config;

import de.codecentric.boot.admin.client.registration.ApplicationFactory;
import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;
import de.codecentric.boot.admin.client.registration.DefaultApplicationFactory;
import de.codecentric.boot.admin.client.registration.ReactiveRegistrationClient;
import de.codecentric.boot.admin.client.registration.RegistrationApplicationListener;
import de.codecentric.boot.admin.client.registration.RegistrationClient;
import de.codecentric.boot.admin.client.registration.ServletApplicationFactory;
import de.codecentric.boot.admin.client.registration.metadata.CompositeMetadataContributor;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;
import de.codecentric.boot.admin.client.registration.metadata.StartupDateMetadataContributor;

import java.util.Collections;
import java.util.List;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@Configuration
@ConditionalOnWebApplication
@Conditional(SpringBootAdminClientEnabledCondition.class)
@AutoConfigureAfter({WebEndpointAutoConfiguration.class, RestTemplateAutoConfiguration.class, WebClientAutoConfiguration.class})
@EnableConfigurationProperties({ClientProperties.class, InstanceProperties.class, ServerProperties.class, ManagementServerProperties.class})
public class SpringBootAdminClientAutoConfiguration {

    @Configuration
    @ConditionalOnWebApplication(type = Type.SERVLET)
    @AutoConfigureAfter(DispatcherServletAutoConfiguration.class)
    public static class ServletConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public ApplicationFactory applicationFactory(InstanceProperties instance,
                                                     ManagementServerProperties management,
                                                     ServerProperties server,
                                                     ServletContext servletContext,
                                                     PathMappedEndpoints pathMappedEndpoints,
                                                     WebEndpointProperties webEndpoint,
                                                     ObjectProvider<List<MetadataContributor>> metadataContributors,
                                                     DispatcherServletPath dispatcherServletPath) {
            return new ServletApplicationFactory(instance,
                management,
                server,
                servletContext,
                pathMappedEndpoints,
                webEndpoint,
                new CompositeMetadataContributor(metadataContributors.getIfAvailable(Collections::emptyList)),
                dispatcherServletPath
            );
        }
    }

    @Configuration
    @ConditionalOnWebApplication(type = Type.REACTIVE)
    public static class ReactiveConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public ApplicationFactory applicationFactory(InstanceProperties instance,
                                                     ManagementServerProperties management,
                                                     ServerProperties server,
                                                     PathMappedEndpoints pathMappedEndpoints,
                                                     WebEndpointProperties webEndpoint,
                                                     ObjectProvider<List<MetadataContributor>> metadataContributors) {
            return new DefaultApplicationFactory(instance,
                management,
                server,
                pathMappedEndpoints,
                webEndpoint,
                new CompositeMetadataContributor(metadataContributors.getIfAvailable(Collections::emptyList))
            );
        }
    }

    @Configuration
    @ConditionalOnBean(RestTemplateBuilder.class)
    static class BlockingRegistrationClientConfig {
        @Bean
        @ConditionalOnMissingBean
        public BlockingRegistrationClient registrationClient(ClientProperties client) {
            RestTemplateBuilder builder = new RestTemplateBuilder().setConnectTimeout(client.getConnectTimeout())
                                                                   .setReadTimeout(client.getReadTimeout());
            if (client.getUsername() != null && client.getPassword() != null) {
                builder = builder.basicAuthentication(client.getUsername(), client.getPassword());
            }
            return new BlockingRegistrationClient(builder.build());
        }
    }

    @Configuration
    @ConditionalOnBean(WebClient.Builder.class)
    @ConditionalOnMissingBean(RestTemplateBuilder.class)
    static class ReactiveRegistrationClientConfig {
        @Bean
        @ConditionalOnMissingBean
        public ReactiveRegistrationClient registrationClient(ClientProperties client, WebClient.Builder webClient) {
            if (client.getUsername() != null && client.getPassword() != null) {
                webClient = webClient.filter(basicAuthentication(client.getUsername(), client.getPassword()));
            }
            return new ReactiveRegistrationClient(webClient.build(), client.getReadTimeout());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationRegistrator registrator(RegistrationClient registrationClient,
                                              ClientProperties client,
                                              ApplicationFactory applicationFactory) {

        return new ApplicationRegistrator(applicationFactory,
            registrationClient,
            client.getAdminUrl(),
            client.isRegisterOnce()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public RegistrationApplicationListener registrationListener(ClientProperties client,
                                                                ApplicationRegistrator registrator,
                                                                Environment environment) {
        RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator);
        listener.setAutoRegister(client.isAutoRegistration());
        listener.setAutoDeregister(client.isAutoDeregistration(environment));
        listener.setRegisterPeriod(client.getPeriod());
        return listener;
    }

    @Bean
    @ConditionalOnMissingBean
    public StartupDateMetadataContributor startupDateMetadataContributor() {
        return new StartupDateMetadataContributor();
    }
}

