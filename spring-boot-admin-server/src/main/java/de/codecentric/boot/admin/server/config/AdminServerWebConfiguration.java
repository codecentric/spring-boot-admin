/*
 * Copyright 2014-2024 the original author or authors.
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

package de.codecentric.boot.admin.server.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;

import de.codecentric.boot.admin.server.eventstore.InstanceEventStore;
import de.codecentric.boot.admin.server.services.ApplicationRegistry;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.utils.jackson.AdminServerModule;
import de.codecentric.boot.admin.server.web.ApplicationsController;
import de.codecentric.boot.admin.server.web.InstancesController;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

@Configuration(proxyBeanMethods = false)
public class AdminServerWebConfiguration {

	private final AdminServerProperties adminServerProperties;

	public AdminServerWebConfiguration(AdminServerProperties adminServerProperties) {
		this.adminServerProperties = adminServerProperties;
	}

	@Bean
	public SimpleModule adminJacksonModule() {
		return new AdminServerModule(this.adminServerProperties.getMetadataKeysToSanitize());
	}

	@Bean
	@ConditionalOnMissingBean
	public InstancesController instancesController(InstanceRegistry instanceRegistry, InstanceEventStore eventStore) {
		return new InstancesController(instanceRegistry, eventStore);
	}

	@Bean
	@ConditionalOnMissingBean
	public ApplicationsController applicationsController(ApplicationRegistry applicationRegistry,
			ApplicationEventPublisher applicationEventPublisher) {
		return new ApplicationsController(applicationRegistry, applicationEventPublisher);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
	public static class ReactiveRestApiConfiguration {

		private final AdminServerProperties adminServerProperties;

		public ReactiveRestApiConfiguration(AdminServerProperties adminServerProperties) {
			this.adminServerProperties = adminServerProperties;
		}

		@Bean
		@ConditionalOnMissingBean
		public de.codecentric.boot.admin.server.web.reactive.InstancesProxyController instancesProxyController(
				InstanceRegistry instanceRegistry, InstanceWebClient.Builder instanceWebClientBuilder) {
			return new de.codecentric.boot.admin.server.web.reactive.InstancesProxyController(
					this.adminServerProperties.getContextPath(),
					this.adminServerProperties.getInstanceProxy().getIgnoredHeaders(), instanceRegistry,
					instanceWebClientBuilder.build());
		}

		@Bean
		public org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping adminHandlerMapping(
				RequestedContentTypeResolver webFluxContentTypeResolver) {
			org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping mapping = new de.codecentric.boot.admin.server.web.reactive.AdminControllerHandlerMapping(
					this.adminServerProperties.getContextPath());
			mapping.setOrder(0);
			mapping.setContentTypeResolver(webFluxContentTypeResolver);
			return mapping;
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
	@AutoConfigureAfter(WebMvcAutoConfiguration.class)
	public static class ServletRestApiConfiguration {

		private final AdminServerProperties adminServerProperties;

		public ServletRestApiConfiguration(AdminServerProperties adminServerProperties) {
			this.adminServerProperties = adminServerProperties;
		}

		@Bean
		@ConditionalOnMissingBean
		public de.codecentric.boot.admin.server.web.servlet.InstancesProxyController instancesProxyController(
				InstanceRegistry instanceRegistry, InstanceWebClient.Builder instanceWebClientBuilder) {
			return new de.codecentric.boot.admin.server.web.servlet.InstancesProxyController(
					this.adminServerProperties.getContextPath(),
					this.adminServerProperties.getInstanceProxy().getIgnoredHeaders(), instanceRegistry,
					instanceWebClientBuilder.build());
		}

		@Bean
		public org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping adminHandlerMapping(
				ContentNegotiationManager contentNegotiationManager) {
			org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping mapping = new de.codecentric.boot.admin.server.web.servlet.AdminControllerHandlerMapping(
					this.adminServerProperties.getContextPath());
			mapping.setOrder(0);
			mapping.setContentNegotiationManager(contentNegotiationManager);
			return mapping;
		}

	}

}
