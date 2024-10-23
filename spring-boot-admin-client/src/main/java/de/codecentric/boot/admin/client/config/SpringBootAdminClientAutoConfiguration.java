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
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import jakarta.servlet.ServletContext;
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
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import de.codecentric.boot.admin.client.registration.ApplicationFactory;
import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;
import de.codecentric.boot.admin.client.registration.DefaultApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.ReactiveApplicationFactory;
import de.codecentric.boot.admin.client.registration.ReactiveRegistrationClient;
import de.codecentric.boot.admin.client.registration.RegistrationApplicationListener;
import de.codecentric.boot.admin.client.registration.RegistrationClient;
import de.codecentric.boot.admin.client.registration.ServletApplicationFactory;
import de.codecentric.boot.admin.client.registration.metadata.CompositeMetadataContributor;
import de.codecentric.boot.admin.client.registration.metadata.MetadataContributor;
import de.codecentric.boot.admin.client.registration.metadata.StartupDateMetadataContributor;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import static org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication
@Conditional(SpringBootAdminClientEnabledCondition.class)
@AutoConfigureAfter({ WebEndpointAutoConfiguration.class, RestTemplateAutoConfiguration.class,
		WebClientAutoConfiguration.class })
@EnableConfigurationProperties({ ClientProperties.class, InstanceProperties.class, ServerProperties.class,
		ManagementServerProperties.class })
public class SpringBootAdminClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ApplicationRegistrator registrator(RegistrationClient registrationClient, ClientProperties client,
			ApplicationFactory applicationFactory) {
		return new DefaultApplicationRegistrator(applicationFactory, registrationClient, client.getAdminUrl(),
				client.isRegisterOnce());
	}

	@Bean
	@ConditionalOnMissingBean
	public RegistrationApplicationListener registrationListener(ClientProperties client,
			ApplicationRegistrator registrator, Environment environment) {
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

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication(type = Type.SERVLET)
	@AutoConfigureAfter(DispatcherServletAutoConfiguration.class)
	public static class ServletConfiguration {

		@Bean
		@Lazy(false)
		@ConditionalOnMissingBean
		public ApplicationFactory applicationFactory(InstanceProperties instance, ManagementServerProperties management,
				ServerProperties server, ServletContext servletContext, PathMappedEndpoints pathMappedEndpoints,
				WebEndpointProperties webEndpoint, ObjectProvider<List<MetadataContributor>> metadataContributors,
				DispatcherServletPath dispatcherServletPath) {
			return new ServletApplicationFactory(instance, management, server, servletContext, pathMappedEndpoints,
					webEndpoint,
					new CompositeMetadataContributor(metadataContributors.getIfAvailable(Collections::emptyList)),
					dispatcherServletPath);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication(type = Type.REACTIVE)
	public static class ReactiveConfiguration {

		@Bean
		@Lazy(false)
		@ConditionalOnMissingBean
		public ApplicationFactory applicationFactory(InstanceProperties instance, ManagementServerProperties management,
				ServerProperties server, PathMappedEndpoints pathMappedEndpoints, WebEndpointProperties webEndpoint,
				ObjectProvider<List<MetadataContributor>> metadataContributors, WebFluxProperties webFluxProperties) {
			return new ReactiveApplicationFactory(instance, management, server, pathMappedEndpoints, webEndpoint,
					new CompositeMetadataContributor(metadataContributors.getIfAvailable(Collections::emptyList)),
					webFluxProperties);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(RestTemplateBuilder.class)
	public static class BlockingRegistrationClientConfig {

		@Bean
		@ConditionalOnMissingBean
		public RegistrationClient registrationClient(ClientProperties client) {
			RestTemplateBuilder builder = new RestTemplateBuilder().setConnectTimeout(client.getConnectTimeout())
				.setReadTimeout(client.getReadTimeout());

			if (client.getUsername() != null && client.getPassword() != null) {
				builder = builder.basicAuthentication(client.getUsername(), client.getPassword());
			}

			RestTemplate build = builder.build();
			return new BlockingRegistrationClient(build);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(WebClient.Builder.class)
	@ConditionalOnMissingBean(RestTemplateBuilder.class)
	public static class ReactiveRegistrationClientConfig {

		@Bean
		@ConditionalOnMissingBean
		public RegistrationClient registrationClient(ClientProperties client, WebClient.Builder webClient) {
			if (client.getUsername() != null && client.getPassword() != null) {
				webClient = webClient.filter(basicAuthentication(client.getUsername(), client.getPassword()));
			}
			HttpClient httpClient = HttpClient.create()
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
						Long.valueOf(client.getConnectTimeout().toMillis()).intValue())
				.doOnConnected(conn -> conn
					.addHandlerLast(new ReadTimeoutHandler(client.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS)));
			webClient.clientConnector(new ReactorClientHttpConnector(httpClient));
			return new ReactiveRegistrationClient(webClient.build(), client.getReadTimeout());
		}

	}

}
