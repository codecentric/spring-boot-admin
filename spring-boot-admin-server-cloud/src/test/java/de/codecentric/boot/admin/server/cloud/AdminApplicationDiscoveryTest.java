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

package de.codecentric.boot.admin.server.cloud;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.discovery.simple.InstanceProperties;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class AdminApplicationDiscoveryTest {

	private ConfigurableApplicationContext instance;

	private SimpleDiscoveryProperties simpleDiscovery;

	private WebTestClient webClient;

	private int port;

	@BeforeEach
	void setUp() {
		this.instance = new SpringApplicationBuilder().sources(TestAdminApplication.class)
			.web(WebApplicationType.REACTIVE)
			.run("--server.port=0", "--management.endpoints.web.base-path=/mgmt",
					"--management.endpoints.web.exposure.include=info,health", "--info.test=foobar",
					"--eureka.client.enabled=false", "--spring.cloud.kubernetes.enabled=false",
					"--spring.cloud.kubernetes.discovery.enabled=false", "--management.info.env.enabled=true");

		this.simpleDiscovery = this.instance.getBean(SimpleDiscoveryProperties.class);

		this.port = this.instance.getEnvironment().getProperty("local.server.port", Integer.class, 0);
		this.webClient = createWebClient(this.port);
	}

	@Test
	void lifecycle() {
		AtomicReference<URI> location = new AtomicReference<>();

		StepVerifier.create(getEventStream().log()).expectSubscription().then(() -> {
			StepVerifier.create(listEmptyInstances()).expectNext(true).verifyComplete();
			StepVerifier.create(registerInstance()).consumeNextWith(location::set).verifyComplete();
		})
			.assertNext((event) -> assertThat(event.opt("type")).isEqualTo("REGISTERED"))
			.assertNext((event) -> assertThat(event.opt("type")).isEqualTo("STATUS_CHANGED"))
			.assertNext((event) -> assertThat(event.opt("type")).isEqualTo("ENDPOINTS_DETECTED"))
			.assertNext((event) -> assertThat(event.opt("type")).isEqualTo("INFO_CHANGED"))
			.then(() -> {
				StepVerifier.create(getInstance(location.get())).expectNext(true).verifyComplete();
				StepVerifier.create(listInstances()).expectNext(true).verifyComplete();
				deregisterInstance();
			})
			.assertNext((event) -> assertThat(event.opt("type")).isEqualTo("DEREGISTERED"))
			.then(() -> StepVerifier.create(listEmptyInstances()).expectNext(true).verifyComplete())
			.thenCancel()
			.verify(Duration.ofSeconds(60));
	}

	private Mono<URI> registerInstance() {
		// We register the instance by setting static values for the SimpleDiscoveryClient
		// and issuing a
		// InstanceRegisteredEvent that makes sure the instance gets registered.
		InstanceProperties instanceProps = new InstanceProperties();
		instanceProps.setServiceId("Test-Instance");
		instanceProps.setUri(URI.create("http://localhost:" + this.port));
		instanceProps.getMetadata().put("management.context-path", "/mgmt");
		this.simpleDiscovery.getInstances().put("Test-Instance", singletonList(instanceProps));

		this.instance.publishEvent(new InstanceRegisteredEvent<>(new Object(), null));

		// To get the location of the registered instances we fetch the instance with the
		// name.
		//@formatter:off
		return this.webClient.get()
			.uri("/instances?name=Test-Instance")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.returnResult(JSONObject.class).getResponseBody()
			.collectList()
			.map((applications) -> {
				assertThat(applications).hasSize(1);
				return URI
					.create("http://localhost:" + this.port + "/instances/" + applications.get(0).optString("id"));
			});
		//@formatter:on
	}

	private void deregisterInstance() {
		this.simpleDiscovery.getInstances().clear();
		this.instance.publishEvent(new InstanceRegisteredEvent<>(new Object(), null));
	}

	private Flux<JSONObject> getEventStream() {
		//@formatter:off
		return this.webClient.get().uri("/instances/events").accept(MediaType.TEXT_EVENT_STREAM)
						.exchange()
						.expectStatus().isOk()
						.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
						.returnResult(JSONObject.class).getResponseBody();
		//@formatter:on
	}

	private Mono<Boolean> getInstance(URI uri) {
		//@formatter:off
		return this.webClient.get().uri(uri).accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(String.class).getResponseBody().single()
				.map((body) -> {
					assertThat(body).contains("\"name\":\"Test-Instance\"");
					assertThat(body).contains("\"status\":\"UP\"");
					assertThat(body).contains("\"test\":\"foobar\"");
					return true;
				});
		//@formatter:on
	}

	private Mono<Boolean> listInstances() {
		//@formatter:off
		return this.webClient.get().uri("/instances").accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(String.class).getResponseBody().single()
				.map((body) -> {
					assertThat(body).contains("\"name\":\"Test-Instance\"");
					assertThat(body).contains("\"status\":\"UP\"");
					assertThat(body).contains("\"test\":\"foobar\"");
					return true;
				});
		//@formatter:on
	}

	private Mono<Boolean> listEmptyInstances() {
		//@formatter:off
		return this.webClient.get().uri("/instances").accept(MediaType.APPLICATION_JSON)
				.exchange()
				.returnResult(String.class).getResponseBody()
				.collectList()
				.map((list) -> {
					assertThat(list).hasSize(1);
					assertThat(list.get(0)).isEqualTo("[]");
					return true;
				});
		//@formatter:on
	}

	private WebTestClient createWebClient(int port) {
		JsonMapper mapper = JsonMapper.builder().addModule(new JsonOrgModule()).build();
		return WebTestClient.bindToServer()
			.baseUrl("http://localhost:" + port)
			.exchangeStrategies(ExchangeStrategies.builder().codecs((configurer) -> {
				configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(mapper));
				configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(mapper));
			}).build())
			.build();
	}

	@AfterEach
	void shutdown() {
		this.instance.close();
	}

	@EnableAdminServer
	@EnableAutoConfiguration
	@SpringBootConfiguration
	@EnableWebFluxSecurity
	public static class TestAdminApplication {

		@Bean
		SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
			return http.authorizeExchange((authorizeExchange) -> authorizeExchange.anyExchange().permitAll())
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.build();
		}

	}

}
