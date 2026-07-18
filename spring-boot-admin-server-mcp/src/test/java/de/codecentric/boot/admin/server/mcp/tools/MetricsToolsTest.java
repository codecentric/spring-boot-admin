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

class MetricsToolsTest {

	private final WireMockServer wireMock = new WireMockServer(Options.DYNAMIC_PORT);

	private InstanceRepository instanceRepository;

	private MetricsTools metricsTools;

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
		this.metricsTools = new MetricsTools(
				new ActuatorClient(this.instanceRepository, instanceWebClient, Duration.ofMillis(450)));
	}

	@AfterEach
	void tearDown() {
		this.wireMock.stop();
	}

	@Test
	void listMetrics_appFound_returnsMetricNames() {
		Instance instance = Instance.create(InstanceId.of("id-1"))
			.register(Registration.create("payment-service", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/metrics"))
			.willReturn(okJson("{\"names\":[\"jvm.memory.used\",\"jvm.memory.max\",\"system.cpu.usage\"]}")));

		StepVerifier.create(this.metricsTools.listMetrics("payment-service")).assertNext((result) -> {
			assertThat(result).contains("Available metrics for payment-service");
			assertThat(result).contains("jvm.memory.used");
			assertThat(result).contains("system.cpu.usage");
		}).verifyComplete();
	}

	@Test
	void listMetrics_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("unknown")).thenReturn(Flux.empty());

		StepVerifier.create(this.metricsTools.listMetrics("unknown"))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'unknown' not found in registry."))
			.verifyComplete();
	}

	@Test
	void getMetrics_appFound_returnsMetricValue() {
		Instance instance = Instance.create(InstanceId.of("id-2"))
			.register(Registration.create("order-service", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("order-service")).thenReturn(Flux.just(instance));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/metrics/jvm.memory.used"))
			.willReturn(okJson("{\"name\":\"jvm.memory.used\",\"baseUnit\":\"bytes\","
					+ "\"measurements\":[{\"statistic\":\"VALUE\",\"value\":1234567890}]}")));

		StepVerifier.create(this.metricsTools.getMetrics("order-service", "jvm.memory.used")).assertNext((result) -> {
			assertThat(result).contains("order-service");
			assertThat(result).contains("jvm.memory.used");
			assertThat(result).contains("VALUE");
			assertThat(result).contains("bytes");
		}).verifyComplete();
	}

	@Test
	void getMetrics_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("ghost")).thenReturn(Flux.empty());

		StepVerifier.create(this.metricsTools.getMetrics("ghost", "jvm.memory.used"))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'ghost' not found in registry."))
			.verifyComplete();
	}

}
