/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.client.config;

import java.util.Collections;
import java.util.List;

import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import de.codecentric.boot.admin.client.registration.ApplicationFactory;
import de.codecentric.boot.admin.client.registration.InetUtilsReactiveApplicationFactory;
import de.codecentric.boot.admin.client.registration.InetUtilsServletApplicationFactory;
import de.codecentric.boot.admin.client.registration.metadata.CompositeMetadataContributor;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@ConditionalOnClass(InetUtils.class)
@ConditionalOnBean(InetUtils.class)
@Conditional(SpringBootAdminClientEnabledCondition.class)
@AutoConfigureAfter({ UtilAutoConfiguration.class, SpringBootAdminClientCloudFoundryAutoConfiguration.class })
@AutoConfigureBefore(SpringBootAdminClientAutoConfiguration.class)
public class SpringBootAdminClientInetUtilsAutoConfiguration {

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication(type = Type.SERVLET)
	public static class ServletConfiguration {

		@Bean
		@Lazy(false)
		@ConditionalOnMissingBean(ApplicationFactory.class)
		public ApplicationFactory applicationFactory(InstanceProperties instance, ManagementServerProperties management,
				ServerProperties server, ServletContext servletContext, PathMappedEndpoints pathMappedEndpoints,
				WebEndpointProperties webEndpoint, ObjectProvider<List<MetadataContributor>> metadataContributors,
				DispatcherServletPath dispatcherServletPath, InetUtils inetUtils) {
			return new InetUtilsServletApplicationFactory(instance, management, server, servletContext,
					pathMappedEndpoints, webEndpoint,
					new CompositeMetadataContributor(metadataContributors.getIfAvailable(Collections::emptyList)),
					dispatcherServletPath, inetUtils);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication(type = Type.REACTIVE)
	public static class ReactiveConfiguration {

		@Bean
		@Lazy(false)
		@ConditionalOnMissingBean(ApplicationFactory.class)
		public ApplicationFactory applicationFactory(InstanceProperties instance, ManagementServerProperties management,
				ServerProperties server, PathMappedEndpoints pathMappedEndpoints, WebEndpointProperties webEndpoint,
				ObjectProvider<List<MetadataContributor>> metadataContributors, WebFluxProperties webFluxProperties,
				InetUtils inetUtils) {
			return new InetUtilsReactiveApplicationFactory(instance, management, server, pathMappedEndpoints,
					webEndpoint,
					new CompositeMetadataContributor(metadataContributors.getIfAvailable(Collections::emptyList)),
					webFluxProperties, inetUtils);
		}

	}

}
