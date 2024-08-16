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

package de.codecentric.boot.admin.server.web;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.endpoint.ApiVersion;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.options;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ALLOW;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

public abstract class AbstractInstancesProxyControllerIntegrationTest {

	private static final String ACTUATOR_CONTENT_TYPE = ApiVersion.LATEST.getProducedMimeType().toString()
			+ ";charset=UTF-8";

	private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE = new ParameterizedTypeReference<>() {
	};

	private final WireMockServer wireMock = new WireMockServer(
			WireMockConfiguration.options().dynamicPort().extensions(new ConnectionCloseExtension()));

	private WebTestClient client;

	private String instanceId;

	private ConfigurableApplicationContext context;

	@BeforeAll
	public static void setUp() {
		StepVerifier.setDefaultTimeout(Duration.ofSeconds(600));
	}

	@AfterAll
	public static void tearDown() {
		StepVerifier.resetDefaultTimeout();
	}

	@BeforeEach
	void setup() {
		this.wireMock.start();
	}

	@AfterEach
	void teardown() {
		this.wireMock.stop();
	}

	protected void setUpClient(ConfigurableApplicationContext context) {
		this.context = context;
		this.client = createWebTestClientBuilder().build();
		this.instanceId = registerInstance("/instance1");
	}

	@NotNull
	private WebTestClient.Builder createWebTestClientBuilder() {
		int localPort = this.context.getEnvironment().getProperty("local.server.port", Integer.class, 0);
		return WebTestClient.bindToServer()
			.baseUrl("http://localhost:" + localPort)
			.responseTimeout(Duration.ofSeconds(10));
	}

	@Test
	public void should_return_status_503() {
		// 503 on invalid instance
		this.client.get()
			.uri("/instances/{instanceId}/actuator/info", "UNKNOWN")
			.accept(new MediaType(ApiVersion.LATEST.getProducedMimeType()))
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
	}

	@Test
	public void should_return_status_404() {
		// 404 on non-existent endpoint
		this.client.get()
			.uri("/instances/{instanceId}/actuator/not-exist", this.instanceId)
			.accept(new MediaType(ApiVersion.LATEST.getProducedMimeType()))
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void should_return_status_502() {
		// 502 on invalid response
		this.client.get()
			.uri("/instances/{instanceId}/actuator/invalid", this.instanceId)
			.accept(new MediaType(ApiVersion.LATEST.getProducedMimeType()))
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.BAD_GATEWAY);
	}

	@Test
	public void should_return_status_504() {
		// 504 on read timeout
		this.client.get()
			.uri("/instances/{instanceId}/actuator/timeout", this.instanceId)
			.accept(new MediaType(ApiVersion.LATEST.getProducedMimeType()))
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
	}

	@Test
	public void should_forward_requests() {
		this.client.options()
			.uri("/instances/{instanceId}/actuator/env", this.instanceId)
			.accept(new MediaType(ApiVersion.LATEST.getProducedMimeType()))
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.OK)
			.expectHeader()
			.valueEquals(ALLOW, HttpMethod.HEAD.name(), HttpMethod.GET.name(), HttpMethod.OPTIONS.name());

