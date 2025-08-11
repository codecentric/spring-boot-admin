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

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.boot.restclient.autoconfigure.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.boot.webflux.autoconfigure.WebFluxProperties;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;
import de.codecentric.boot.admin.client.registration.RegistrationClient;

import static org.assertj.core.api.Assertions.assertThat;

class SpringBootAdminClientAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(EndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class,
				DispatcherServletAutoConfiguration.class, RestTemplateAutoConfiguration.class,
				SpringBootAdminClientAutoConfiguration.class));

	@Test
	void not_active() {
		this.contextRunner.run((context) -> assertThat(context).doesNotHaveBean(ApplicationRegistrator.class));
	}

	@Test
	void active() {
		this.contextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081")
			.run((context) -> assertThat(context).hasSingleBean(ApplicationRegistrator.class));
	}

	@Test
	void disabled() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081",
					"spring.boot.admin.client.enabled:false")
			.run((context) -> assertThat(context).doesNotHaveBean(ApplicationRegistrator.class));
	}

	@Test
	void nonWebEnvironment() {
		final ApplicationContextRunner nonWebContextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(SpringBootAdminClientAutoConfiguration.class));

		nonWebContextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081")
			.run((context) -> assertThat(context).doesNotHaveBean(ApplicationRegistrator.class));
	}

	@Test
	void reactiveEnvironment() {
		final ReactiveWebApplicationContextRunner reactiveContextRunner = new ReactiveWebApplicationContextRunner()
			.withConfiguration(
					AutoConfigurations.of(EndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class,
							WebClientAutoConfiguration.class, SpringBootAdminClientAutoConfiguration.class))
			.withBean(WebFluxProperties.class);
		reactiveContextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081")
			.run((context) -> assertThat(context).hasSingleBean(ApplicationRegistrator.class));
	}

	@Test
	void blockingClientInBlockingEnvironment() {
		final WebApplicationContextRunner webApplicationContextRunner = new WebApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(EndpointAutoConfiguration.class,
					WebEndpointAutoConfiguration.class, DispatcherServletAutoConfiguration.class,
					RestTemplateAutoConfiguration.class, SpringBootAdminClientAutoConfiguration.class));

		webApplicationContextRunner
			.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081",
					"spring.boot.admin.client.connectTimeout=1337", "spring.boot.admin.client.readTimeout=42")
			.run((context) -> {
				final RegistrationClient registrationClient = context.getBean(RegistrationClient.class);
				final RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(registrationClient,
						"restTemplate");
				assertThat(restTemplate).isNotNull();

				final ClientHttpRequestFactory requestFactory = restTemplate.getRequestFactory();

				final Integer connectTimeout = (Integer) ReflectionTestUtils.getField(requestFactory, "connectTimeout");
				assertThat(connectTimeout).isEqualTo(1337);
				final Duration readTimeout = (Duration) ReflectionTestUtils.getField(requestFactory, "readTimeout");
				assertThat(readTimeout).isEqualTo(Duration.ofMillis(42));
			});
	}

	@Test
	void restClientRegistrationClientInBlockingEnvironment() {
		final WebApplicationContextRunner webApplicationContextRunner = new WebApplicationContextRunner()
			.withConfiguration(
					AutoConfigurations.of(EndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class,
							DispatcherServletAutoConfiguration.class, HttpClientAutoConfiguration.class,
							RestClientAutoConfiguration.class, SpringBootAdminClientAutoConfiguration.class));

		webApplicationContextRunner
			.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081",
					"spring.boot.admin.client.connectTimeout=1337", "spring.boot.admin.client.readTimeout=42")
			.withInitializer(new ConditionEvaluationReportLoggingListener())
			.run((context) -> {
				final RegistrationClient registrationClient = context.getBean(RegistrationClient.class);
				final RestClient restClient = (RestClient) ReflectionTestUtils.getField(registrationClient,
						"restClient");
				assertThat(restClient).isNotNull();

				final ClientHttpRequestFactory requestFactory = (ClientHttpRequestFactory) ReflectionTestUtils
					.getField(restClient, "clientRequestFactory");

				final Integer connectTimeout = (Integer) ReflectionTestUtils.getField(requestFactory, "connectTimeout");
				assertThat(connectTimeout).isEqualTo(1337);
				final Duration readTimeout = (Duration) ReflectionTestUtils.getField(requestFactory, "readTimeout");
				assertThat(readTimeout).isEqualTo(Duration.ofMillis(42));
			});
	}

	@Test
	void customBlockingClientInReactiveEnvironment() {
		final ReactiveWebApplicationContextRunner reactiveContextRunner = new ReactiveWebApplicationContextRunner()
			.withConfiguration(UserConfigurations.of(CustomBlockingConfiguration.class))
			.withConfiguration(
					AutoConfigurations.of(EndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class,
							WebClientAutoConfiguration.class, SpringBootAdminClientAutoConfiguration.class))
			.withBean(WebFluxProperties.class);

		reactiveContextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081")
			.run((context) -> {
				assertThat(context).hasSingleBean(ApplicationRegistrator.class);
				assertThat(context).getBean("registrationClient")
					.isEqualTo(context.getBean(CustomBlockingConfiguration.class).registrationClient);
			});
	}

	@Test
	void customBlockingClientInBlockingEnvironment() {
		final WebApplicationContextRunner webApplicationContextRunner = new WebApplicationContextRunner()
			.withConfiguration(UserConfigurations.of(CustomBlockingConfiguration.class))
			.withConfiguration(AutoConfigurations.of(EndpointAutoConfiguration.class,
					WebEndpointAutoConfiguration.class, DispatcherServletAutoConfiguration.class,
					RestTemplateAutoConfiguration.class, SpringBootAdminClientAutoConfiguration.class));

		webApplicationContextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081")
			.run((context) -> {
				assertThat(context).hasSingleBean(ApplicationRegistrator.class);
				assertThat(context).getBean("registrationClient")
					.isEqualTo(context.getBean(CustomBlockingConfiguration.class).registrationClient);
			});
	}

	@Configuration
	public static class CustomBlockingConfiguration {

		final RegistrationClient registrationClient = new BlockingRegistrationClient(new RestTemplate());

		@Bean
		public RegistrationClient registrationClient() {
			return registrationClient;
		}

	}

}
