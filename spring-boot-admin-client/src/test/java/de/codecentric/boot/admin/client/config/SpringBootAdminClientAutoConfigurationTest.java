/*
 * Copyright 2014-2019 the original author or authors.
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

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxProperties;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;
import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;
import de.codecentric.boot.admin.client.registration.RegistrationClient;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringBootAdminClientAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(EndpointAutoConfiguration.class,
					WebEndpointAutoConfiguration.class, DispatcherServletAutoConfiguration.class,
					RestTemplateAutoConfiguration.class, SpringBootAdminClientAutoConfiguration.class));

	@Test
	public void not_active() {
		this.contextRunner.run((context) -> assertThat(context).doesNotHaveBean(ApplicationRegistrator.class));
	}

	@Test
	public void active() {
		this.contextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081")
				.run((context) -> assertThat(context).hasSingleBean(ApplicationRegistrator.class));
	}

	@Test
	public void disabled() {
		this.contextRunner
				.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081",
						"spring.boot.admin.client.enabled:false")
				.run((context) -> assertThat(context).doesNotHaveBean(ApplicationRegistrator.class));
	}

	@Test
	public void nonWebEnvironment() {
		ApplicationContextRunner nonWebcontextRunner = new ApplicationContextRunner()
				.withConfiguration(AutoConfigurations.of(SpringBootAdminClientAutoConfiguration.class));

		nonWebcontextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081")
				.run((context) -> assertThat(context).doesNotHaveBean(ApplicationRegistrator.class));
	}

	@Test
	public void reactiveEnvironment() {
		ReactiveWebApplicationContextRunner reactiveContextRunner = new ReactiveWebApplicationContextRunner()
				.withConfiguration(
						AutoConfigurations.of(EndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class,
								WebClientAutoConfiguration.class, SpringBootAdminClientAutoConfiguration.class))
				.withBean(WebFluxProperties.class);
		reactiveContextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081")
				.run((context) -> assertThat(context).hasSingleBean(ApplicationRegistrator.class));
	}

	@Test
	public void customBlockingClientInReactiveEnvironment() {
		ReactiveWebApplicationContextRunner reactiveContextRunner = new ReactiveWebApplicationContextRunner()
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
	public void customBlockingClientInBlockingEnvironment() {
		WebApplicationContextRunner webApplicationContextRunner = new WebApplicationContextRunner()
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

		RegistrationClient registrationClient = new BlockingRegistrationClient(new RestTemplate());

		@Bean
		public RegistrationClient registrationClient() {
			return registrationClient;
		}

	}

}
