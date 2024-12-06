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

package de.codecentric.boot.admin.server.services.endpoints;

import java.time.Duration;

import javax.net.ssl.SSLException;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.Fault;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.endpoint.ApiVersion;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.services.ApiMediaTypeHandler;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.retry;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.rewriteEndpointUrl;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.timeout;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

public class QueryIndexEndpointStrategyTest {

	private final ApiMediaTypeHandler apiMediaTypeHandler = new ApiMediaTypeHandler();

	public WireMockServer wireMock = new WireMockServer(wireMockConfig().dynamicPort().dynamicHttpsPort());

	private InstanceWebClient instanceWebClient = InstanceWebClient.builder()
		.webClient(WebClient.builder().clientConnector(httpConnector()))
		.filter(rewriteEndpointUrl())
		.filter(retry(0, singletonMap(Endpoint.ACTUATOR_INDEX, 1)))
		.filter(timeout(Duration.ofSeconds(1), emptyMap()))
		.build();

	@BeforeEach
	void setUp() {
		wireMock.start();
	}

	@AfterEach
	void tearDown() {
		wireMock.stop();
	}

	@Test
	public void should_return_endpoints() {
		// given
		Instance instance = Instance.create(InstanceId.of("id"))
			.register(Registration.create("test", this.wireMock.url("/mgmt/health"))
				.managementUrl(this.wireMock.url("/mgmt"))
				.build());

		String host = "https://localhost:" + this.wireMock.httpsPort();
		String body = "{\"_links\":{\"metrics-requiredMetricName\":{\"templated\":true,\"href\":\"" + host
				+ "/mgmt/metrics/{requiredMetricName}\"},\"self\":{\"templated\":false,\"href\":\"" + host
				+ "/mgmt\"},\"metrics\":{\"templated\":false,\"href\":\"" + host
				+ "/mgmt/stats\"},\"info\":{\"templated\":false,\"href\":\"" + host + "/mgmt/info\"}}}";

		this.wireMock.stubFor(get("/mgmt")
			.willReturn(ok(body).withHeader("Content-Type", ApiVersion.LATEST.getProducedMimeType().toString())));

		QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(this.instanceWebClient,
				this.apiMediaTypeHandler);

		// when
		StepVerifier.create(strategy.detectEndpoints(instance))
			// then
			.expectNext(Endpoints.single("metrics", host + "/mgmt/stats").withEndpoint("info", host + "/mgmt/info"))//
			.verifyComplete();
	}

	@Test
	public void should_return_endpoints_with_aligned_scheme() {
		// given
		Instance instance = Instance.create(InstanceId.of("id"))
			.register(Registration.create("test", this.wireMock.url("/mgmt/health"))
				.managementUrl(this.wireMock.url("/mgmt"))
				.build());

		String host = "http://localhost:" + this.wireMock.httpsPort();
		String body = "{\"_links\":{\"metrics-requiredMetricName\":{\"templated\":true,\"href\":\"" + host
				+ "/mgmt/metrics/{requiredMetricName}\"},\"self\":{\"templated\":false,\"href\":\"" + host
				+ "/mgmt\"},\"metrics\":{\"templated\":false,\"href\":\"" + host
				+ "/mgmt/stats\"},\"info\":{\"templated\":false,\"href\":\"" + host + "/mgmt/info\"}}}";

		this.wireMock.stubFor(get("/mgmt")
			.willReturn(ok(body).withHeader("Content-Type", ApiVersion.LATEST.getProducedMimeType().toString())));

		QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(this.instanceWebClient,
				this.apiMediaTypeHandler);

		// when
		String secureHost = "https://localhost:" + this.wireMock.httpsPort();
		StepVerifier.create(strategy.detectEndpoints(instance))
			// then
			.expectNext(Endpoints.single("metrics", secureHost + "/mgmt/stats")
				.withEndpoint("info", secureHost + "/mgmt/info"))//
			.verifyComplete();
	}

	@Test
	public void should_return_empty_on_empty_endpoints() {
		// given
		Instance instance = Instance.create(InstanceId.of("id"))
			.register(Registration.create("test", this.wireMock.url("/mgmt/health"))
				.managementUrl(this.wireMock.url("/mgmt"))
				.build());

		String body = "{\"_links\":{}}";
		this.wireMock.stubFor(get("/mgmt")
			.willReturn(okJson(body).withHeader("Content-Type", ApiVersion.LATEST.getProducedMimeType().toString())));

		QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(this.instanceWebClient,
				this.apiMediaTypeHandler);

		// when
		StepVerifier.create(strategy.detectEndpoints(instance))
			// then
			.verifyComplete();
	}

