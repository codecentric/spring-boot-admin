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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.rewriteEndpointUrl;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogsToolsTest {

	private final WireMockServer wireMock = new WireMockServer(Options.DYNAMIC_PORT);

	private InstanceRepository instanceRepository;

	private LogsTools logsTools;

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
		this.logsTools = new LogsTools(
				new ActuatorClient(this.instanceRepository, instanceWebClient, Duration.ofMillis(450)));
	}

	@AfterEach
	void tearDown() {
		this.wireMock.stop();
	}

	@Test
	void getLogs_appFound_returnsLastNLines() {
		Instance instance = Instance.create(InstanceId.of("id-1"))
			.register(Registration.create("payment-service", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance));

		String logContent = "line1\nline2\nline3\nline4\nline5";
		this.wireMock.stubFor(get(urlEqualTo("/actuator/logfile"))
			.willReturn(aResponse().withStatus(200).withBody(logContent).withHeader("Content-Type", "text/plain")));

		StepVerifier.create(this.logsTools.getLogs("payment-service", 3)).assertNext((result) -> {
			assertThat(result).contains("payment-service");
			assertThat(result).contains("line3");
			assertThat(result).contains("line4");
			assertThat(result).contains("line5");
			assertThat(result).doesNotContain("line1");
			assertThat(result).doesNotContain("line2");
		}).verifyComplete();
	}

	@Test
	void getLogs_emptyLogfile_returnsNoContentMessage() {
		Instance instance = Instance.create(InstanceId.of("id-2"))
			.register(Registration.create("order-service", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("order-service")).thenReturn(Flux.just(instance));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/logfile"))
			.willReturn(aResponse().withStatus(200).withBody("").withHeader("Content-Type", "text/plain")));

		StepVerifier.create(this.logsTools.getLogs("order-service", null))
			.assertNext((result) -> assertThat(result).contains("No log content available"))
			.verifyComplete();
	}

	@Test
	void getLogs_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("ghost")).thenReturn(Flux.empty());

		StepVerifier.create(this.logsTools.getLogs("ghost", null))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'ghost' not found in registry."))
			.verifyComplete();
	}

	@Test
	void getLogs_defaultLines_returns50Lines() {
		Instance instance = Instance.create(InstanceId.of("id-3"))
			.register(Registration.create("checkout-service", this.wireMock.url("/actuator/health"))
				.managementUrl(this.wireMock.url("/actuator"))
				.build())
			.withStatusInfo(StatusInfo.ofUp());

		when(this.instanceRepository.findByName("checkout-service")).thenReturn(Flux.just(instance));

		StringBuilder sb = new StringBuilder();
		for (int i = 1; i <= 100; i++) {
			sb.append("line").append(i).append("\n");
		}
		this.wireMock.stubFor(get(urlEqualTo("/actuator/logfile"))
			.willReturn(aResponse().withStatus(200).withBody(sb.toString()).withHeader("Content-Type", "text/plain")));

		StepVerifier.create(this.logsTools.getLogs("checkout-service", null)).assertNext((result) -> {
			assertThat(result).contains("Last 50 lines");
			assertThat(result).contains("line100");
			assertThat(result).doesNotContain("line50\n").contains("line51");
		}).verifyComplete();
	}

}
