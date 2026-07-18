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
import static com.github.tomakehurst.wiremock.client.WireMock.noContent;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.rewriteEndpointUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoggersToolsTest {

	private final WireMockServer wireMock = new WireMockServer(Options.DYNAMIC_PORT);

	private InstanceRepository instanceRepository;

	private LoggersTools loggersTools;

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
		this.loggersTools = new LoggersTools(this.instanceRepository, instanceWebClient);
	}

	@AfterEach
	void tearDown() {
		this.wireMock.stop();
	}

	private Instance instance(String name) {
		return Instance.create(InstanceId.of("id-" + name))
			.register(Registration.create(name, this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());
	}

	@Test
	void listLoggers_returnsLoggersGroupedByLevel() {
		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance("payment-service")));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/loggers"))
			.willReturn(okJson("{\"levels\":[\"TRACE\",\"DEBUG\",\"INFO\",\"WARN\",\"ERROR\",\"OFF\"],"
					+ "\"loggers\":{" + "\"ROOT\":{\"configuredLevel\":\"INFO\",\"effectiveLevel\":\"INFO\"},"
					+ "\"com.example\":{\"configuredLevel\":null,\"effectiveLevel\":\"INFO\"}" + "}}")));

		StepVerifier.create(this.loggersTools.listLoggers("payment-service", null)).assertNext((result) -> {
			assertThat(result).contains("Loggers for payment-service");
			assertThat(result).contains("ROOT");
			assertThat(result).contains("effective=INFO");
			assertThat(result).contains("com.example");
		}).verifyComplete();
	}

	@Test
	void listLoggers_withFilter_returnsOnlyMatchingLoggers() {
		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance("payment-service")));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/loggers"))
			.willReturn(okJson("{\"loggers\":{" + "\"ROOT\":{\"effectiveLevel\":\"INFO\"},"
					+ "\"com.example.PaymentService\":{\"effectiveLevel\":\"DEBUG\"},"
					+ "\"org.springframework\":{\"effectiveLevel\":\"WARN\"}" + "}}")));

		StepVerifier.create(this.loggersTools.listLoggers("payment-service", "example")).assertNext((result) -> {
			assertThat(result).contains("filtered by \"example\"");
			assertThat(result).contains("com.example.PaymentService");
			assertThat(result).doesNotContain("ROOT");
			assertThat(result).doesNotContain("org.springframework");
		}).verifyComplete();
	}

	@Test
	void listLoggers_withFilter_noMatches_returnsMessage() {
		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance("payment-service")));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/loggers"))
			.willReturn(okJson("{\"loggers\":{\"ROOT\":{\"effectiveLevel\":\"INFO\"}}}")));

		StepVerifier.create(this.loggersTools.listLoggers("payment-service", "nomatch"))
			.assertNext((result) -> assertThat(result).isEqualTo("No loggers matching 'nomatch' for payment-service."))
			.verifyComplete();
	}

	@Test
	void listLoggers_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("ghost")).thenReturn(Flux.empty());

		StepVerifier.create(this.loggersTools.listLoggers("ghost", null))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'ghost' not found in registry."))
			.verifyComplete();
	}

	@Test
	void getLogger_returnsEffectiveAndConfiguredLevel() {
		when(this.instanceRepository.findByName("order-service")).thenReturn(Flux.just(instance("order-service")));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/loggers/com.example.OrderService"))
			.willReturn(okJson("{\"configuredLevel\":\"DEBUG\",\"effectiveLevel\":\"DEBUG\"}")));

		StepVerifier.create(this.loggersTools.getLogger("order-service", "com.example.OrderService"))
			.assertNext((result) -> {
				assertThat(result).contains("order-service");
				assertThat(result).contains("com.example.OrderService");
				assertThat(result).contains("effectiveLevel: DEBUG");
				assertThat(result).contains("configuredLevel: DEBUG");
			})
			.verifyComplete();
	}

	@Test
	void getLogger_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("ghost")).thenReturn(Flux.empty());

		StepVerifier.create(this.loggersTools.getLogger("ghost", "com.example.Foo"))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'ghost' not found in registry."))
			.verifyComplete();
	}

	@Test
	void setLoggerLevel_success_returnsConfirmation() {
		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance("payment-service")));

		this.wireMock.stubFor(post(urlEqualTo("/actuator/loggers/com.example.PaymentService")).willReturn(noContent()));

		StepVerifier.create(this.loggersTools.setLoggerLevel("payment-service", "com.example.PaymentService", "DEBUG"))
			.assertNext((result) -> {
				assertThat(result).contains("com.example.PaymentService");
				assertThat(result).contains("payment-service");
				assertThat(result).contains("DEBUG");
			})
			.verifyComplete();
	}

	@Test
	void setLoggerLevel_nullLevel_resetsToInherited() {
		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance("payment-service")));

		this.wireMock.stubFor(post(urlEqualTo("/actuator/loggers/com.example.PaymentService")).willReturn(noContent()));

		StepVerifier.create(this.loggersTools.setLoggerLevel("payment-service", "com.example.PaymentService", null))
			.assertNext((result) -> assertThat(result).contains("inherited"))
			.verifyComplete();
	}

	@Test
	void setLoggerLevel_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("ghost")).thenReturn(Flux.empty());

		StepVerifier.create(this.loggersTools.setLoggerLevel("ghost", "com.example.Foo", "DEBUG"))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'ghost' not found in registry."))
			.verifyComplete();
	}

}
