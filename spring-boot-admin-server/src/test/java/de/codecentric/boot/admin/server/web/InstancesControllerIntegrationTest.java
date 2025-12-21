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

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.AdminReactiveApplicationTest;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class InstancesControllerIntegrationTest {

	private int localPort;

	private WebTestClient client;

	private String registerAsTest;

	private String registerAsTwice;

	private ConfigurableApplicationContext instance;

	private final ParameterizedTypeReference<Map<String, Object>> responseType = new ParameterizedTypeReference<>() {
	};

	@AfterAll
	static void tearDown() {
		StepVerifier.resetDefaultTimeout();
	}

	@BeforeAll
	static void beforeAll() {
		StepVerifier.setDefaultTimeout(Duration.ofSeconds(600));
	}

	@BeforeEach
	void setUp() {
		instance = new SpringApplicationBuilder().sources(AdminReactiveApplicationTest.TestAdminApplication.class)
			.web(WebApplicationType.REACTIVE)
			.run("--server.port=0", "--eureka.client.enabled=false");

		localPort = instance.getEnvironment().getProperty("local.server.port", Integer.class, 0);

		this.client = WebTestClient.bindToServer().baseUrl("http://localhost:" + localPort).build();
		this.registerAsTest = "{ \"name\": \"test\", \"healthUrl\": \"http://localhost:" + localPort
				+ "/application/health\" }";
		this.registerAsTwice = "{ \"name\": \"twice\", \"healthUrl\": \"http://localhost:" + localPort
				+ "/application/health\" }";
	}

	@AfterEach
	void shutdown() {
		instance.close();
	}

	@Test
	void should_return_not_found_when_get_unknown_instance() {
		this.client.get().uri("/instances/unknown").exchange().expectStatus().isNotFound();
	}

	@Test
	void should_return_empty_list() {
		this.client.get()
			.uri("/instances?name=unknown")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBody(java.util.List.class)
			.isEqualTo(emptyList());
	}

	@Test
	void should_return_not_found_when_deleting_unknown_instance() {
		this.client.delete().uri("/instances/unknown").exchange().expectStatus().isNotFound();
	}

	@Test
	void should_return_registered_instances() {
		AtomicReference<String> id = new AtomicReference<>();

		StepVerifier.create(this.getEventStream().log())
			.expectSubscription()
			.then(() -> StepVerifier.create(register()).consumeNextWith(id::set).verifyComplete())
			.assertNext((body) -> {
				assertThat(body).containsEntry("version", 0).containsEntry("type", "REGISTERED");
				// The id might not be set yet if event arrives before registration
				// completes
				if (id.get() == null) {
					id.set((String) body.get("instance"));
				}
				assertThat(body).containsEntry("instance", id.get());
			})
			.then(() -> {
				StepVerifier.create(assertInstances(id.get())).expectNext(true).verifyComplete();
				StepVerifier.create(assertInstancesByName("test", id.get())).expectNext(true).verifyComplete();
				StepVerifier.create(assertInstanceById(id.get())).expectNext(true).verifyComplete();
			})
			.assertNext((body) -> assertThat(body).containsEntry("instance", id.get())
				.containsEntry("version", 1)
				.containsEntry("type", "STATUS_CHANGED"))
			.then(() -> StepVerifier.create(registerSecondTime(id.get())).expectNext(true).verifyComplete())
			.assertNext((body) -> assertThat(body).containsEntry("instance", id.get())
				.containsEntry("version", 2)
				.containsEntry("type", "REGISTRATION_UPDATED"))
			.then(() -> StepVerifier.create(deregister(id.get())).expectNext(true).verifyComplete())
			.assertNext((body) -> assertThat(body).containsEntry("instance", id.get())
				.containsEntry("version", 3)
				.containsEntry("type", "DEREGISTERED"))
			.then(() -> {
				StepVerifier.create(assertInstanceNotFound(id.get())).expectNext(true).verifyComplete();
				StepVerifier.create(assertEvents(id.get())).expectNext(true).verifyComplete();
			})
			.thenCancel()
			.verify();
	}

	private Mono<Boolean> assertEvents(String id) {
		//@formatter:off
		return this.client.get()
			.uri("/instances/events")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.returnResult(String.class).getResponseBody().single()
			.map((responseBody) -> {
				DocumentContext json = JsonPath.parse(responseBody);
				assertThat(json.read("$[0].instance", String.class)).isEqualTo(id);
				assertThat(json.read("$[0].version", Long.class)).isZero();
				assertThat(json.read("$[0].type", String.class)).isEqualTo("REGISTERED");
				assertThat(json.read("$[1].instance", String.class)).isEqualTo(id);
				assertThat(json.read("$[1].version", Long.class)).isEqualTo(1L);
				assertThat(json.read("$[1].type", String.class)).isEqualTo("STATUS_CHANGED");
				assertThat(json.read("$[2].instance", String.class)).isEqualTo(id);
				assertThat(json.read("$[2].version", Long.class)).isEqualTo(2L);
				assertThat(json.read("$[2].type", String.class)).isEqualTo("REGISTRATION_UPDATED");
				assertThat(json.read("$[3].instance", String.class)).isEqualTo(id);
				assertThat(json.read("$[3].version", Long.class)).isEqualTo(3L);
				assertThat(json.read("$[3].type", String.class)).isEqualTo("DEREGISTERED");
				return true;
			});
		//@formatter:on
	}

	private Mono<Boolean> assertInstanceNotFound(String id) {
		//@formatter:off
		return this.client.get()
			.uri(getLocation(id))
			.exchange()
			.expectStatus().isNotFound()
			.returnResult(Void.class).getResponseBody()
			.then(Mono.just(true));
		//@formatter:on
	}

	private Mono<Boolean> deregister(String id) {
		//@formatter:off
		return this.client.delete()
			.uri(getLocation(id))
			.exchange()
			.expectStatus().isNoContent()
			.returnResult(Void.class).getResponseBody()
			.then(Mono.just(true));
		//@formatter:on
	}

	private Mono<Boolean> assertInstanceById(String id) {
		//@formatter:off
		return this.client.get()
			.uri(getLocation(id))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.returnResult(String.class).getResponseBody().single()
			.map((body) -> {
				DocumentContext json = JsonPath.parse(body);
				assertThat(json.read("$.id", String.class)).isEqualTo(id);
				return true;
			});
		//@formatter:on
	}

	private Mono<Boolean> assertInstancesByName(String name, String id) {
		//@formatter:off
		return this.client.get()
			.uri("/instances?name=" + name)
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.returnResult(String.class).getResponseBody().single()
			.map((body) -> {
				DocumentContext json = JsonPath.parse(body);
				assertThat(json.read("$[0].id", String.class)).isEqualTo(id);
				return true;
			});
		//@formatter:on
	}

	private Mono<Boolean> assertInstances(String id) {
		//@formatter:off
		return this.client.get()
			.uri("/instances")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.returnResult(String.class).getResponseBody().single()
			.map((body) -> {
				DocumentContext json = JsonPath.parse(body);
				assertThat(json.read("$[0].id", String.class)).isEqualTo(id);
				return true;
			});
		//@formatter:on
	}

	private Mono<Boolean> registerSecondTime(String id) {
		//@formatter:off
		return this.client.post()
			.uri("/instances")
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(registerAsTwice)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().valueEquals("location", getLocation(id))
			.returnResult(responseType).getResponseBody().single()
			.map((body) -> {
				assertThat(body).isEqualTo(singletonMap("id", id));
				return true;
			});
		//@formatter:on
	}

	private Mono<String> register() {
		//@formatter:off
		return this.client.post()
			.uri("/instances")
			.accept(MediaType.APPLICATION_JSON)
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(registerAsTest)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().valueMatches("location", "http://localhost:" + localPort + "/instances/[0-9a-f]+")
			.returnResult(responseType).getResponseBody().single()
			.map((body) -> {
				assertThat(body).containsKeys("id");
				return body.get("id").toString();
			});
		//@formatter:on
	}

	private String getLocation(String id) {
		return "http://localhost:" + localPort + "/instances/" + id;

	}

	private Flux<Map<String, Object>> getEventStream() {
		//@formatter:off
		return this.client.get().uri("/instances/events").accept(MediaType.TEXT_EVENT_STREAM)
						.exchange()
						.expectStatus().isOk()
						.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
						.returnResult(responseType).getResponseBody();
		//@formatter:on
	}

}
