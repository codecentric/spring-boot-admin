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
import reactor.core.publisher.Mono;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CachesToolsTest {

	private final WireMockServer wireMock = new WireMockServer(Options.DYNAMIC_PORT);

	private InstanceRepository instanceRepository;

	private CachesTools cachesTools;

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
		when(this.instanceRepository.find(any())).thenReturn(Mono.empty());
		InstanceWebClient instanceWebClient = InstanceWebClient.builder().filter(rewriteEndpointUrl()).build();
		this.cachesTools = new CachesTools(
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
	void listCaches_returnsCachesGroupedByCacheManager() {
		when(this.instanceRepository.findByName("payment-service")).thenReturn(Flux.just(instance("payment-service")));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/caches"))
			.willReturn(okJson("{\"cacheManagers\":{" + "\"cacheManager\":{\"caches\":{"
					+ "\"payments\":{\"target\":\"java.util.concurrent.ConcurrentHashMap\"},"
					+ "\"rates\":{\"target\":\"java.util.concurrent.ConcurrentHashMap\"}" + "}}}}")));

		StepVerifier.create(this.cachesTools.listCaches("payment-service")).assertNext((result) -> {
			assertThat(result).contains("Caches for payment-service");
			assertThat(result).contains("[cacheManager]");
			assertThat(result).contains("payments");
			assertThat(result).contains("rates");
			assertThat(result).contains("ConcurrentHashMap");
		}).verifyComplete();
	}

	@Test
	void listCaches_noCacheManagers_returnsNoCachesMessage() {
		when(this.instanceRepository.findByName("order-service")).thenReturn(Flux.just(instance("order-service")));

		this.wireMock.stubFor(get(urlEqualTo("/actuator/caches")).willReturn(okJson("{\"cacheManagers\":{}}")));

		StepVerifier.create(this.cachesTools.listCaches("order-service"))
			.assertNext((result) -> assertThat(result).isEqualTo("No caches available for order-service."))
			.verifyComplete();
	}

	@Test
	void listCaches_appNotFound_returnsNotFoundMessage() {
		when(this.instanceRepository.findByName("ghost")).thenReturn(Flux.empty());

		StepVerifier.create(this.cachesTools.listCaches("ghost"))
			.assertNext((result) -> assertThat(result).isEqualTo("Application 'ghost' not found in registry."))
			.verifyComplete();
	}

}