		this.client.get()
			.uri("/instances/{instanceId}/actuator/test", this.instanceId)
			.accept(new MediaType(ApiVersion.LATEST.getProducedMimeType()))
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.OK)
			.expectBody()
			.json("{ \"foo\" : \"bar\" }");

		this.client.post()
			.uri("/instances/{instanceId}/actuator/post", this.instanceId)
			.bodyValue("PAYLOAD")
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.OK);

		this.wireMock.verify(postRequestedFor(urlEqualTo("/instance1/post")).withRequestBody(equalTo("PAYLOAD")));

		this.client.delete()
			.uri("/instances/{instanceId}/actuator/delete", this.instanceId)
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
			.expectBody()
			.json("{\"error\": \"You're doing it wrong!\"}");

		this.wireMock.verify(deleteRequestedFor(urlEqualTo("/instance1/delete")));
	}

	@Test
	public void should_forward_requests_with_spaces_in_path() {
		this.client.get()
			.uri("/instances/{instanceId}/actuator/test/has spaces", this.instanceId)
			.accept(new MediaType(ApiVersion.LATEST.getProducedMimeType()))
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.OK)
			.expectBody()
			.json("{ \"foo\" : \"bar-with-spaces\" }");

		this.wireMock.verify(getRequestedFor(urlEqualTo("/instance1/test/has%20spaces")));
	}

	@Test
	public void should_forward_requests_to_multiple_instances() {
		this.client = createWebTestClientBuilder().responseTimeout(Duration.ofSeconds(30)).build();

		String instance2Id = registerInstance("/instance2");

		this.client.get()
			.uri("applications/test/actuator/test")
			.accept(new MediaType(ApiVersion.LATEST.getProducedMimeType()))
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.OK)
			.expectBody()
			.jsonPath("$[?(@.instanceId == '" + this.instanceId + "')].status")
			.isEqualTo(200)
			.jsonPath("$[?(@.instanceId == '" + this.instanceId + "')].body")
			.isEqualTo("{ \"foo\" : \"bar\" }")
			.jsonPath("$[?(@.instanceId == '" + instance2Id + "')].status")
			.isEqualTo(200)
			.jsonPath("$[?(@.instanceId == '" + instance2Id + "')].body")
			.isEqualTo("{ \"foo\" : \"bar\" }");

		this.client.post()
			.uri("applications/test/actuator/post")
			.bodyValue("PAYLOAD")
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.OK);

		this.wireMock.verify(postRequestedFor(urlEqualTo("/instance1/post")).withRequestBody(equalTo("PAYLOAD")));
		this.wireMock.verify(postRequestedFor(urlEqualTo("/instance2/post")).withRequestBody(equalTo("PAYLOAD")));

		this.client.delete()
			.uri("applications/test/actuator/delete")
			.exchange()
			.expectStatus()
			.isEqualTo(HttpStatus.OK)
			.expectBody()
			.jsonPath("$[?(@.instanceId == '" + this.instanceId + "')].status")
			.isEqualTo(500)
			.jsonPath("$[?(@.instanceId == '" + this.instanceId + "')].body")
			.isEqualTo("{\"error\": \"You're doing it wrong!\"}")
			.jsonPath("$[?(@.instanceId == '" + instance2Id + "')].status")
			.isEqualTo(500)
			.jsonPath("$[?(@.instanceId == '" + instance2Id + "')].body")
			.isEqualTo("{\"error\": \"You're doing it wrong!\"}");

		this.wireMock.verify(deleteRequestedFor(urlEqualTo("/instance1/delete")));
		this.wireMock.verify(deleteRequestedFor(urlEqualTo("/instance2/delete")));
	}

	private void stubForInstance(String managementPath) {
		String managementUrl = this.wireMock.url(managementPath);

		//@formatter:off
		String actuatorIndex = "{ \"_links\": { " +
			"\"env\": { \"href\": \"" + managementUrl + "/env\", \"templated\": false }," +
			"\"test\": { \"href\": \"" + managementUrl + "/test\", \"templated\": false }," +
			"\"post\": { \"href\": \"" + managementUrl + "/post\", \"templated\": false }," +
			"\"delete\": { \"href\": \"" + managementUrl + "/delete\", \"templated\": false }," +
			"\"invalid\": { \"href\": \"" + managementUrl + "/invalid\", \"templated\": false }," +
			"\"timeout\": { \"href\": \"" + managementUrl + "/timeout\", \"templated\": false }" +
			" } }";
		//@formatter:on
		this.wireMock.stubFor(get(urlEqualTo(managementPath + "/health")).willReturn(ok("{ \"status\" : \"UP\" }")
			.withHeader(CONTENT_TYPE, ApiVersion.LATEST.getProducedMimeType().toString())));
		this.wireMock.stubFor(get(urlEqualTo(managementPath + "/info"))
			.willReturn(ok("{ }").withHeader(CONTENT_TYPE, ACTUATOR_CONTENT_TYPE)));
		this.wireMock.stubFor(options(urlEqualTo(managementPath + "/env")).willReturn(
				ok().withHeader(ALLOW, HttpMethod.HEAD.name(), HttpMethod.GET.name(), HttpMethod.OPTIONS.name())));
		this.wireMock.stubFor(get(urlEqualTo(managementPath + ""))
			.willReturn(ok(actuatorIndex).withHeader(CONTENT_TYPE, ACTUATOR_CONTENT_TYPE)));
		this.wireMock.stubFor(get(urlEqualTo(managementPath + "/invalid"))
			.willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
		this.wireMock.stubFor(get(urlEqualTo(managementPath + "/timeout")).willReturn(ok().withFixedDelay(10000)));
		this.wireMock.stubFor(get(urlEqualTo(managementPath + "/test"))
			.willReturn(ok("{ \"foo\" : \"bar\" }").withHeader(CONTENT_TYPE, ACTUATOR_CONTENT_TYPE)));
		this.wireMock.stubFor(get(urlEqualTo(managementPath + "/test/has%20spaces"))
			.willReturn(ok("{ \"foo\" : \"bar-with-spaces\" }").withHeader(CONTENT_TYPE, ACTUATOR_CONTENT_TYPE)));
		this.wireMock.stubFor(post(urlEqualTo(managementPath + "/post")).willReturn(ok()));
		this.wireMock.stubFor(delete(urlEqualTo(managementPath + "/delete"))
			.willReturn(serverError().withBody("{\"error\": \"You're doing it wrong!\"}")
				.withHeader(CONTENT_TYPE, ACTUATOR_CONTENT_TYPE)));
	}

	private String registerInstance(String managementPath) {
		stubForInstance(managementPath);

		AtomicReference<String> instanceId = new AtomicReference<>();
		StepVerifier.create(getEventStream())
			.expectSubscription()
			.then(() -> instanceId.set(sendRegistration(managementPath)))
			.thenConsumeWhile((event) -> !event.get("type").equals("ENDPOINTS_DETECTED"))
			.assertNext((event) -> assertThat(event).containsEntry("type", "ENDPOINTS_DETECTED"))
			.thenCancel()
			.verify();
		return instanceId.get();
	}

	private String sendRegistration(String managementPath) {
		String managementUrl = this.wireMock.url(managementPath);

		//@formatter:off
		String registration = "{ \"name\": \"test\", " +
			"\"healthUrl\": \"" + managementUrl + "/health\", " +
			"\"managementUrl\": \"" + managementUrl + "\" }";

		EntityExchangeResult<Map<String, Object>> result = this.client.post()
			.uri("/instances")
			.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)
			.bodyValue(registration)
			.exchange()
			.expectStatus().isCreated()
			.expectBody(RESPONSE_TYPE)
			.returnResult();
		//@formatter:on
		assertThat(result.getResponseBody()).containsKeys("id");
		return result.getResponseBody().get("id").toString();
	}

	private Flux<Map<String, Object>> getEventStream() {
		//@formatter:off
		return this.client.get().uri("/instances/events").accept(MediaType.TEXT_EVENT_STREAM)
			.exchange()
			.expectStatus().isOk()
			.returnResult(RESPONSE_TYPE).getResponseBody();
		//@formatter:on
	}

}
