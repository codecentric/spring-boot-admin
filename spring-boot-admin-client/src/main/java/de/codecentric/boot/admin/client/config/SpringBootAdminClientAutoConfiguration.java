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

package de.codecentric.boot.admin.client.config;

import de.codecentric.boot.admin.client.registration.ApplicationFactory;
import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.DefaultApplicationFactory;
import de.codecentric.boot.admin.client.registration.RegistrationApplicationListener;
import de.codecentric.boot.admin.client.registration.ServletApplicationFactory;
import de.codecentric.boot.admin.client.registration.metadata.CompositeMetadataContributor;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;
import de.codecentric.boot.admin.client.registration.metadata.StartupDateMetadataContributor;

import java.util.Collections;
import java.util.List;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.servlet.WebMvcEndpointManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;

@Configuration
@ConditionalOnWebApplication
@Conditional(SpringBootAdminClientEnabledCondition.class)
@AutoConfigureAfter(WebMvcEndpointManagementContextConfiguration.class)
@EnableConfigurationProperties({ClientProperties.class, InstanceProperties.class})
public class SpringBootAdminClientAutoConfiguration {

    @Configuration
    @ConditionalOnWebApplication(type = Type.SERVLET)
    public static class ServletConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public ApplicationFactory applicationFactory(InstanceProperties instance,
                                                     ManagementServerProperties management,
                                                     ServerProperties server,
                                                     ServletContext servletContext,
                                                     PathMappedEndpoints pathMappedEndpoints,
                                                     WebEndpointProperties webEndpoint,
                                                     MetadataContributor metadataContributor) {
            return new ServletApplicationFactory(instance, management, server, servletContext, pathMappedEndpoints,
                webEndpoint, metadataContributor);
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
                                                     MetadataContributor metadataContributor) {
            return new DefaultApplicationFactory(instance, management, server, pathMappedEndpoints, webEndpoint,
                metadataContributor);
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationRegistrator registrator(ClientProperties client,
                                              ApplicationFactory applicationFactory,
                                              RestTemplateBuilder restTemplBuilder) {
        RestTemplateBuilder builder = restTemplBuilder.messageConverters(new MappingJackson2HttpMessageConverter())
                                                      .requestFactory(SimpleClientHttpRequestFactory.class)
                                                      .setConnectTimeout((int) client.getConnectTimeout().toMillis())
                                                      .setReadTimeout((int) client.getReadTimeout().toMillis());
        if (client.getUsername() != null) {
            builder = builder.basicAuthorization(client.getUsername(), client.getPassword());
        }
        return new ApplicationRegistrator(builder.build(), client, applicationFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public RegistrationApplicationListener registrationListener(ClientProperties client,
                                                                ApplicationRegistrator registrator) {
        RegistrationApplicationListener listener = new RegistrationApplicationListener(registrator);
        listener.setAutoRegister(client.isAutoRegistration());
        listener.setAutoDeregister(client.isAutoDeregistration());
        listener.setRegisterPeriod(client.getPeriod());
        return listener;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public CompositeMetadataContributor metadataContributor(ObjectProvider<List<MetadataContributor>> contributorProvider) {
        List<MetadataContributor> contributors = contributorProvider.getIfAvailable(Collections::emptyList);
        return new CompositeMetadataContributor(contributors);
    }

    @Bean
    @ConditionalOnMissingBean
    public StartupDateMetadataContributor startupDateMetadataContributor() {
        return new StartupDateMetadataContributor();
    }
}

