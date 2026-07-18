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
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.rewriteEndpointUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthToolsTest {

	private final WireMockServer wireMock = new WireMockServer(Options.DYNAMIC_PORT);

	private InstanceRepository instanceRepository;

	private HealthTools healthTools;

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
		this.healthTools = new HealthTools(
				new ActuatorClient(this.instanceRepository, instanceWebClient, Duration.ofMillis(450)));
	}

	@AfterEach
	void tearDown() {
		this.wireMock.stop();
	}

	@Test
	void getHealth_appFoundAndActuatorReachable_returnsHealthSummary() {
		Instance instance = Instance.create(InstanceId.of("id-1"))
			.register(Registration.create("payment-service", this.wireMock.url("/actuator/health")).build())
			.withEndpoints(Endpoints.single(Endpoint.HEALTH, this.wireMock.url("/actuator/health")))
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance));

		String healthBody = "{\"status\":\"UP\",\"components\":{\"db\":{\"status\":\"UP\"},\"diskSpace\":{\"status\":\"UP\"}}}";
		this.wireMock.stubFor(get(urlEqualTo("/actuator/health")).willReturn(okJson(healthBody)));

		StepVerifier.create(this.healthTools.getHealth("payment-service")).assertNext((result) -> {
			assertThat(result).contains("payment-service health: UP");
			assertThat(result).contains("db: UP");
			assertThat(result).contains("diskSpace: UP");
		}).verifyComplete();
	}

	@Test
	void getHealth_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("unknown")).thenReturn(Flux.empty());

		StepVerifier.create(this.healthTools.getHealth("unknown"))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'unknown' not found in registry."))
			.verifyComplete();
	}

	@Test
	void getHealth_actuatorTimeout_returnsErrorMessage() {
		Instance instance = Instance.create(InstanceId.of("id-2"))
			.register(Registration.create("slow-service", this.wireMock.url("/actuator/health")).build())
			.withEndpoints(Endpoints.single(Endpoint.HEALTH, this.wireMock.url("/actuator/health")))
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("slow-service")).thenReturn(Flux.just(instance));

		this.wireMock
			.stubFor(get(urlEqualTo("/actuator/health")).willReturn(aResponse().withFixedDelay(600).withBody("{}")));

		StepVerifier.create(this.healthTools.getHealth("slow-service"))
			.assertNext((result) -> assertThat(result).contains("Error retrieving health for slow-service"))
			.verifyComplete();
	}

	@Test
	void getStatus_appFound_returnsStatusLine() {
		Instance instance = Instance.create(InstanceId.of("id-3"))
			.register(Registration.create("order-service", "http://localhost/health").build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("order-service")).thenReturn(Flux.just(instance));

		StepVerifier.create(this.healthTools.getStatus("order-service"))
			.assertNext((result) -> assertThat(result).isEqualTo("order-service status: UP"))
			.verifyComplete();
	}

	@Test
	void getStatus_appDown_returnsDownStatus() {
		Instance instance = Instance.create(InstanceId.of("id-4"))
			.register(Registration.create("checkout-service", "http://localhost/health").build())
			.withStatusInfo(StatusInfo.ofDown());

		when(this.instanceRepository.findByName("checkout-service")).thenReturn(Flux.just(instance));

		StepVerifier.create(this.healthTools.getStatus("checkout-service"))
			.assertNext((result) -> assertThat(result).isEqualTo("checkout-service status: DOWN"))
			.verifyComplete();
	}

	@Test
	void getStatus_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("ghost")).thenReturn(Flux.empty());

		StepVerifier.create(this.healthTools.getStatus("ghost"))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'ghost' not found in registry."))
			.verifyComplete();
	}

}
