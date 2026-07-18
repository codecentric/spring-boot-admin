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

class HttpExchangesToolsTest {

	private final WireMockServer wireMock = new WireMockServer(Options.DYNAMIC_PORT);

	private InstanceRepository instanceRepository;

	private HttpExchangesTools httpExchangesTools;

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
		this.httpExchangesTools = new HttpExchangesTools(
				new ActuatorClient(this.instanceRepository, instanceWebClient, Duration.ofMillis(450)));
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
	void getHttpExchanges_returnsRecentExchanges() {
		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance("payment-service")));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/httpexchanges"))
			.willReturn(okJson("{\"exchanges\":[" + "{\"timestamp\":\"2026-01-01T10:00:00Z\","
					+ "\"request\":{\"method\":\"GET\",\"uri\":\"http://localhost/api/payments\"},"
					+ "\"response\":{\"status\":200}," + "\"timeTaken\":\"PT0.042S\"},"
					+ "{\"timestamp\":\"2026-01-01T10:00:01Z\","
					+ "\"request\":{\"method\":\"POST\",\"uri\":\"http://localhost/api/payments\"},"
					+ "\"response\":{\"status\":422}," + "\"timeTaken\":\"PT0.015S\"}" + "]}")));

		StepVerifier.create(this.httpExchangesTools.getHttpExchanges("payment-service", null)).assertNext((result) -> {
			assertThat(result).contains("Recent HTTP exchanges for payment-service");
			assertThat(result).contains("GET http://localhost/api/payments -> 200");
			assertThat(result).contains("POST http://localhost/api/payments -> 422");
			assertThat(result).contains("PT0.042S");
		}).verifyComplete();
	}

	@Test
	void getHttpExchanges_withLimit_returnsLimitedExchanges() {
		when(this.instanceRepository.findByName("order-service")).thenReturn(Flux.just(instance("order-service")));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/httpexchanges")).willReturn(okJson(
				"{\"exchanges\":[" + "{\"request\":{\"method\":\"GET\",\"uri\":\"/a\"},\"response\":{\"status\":200}},"
						+ "{\"request\":{\"method\":\"GET\",\"uri\":\"/b\"},\"response\":{\"status\":200}},"
						+ "{\"request\":{\"method\":\"GET\",\"uri\":\"/c\"},\"response\":{\"status\":200}}" + "]}")));

		StepVerifier.create(this.httpExchangesTools.getHttpExchanges("order-service", 2)).assertNext((result) -> {
			assertThat(result).contains("showing 2 of 3");
			assertThat(result).contains("/b");
			assertThat(result).contains("/c");
			assertThat(result).doesNotContain("/a");
		}).verifyComplete();
	}

	@Test
	void getHttpExchanges_noExchanges_returnsNoContentMessage() {
		when(this.instanceRepository.findByName("order-service")).thenReturn(Flux.just(instance("order-service")));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/httpexchanges")).willReturn(okJson("{\"exchanges\":[]}")));

		StepVerifier.create(this.httpExchangesTools.getHttpExchanges("order-service", null))
			.assertNext((result) -> assertThat(result).isEqualTo("No HTTP exchanges recorded for order-service."))
			.verifyComplete();
	}

	@Test
	void getHttpExchanges_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("ghost")).thenReturn(Flux.empty());

		StepVerifier.create(this.httpExchangesTools.getHttpExchanges("ghost", null))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'ghost' not found in registry."))
			.verifyComplete();
	}

}