	@Test
	public void should_return_empty_on_not_found() {
		// given
		Instance instance = Instance.create(InstanceId.of("id"))
			.register(Registration.create("test", this.wireMock.url("/mgmt/health"))
				.managementUrl(this.wireMock.url("/mgmt"))
				.build());

		this.wireMock.stubFor(get("/mgmt").willReturn(notFound()));

		QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(this.instanceWebClient,
				this.apiMediaTypeHandler);

		// when
		StepVerifier.create(strategy.detectEndpoints(instance))
			// then
			.verifyComplete();
	}

	@Test
	public void should_return_empty_on_error() {
		// given
		Instance instance = Instance.create(InstanceId.of("id"))
			.register(Registration.create("test", this.wireMock.url("/mgmt/health"))
				.managementUrl(this.wireMock.url("/mgmt"))
				.build());

		this.wireMock.stubFor(get("/mgmt").willReturn(aResponse().withFault(Fault.EMPTY_RESPONSE)));

		QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(this.instanceWebClient,
				this.apiMediaTypeHandler);

		// when
		StepVerifier.create(strategy.detectEndpoints(instance))
			// then
			.verifyComplete();
	}

	@Test
	public void should_return_empty_on_wrong_content_type() {
		// given
		Instance instance = Instance.create(InstanceId.of("id"))
			.register(Registration.create("test", this.wireMock.url("/mgmt/health"))
				.managementUrl(this.wireMock.url("/mgmt"))
				.build());

		String body = "HELLOW WORLD";
		this.wireMock.stubFor(get("/mgmt").willReturn(ok(body).withHeader("Content-Type", MediaType.TEXT_PLAIN_VALUE)));

		QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(this.instanceWebClient,
				this.apiMediaTypeHandler);

		// when
		StepVerifier.create(strategy.detectEndpoints(instance))
			// then
			.verifyComplete();
	}

	@Test
	public void should_return_empty_when_mgmt_equals_service_url() {
		// given
		Instance instance = Instance.create(InstanceId.of("id"))
			.register(Registration.create("test", this.wireMock.url("/app/health"))
				.managementUrl(this.wireMock.url("/app"))
				.serviceUrl(this.wireMock.url("/app"))
				.build());

		QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(this.instanceWebClient,
				this.apiMediaTypeHandler);

		// when/then
		StepVerifier.create(strategy.detectEndpoints(instance)).verifyComplete();
		this.wireMock.verify(0, anyRequestedFor(urlPathEqualTo("/app")));
	}

	@Test
	public void should_retry() {
		// given
		Instance instance = Instance.create(InstanceId.of("id"))
			.register(Registration.create("test", this.wireMock.url("/mgmt/health"))
				.managementUrl(this.wireMock.url("/mgmt"))
				.build());

		String body = "{\"_links\":{\"metrics-requiredMetricName\":{\"templated\":true,\"href\":\"/mgmt/metrics/{requiredMetricName}\"},\"self\":{\"templated\":false,\"href\":\"/mgmt\"},\"metrics\":{\"templated\":false,\"href\":\"/mgmt/stats\"},\"info\":{\"templated\":false,\"href\":\"/mgmt/info\"}}}";

		this.wireMock.stubFor(get("/mgmt").inScenario("retry")
			.whenScenarioStateIs(STARTED)
			.willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER))
			.willSetStateTo("recovered"));

		this.wireMock.stubFor(get("/mgmt").inScenario("retry")
			.whenScenarioStateIs("recovered")
			.willReturn(ok(body).withHeader("Content-Type", ApiVersion.LATEST.getProducedMimeType().toString())));

		QueryIndexEndpointStrategy strategy = new QueryIndexEndpointStrategy(this.instanceWebClient,
				this.apiMediaTypeHandler);

		// when
		StepVerifier.create(strategy.detectEndpoints(instance))
			// then
			.expectNext(Endpoints.single("metrics", "/mgmt/stats").withEndpoint("info", "/mgmt/info"))//
			.verifyComplete();
	}

	private ReactorClientHttpConnector httpConnector() {
		HttpClient client = HttpClient.create().secure((ssl) -> {
			try {
				SslContextBuilder sslCtx = SslContextBuilder.forClient()
					.trustManager(InsecureTrustManagerFactory.INSTANCE);
				ssl.sslContext(sslCtx.build());
			}
			catch (SSLException ex) {
				throw new RuntimeException(ex);
			}
		});
		return new ReactorClientHttpConnector(client);
	}

}
