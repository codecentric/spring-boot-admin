/*
 * Copyright 2014-2026 the original author or authors.
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

package de.codecentric.boot.admin.server.mcp.tools;

import java.time.Duration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.rewriteEndpointUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnvToolsTest {

	private final WireMockServer wireMock = new WireMockServer(Options.DYNAMIC_PORT);

	private InstanceRepository instanceRepository;

	private EnvTools envTools;

	@BeforeAll
	static void setUpClass() {
		StepVerifier.setDefaultTimeout(Duration.ofSeconds(5));
	}

	@AfterAll
	static void tearDownClass() {
		StepVerifier.resetDefaultTimeout();
	}

	@BeforeEach
	void setUp() {
		this.wireMock.start();
		this.instanceRepository = mock(InstanceRepository.class);
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().filter(rewriteEndpointUrl()).build();
		this.envTools = new EnvTools(
				new ActuatorClient(this.instanceRepository, instanceWebClient, Duration.ofMillis(450)));
	}

	@AfterEach
	void tearDown() {
		this.wireMock.stop();
	}

	@Test
	void getEnv_propertySet_returnsValueAndSources() {
		Instance instance = Instance.create(InstanceId.of("id-1"))
			.register(Registration.create("payment-service", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/env/HELLO"))
			.willReturn(okJson("{\"property\":{\"source\":\"systemEnvironment\",\"value\":\"world\"},"
					+ "\"propertySources\":[{\"name\":\"systemEnvironment\","
					+ "\"property\":{\"value\":\"world\",\"origin\":\"System Environment Property \\\"HELLO\\\"\"}}]}")));

		StepVerifier.create(this.envTools.getEnv("payment-service", "HELLO")).assertNext((result) -> {
			assertThat(result).contains("payment-service");
			assertThat(result).contains("HELLO");
			assertThat(result).contains("value: world");
			assertThat(result).contains("systemEnvironment");
		}).verifyComplete();
	}

	@Test
	void getEnv_propertyNotSet_returnsNotSetMessage() {
		Instance instance = Instance.create(InstanceId.of("id-2"))
			.register(Registration.create("order-service", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("order-service")).thenReturn(Flux.just(instance));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/env/MISSING"))
			.willReturn(okJson("{\"property\":null,\"propertySources\":[{\"name\":\"systemEnvironment\"}]}")));

		StepVerifier.create(this.envTools.getEnv("order-service", "MISSING"))
			.assertNext((result) -> assertThat(result).isEqualTo("Property 'MISSING' is not set for order-service."))
			.verifyComplete();
	}

	@Test
	void getEnv_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("ghost")).thenReturn(Flux.empty());

		StepVerifier.create(this.envTools.getEnv("ghost", "HELLO"))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'ghost' not found in registry."))
			.verifyComplete();
	}

	@Test
	void listEnv_returnsAllPropertiesGroupedBySource() {
		Instance instance = Instance.create(InstanceId.of("id-3"))
			.register(Registration.create("payment-service", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/env"))
			.willReturn(okJson("{\"activeProfiles\":[\"prod\"],\"propertySources\":["
					+ "{\"name\":\"systemEnvironment\",\"properties\":{"
					+ "\"HELLO\":{\"value\":\"world\",\"origin\":\"System Environment Property \\\"HELLO\\\"\"}}},"
					+ "{\"name\":\"application.yml\",\"properties\":{"
					+ "\"spring.application.name\":{\"value\":\"payment-service\"}}}]}")));

		StepVerifier.create(this.envTools.listEnv("payment-service", null)).assertNext((result) -> {
			assertThat(result).contains("Environment for payment-service");
			assertThat(result).contains("active profiles: [prod]");
			assertThat(result).contains("[systemEnvironment]");
			assertThat(result).contains("HELLO = world");
			assertThat(result).contains("[application.yml]");
			assertThat(result).contains("spring.application.name = payment-service");
		}).verifyComplete();
	}

	@Test
	void listEnv_withFilter_returnsOnlyMatchingProperties() {
		Instance instance = Instance.create(InstanceId.of("id-5"))
			.register(Registration.create("payment-service", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/env"))
			.willReturn(okJson("{\"activeProfiles\":[\"prod\"],\"propertySources\":["
					+ "{\"name\":\"systemEnvironment\",\"properties\":{"
					+ "\"HELLO\":{\"value\":\"world\"},\"DB_URL\":{\"value\":\"jdbc:h2:mem\"}}},"
					+ "{\"name\":\"application.yml\",\"properties\":{"
					+ "\"spring.datasource.url\":{\"value\":\"jdbc:h2\"},"
					+ "\"spring.application.name\":{\"value\":\"payment-service\"}}}]}")));

		StepVerifier.create(this.envTools.listEnv("payment-service", "url")).assertNext((result) -> {
			assertThat(result).contains("filtered by \"url\"");
			assertThat(result).contains("DB_URL = jdbc:h2:mem");
			assertThat(result).contains("spring.datasource.url = jdbc:h2");
			assertThat(result).doesNotContain("HELLO");
			assertThat(result).doesNotContain("spring.application.name");
		}).verifyComplete();
	}

	@Test
	void listEnv_withFilter_noMatches_returnsMessage() {
		Instance instance = Instance.create(InstanceId.of("id-6"))
			.register(Registration.create("payment-service", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/env"))
			.willReturn(okJson("{\"propertySources\":[{\"name\":\"systemEnvironment\",\"properties\":{"
					+ "\"HELLO\":{\"value\":\"world\"}}}]}")));

		StepVerifier.create(this.envTools.listEnv("payment-service", "nomatch"))
			.assertNext((result) -> assertThat(result)
				.isEqualTo("No environment properties matching 'nomatch' for payment-service."))
			.verifyComplete();
	}

	@Test
	void listEnv_noPropertySources_returnsNoPropertiesMessage() {
		Instance instance = Instance.create(InstanceId.of("id-4"))
			.register(Registration.create("order-service", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("order-service")).thenReturn(Flux.just(instance));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/env")).willReturn(okJson("{\"propertySources\":[]}")));

		StepVerifier.create(this.envTools.listEnv("order-service", null))
			.assertNext(
					(result) -> assertThat(result).isEqualTo("No environment properties available for order-service."))
			.verifyComplete();
	}

	@Test
	void listEnv_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("ghost")).thenReturn(Flux.empty());

		StepVerifier.create(this.envTools.listEnv("ghost", null))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'ghost' not found in registry."))
			.verifyComplete();
	}

}
