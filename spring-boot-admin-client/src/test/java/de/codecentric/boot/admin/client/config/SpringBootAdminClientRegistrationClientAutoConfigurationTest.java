/*
 * Copyright 2014-2025 the original author or authors.
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

import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import de.codecentric.boot.admin.client.registration.BlockingRegistrationClient;
import de.codecentric.boot.admin.client.registration.ReactiveRegistrationClient;
import de.codecentric.boot.admin.client.registration.RegistrationClient;
import de.codecentric.boot.admin.client.registration.RestClientRegistrationClient;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringBootAdminClientRegistrationClientAutoConfigurationTest {

	@ParameterizedTest(name = "{0}")
	@MethodSource("contextRunnerCustomizations")
	public void autoConfiguresRegistrationClient(String testCaseName,
			Function<WebApplicationContextRunner, WebApplicationContextRunner> customizer,
			Class<RegistrationClient> expectedRegistrationClient) {
		WebApplicationContextRunner webApplicationContextRunner = new WebApplicationContextRunner()
			.withConfiguration(
					AutoConfigurations.of(EndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class,
							DispatcherServletAutoConfiguration.class, SpringBootAdminClientAutoConfiguration.class))
			.with(customizer)
			.withInitializer(new ConditionEvaluationReportLoggingListener());

		webApplicationContextRunner.withPropertyValues("spring.boot.admin.client.url:http://localhost:8081")
			.run((context) -> {
				RegistrationClient registrationClient = context.getBean(RegistrationClient.class);

				assertThat(registrationClient).isInstanceOf(expectedRegistrationClient);
			});
	}

	/**
	 * <img src="doc-files/RegistrationClientTestCases.png" alt="">
	 * @return context runner customizations
	 */
	public static Stream<Arguments> contextRunnerCustomizations() {
		return Stream.of(//
				Arguments.of(//
						"Test case 01", //
						customizer() //
							.withRestTemplateBuilder()
							.withRestClientBuilder()
							.withClientHttpRequestFactoryBuilder()
							.withWebClientBuilder()
							.build(), //
						ReactiveRegistrationClient.class),
				Arguments.of(//
						"Test case 02", //
						customizer() //
							.withRestTemplateBuilder()
							.withRestClientBuilder()
							.withClientHttpRequestFactoryBuilder()
							.build(), //
						RestClientRegistrationClient.class),
				Arguments.of(//
						"Test case 03", //
						customizer() //
							.withRestTemplateBuilder()
							.withRestClientBuilder()
							.withWebClientBuilder()
							.build(), //
						ReactiveRegistrationClient.class),
				Arguments.of(//
						"Test case 04", //
						customizer() //
							.withRestTemplateBuilder()
							.withRestClientBuilder()
							.build(), //
						BlockingRegistrationClient.class),
				Arguments.of(//
						"Test case 05", //
						customizer() //
							.withRestTemplateBuilder()
							.withClientHttpRequestFactoryBuilder()
							.withWebClientBuilder()
							.build(), //
						ReactiveRegistrationClient.class),
				Arguments.of(//
						"Test case 06", //
						customizer() //
							.withRestTemplateBuilder()
							.withClientHttpRequestFactoryBuilder()
							.build(), //
						BlockingRegistrationClient.class),
				Arguments.of(//
						"Test case 07", //
						customizer() //
							.withRestTemplateBuilder()
							.withWebClientBuilder()
							.build(), //
						ReactiveRegistrationClient.class),
				Arguments.of(//
						"Test case 08", //
						customizer() //
							.withRestTemplateBuilder()
							.build(), //
						BlockingRegistrationClient.class),
				Arguments.of(//
						"Test case 09", //
						customizer() //
							.withRestClientBuilder()
							.withClientHttpRequestFactoryBuilder()
							.withWebClientBuilder()
							.build(), //
						ReactiveRegistrationClient.class),
				Arguments.of(//
						"Test case 10", //
						customizer() //
							.withRestClientBuilder()
							.withClientHttpRequestFactoryBuilder()
							.build(), //
						RestClientRegistrationClient.class),
				Arguments.of(//
						"Test case 11", //
						customizer() //
							.withRestClientBuilder()
							.withWebClientBuilder()
							.build(), //
						ReactiveRegistrationClient.class),
				Arguments.of(//
						"Test case 13", //
						customizer() //
							.withClientHttpRequestFactoryBuilder()
							.withWebClientBuilder()
							.build(), //
						ReactiveRegistrationClient.class),
				Arguments.of(//
						"Test case 15", //
						customizer() //
							.withWebClientBuilder()
							.build(), //
						ReactiveRegistrationClient.class) //
		);
	}

	private static ContextRunnerCustomizerBuilder customizer() {
		return new ContextRunnerCustomizerBuilder();
	}

	private static final class ContextRunnerCustomizerBuilder {

		private Function<WebApplicationContextRunner, WebApplicationContextRunner> customizer = (runner) -> runner;

		ContextRunnerCustomizerBuilder withRestTemplateBuilder() {
			customizer = customizer
				.andThen((runner) -> runner.withBean(RestTemplateBuilder.class, RestTemplateBuilder::new));
			return this;
		}

		ContextRunnerCustomizerBuilder withRestClientBuilder() {
			customizer = customizer.andThen((runner) -> runner.withBean(RestClient.Builder.class, RestClient::builder));
			return this;
		}

		ContextRunnerCustomizerBuilder withClientHttpRequestFactoryBuilder() {
			customizer = customizer.andThen((runner) -> runner.withBean(ClientHttpRequestFactoryBuilder.class,
					ClientHttpRequestFactoryBuilder::detect));
			return this;
		}

		ContextRunnerCustomizerBuilder withWebClientBuilder() {
			customizer = customizer.andThen((runner) -> runner.withBean(WebClient.Builder.class, WebClient::builder));
			return this;
		}

		Function<WebApplicationContextRunner, WebApplicationContextRunner> build() {
			return customizer;
		}

	}

}
