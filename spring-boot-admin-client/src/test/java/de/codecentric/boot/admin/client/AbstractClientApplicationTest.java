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

package de.codecentric.boot.admin.client;

import java.net.InetAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.jackson.autoconfigure.JacksonProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import de.codecentric.boot.admin.client.registration.ApplicationRegistrator;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.awaitility.Awaitility.await;

@Slf4j
public abstract class AbstractClientApplicationTest {

	private final WireMockServer wireMock = new WireMockServer(
			options().dynamicPort().notifier(new ConsoleNotifier(true)));

	private SpringApplication application;

	private ConfigurableApplicationContext instance;

	private static final CountDownLatch cdl = new CountDownLatch(1);

	protected void setUp(WebApplicationType type) {
		setUpWiremock();
		setUpApplication(type);
	}

	private void setUpWiremock() {
		this.wireMock.start();
		ResponseDefinitionBuilder response = created().withHeader("Content-Type", "application/json")
			.withHeader("Connection", "close")
			.withHeader("Location", this.wireMock.url("/instances/abcdef"))
			.withBody("{ \"id\" : \"abcdef\" }");
		this.wireMock.stubFor(post(urlEqualTo("/instances")).willReturn(response));
	}

	private void setUpApplication(WebApplicationType type) {
		this.application = new SpringApplication(TestClientApplication.class);
		this.application.setWebApplicationType(type);
	}

	private void setUpApplicationContext(String... additionalArgs) {
		Stream<String> defaultArgs = Stream.of("--spring.application.name=Test-Client", "--server.port=0",
				"--management.endpoints.web.base-path=/mgmt", "--endpoints.health.enabled=true",
				"--spring.boot.admin.client.url=" + this.wireMock.url("/"));

		String[] args = Stream.concat(defaultArgs, Arrays.stream(additionalArgs)).toArray(String[]::new);

		this.instance = this.application.run(args);
	}

	@AfterEach
	void tearDown() {
		this.wireMock.stop();
		if (this.instance != null) {
			this.instance.close();
		}
	}

	/**
	 * @see JacksonProperties
	 * @see PropertyNamingStrategies#LOWER_CAMEL_CASE
	 */
	@Test
	public void test_context() throws Exception {
		setUpApplicationContext();

		String hostName = InetAddress.getLocalHost().getCanonicalHostName();
		String serviceHost = "http://" + hostName + ":" + getServerPort();
		String managementHost = "http://" + hostName + ":" + getManagementPort();
		RequestPatternBuilder request = postRequestedFor(urlEqualTo("/instances"));
		request.withHeader("Content-Type", equalTo("application/json"))
			.withRequestBody(matchingJsonPath("$.name", equalTo("Test-Client")))
			.withRequestBody(matchingJsonPath("$.healthUrl", equalTo(managementHost + "/mgmt/health")))
			.withRequestBody(matchingJsonPath("$.managementUrl", equalTo(managementHost + "/mgmt")))
			.withRequestBody(matchingJsonPath("$.serviceUrl", equalTo(serviceHost + "/")))
			.withRequestBody(matchingJsonPath("$.metadata.startup", matching(".+")));

		log.info("Waiting for registration at mocked sba-server for '{}' ...", this.instance);
		cdl.await(5_000, TimeUnit.MILLISECONDS);
		await().atMost(Duration.ofMillis(2500)).untilAsserted(() -> this.wireMock.verify(request));
	}

	/**
	 * @see JacksonProperties
	 * @see PropertyNamingStrategies#SNAKE_CASE
	 */
	@Test
	public void test_context_with_snake_case() throws Exception {
		setUpApplicationContext("--spring.jackson.property-naming-strategy=SNAKE_CASE");

		String hostName = InetAddress.getLocalHost().getCanonicalHostName();
		String serviceHost = "http://" + hostName + ":" + getServerPort();
		String managementHost = "http://" + hostName + ":" + getManagementPort();
		RequestPatternBuilder request = postRequestedFor(urlEqualTo("/instances"));
		request.withHeader("Content-Type", equalTo("application/json"))
			.withRequestBody(matchingJsonPath("$.name", equalTo("Test-Client")))
			.withRequestBody(matchingJsonPath("$.health_url", equalTo(managementHost + "/mgmt/health")))
			.withRequestBody(matchingJsonPath("$.management_url", equalTo(managementHost + "/mgmt")))
			.withRequestBody(matchingJsonPath("$.service_url", equalTo(serviceHost + "/")))
			.withRequestBody(matchingJsonPath("$.metadata.startup", matching(".+")));

		log.info("Waiting for registration at mocked sba-server for '{}' ...", this.instance);
		cdl.await(5_000, TimeUnit.MILLISECONDS);
		await().atMost(Duration.ofMillis(2500)).untilAsserted(() -> this.wireMock.verify(request));
	}

	private int getServerPort() {
		return this.instance.getEnvironment().getProperty("local.server.port", Integer.class, 0);
	}

	private int getManagementPort() {
		return this.instance.getEnvironment().getProperty("local.management.port", Integer.class, 0);
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	public static class TestClientApplication {

		@Autowired
		private ApplicationRegistrator registrator;

		@EventListener
		public void ping(ApplicationReadyEvent ev) {
			new Thread(() -> {
				log.info("Waiting for registration at mocked sba-server for '{}' ...", this);
				await().atMost(Duration.ofMillis(3_500)).until(() -> this.registrator.getRegisteredId() != null);
				log.info("Found registration id '{}' for '{}'", this.registrator.getRegisteredId(), this);
				cdl.countDown();
			}).start();
		}

	}

}
