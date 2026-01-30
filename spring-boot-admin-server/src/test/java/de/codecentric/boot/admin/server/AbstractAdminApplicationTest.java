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

package de.codecentric.boot.admin.server;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Getter;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;

import de.codecentric.boot.admin.server.domain.values.Registration;

import static org.assertj.core.api.Assertions.assertThat;

import tools.jackson.datatype.jsonorg.JsonOrgModule;

@Getter
public abstract class AbstractAdminApplicationTest {

	private WebTestClient webClient;

	private int port;

	public void setUp(int port) {
		this.port = port;
		this.webClient = createWebClient(port);
	}

	@Test
	public void lifecycle() {
		AtomicReference<URI> location = new AtomicReference<>();

		StepVerifier.create(getEventStream().log()).expectSubscription().then(() -> {
			StepVerifier.create(listEmptyInstances()).expectNext(true).verifyComplete();
			location.set(registerInstance());
		})
			.assertNext((event) -> assertThat(event.opt("type")).isEqualTo("REGISTERED"))
			.assertNext((event) -> assertThat(event.opt("type")).isEqualTo("STATUS_CHANGED"))
			.assertNext((event) -> assertThat(event.opt("type")).isEqualTo("ENDPOINTS_DETECTED"))
			.assertNext((event) -> assertThat(event.opt("type")).isEqualTo("INFO_CHANGED"))
			.then(() -> {
				StepVerifier.create(getInstance(location.get())).expectNext(true).verifyComplete();
				StepVerifier.create(listInstances()).expectNext(true).verifyComplete();
				StepVerifier.create(deregisterInstance(location.get())).expectNext(true).verifyComplete();
			})
			.assertNext((event) -> assertThat(event.opt("type")).isEqualTo("DEREGISTERED"))
			.then(() -> StepVerifier.create(listEmptyInstances()).expectNext(true).verifyComplete())
			.thenCancel()
			.verify(Duration.ofSeconds(120));
	}

	protected Flux<JSONObject> getEventStream() {
		//@formatter:off
		return this.webClient.get().uri("/instances/events")
							.accept(MediaType.TEXT_EVENT_STREAM)
							.exchange()
							.expectStatus().isOk()
							.expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
							.returnResult(JSONObject.class).getResponseBody();
		//@formatter:on
	}

	protected URI registerInstance() {
		//@formatter:off
		return this.webClient.post().uri("/instances")
							.contentType(MediaType.APPLICATION_JSON)
							.bodyValue(createRegistration())
							.exchange()
							.expectStatus().isCreated()
							.expectHeader().valueMatches("location", "^http://localhost:" + this.port + "/instances/[a-f0-9]+$")
							.returnResult(Void.class).getResponseHeaders().getLocation();
		//@formatter:on
	}

	protected Mono<Boolean> getInstance(URI uri) {
		//@formatter:off
		return this.webClient.get().uri(uri)
					.accept(MediaType.APPLICATION_JSON)
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

	protected Mono<Boolean> listInstances() {
		//@formatter:off
		return this.webClient.get().uri("/instances")
					.accept(MediaType.APPLICATION_JSON)
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

	protected Mono<Boolean> listEmptyInstances() {
		//@formatter:off
		return this.webClient.get().uri("/instances")
					.accept(MediaType.APPLICATION_JSON)
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

	protected Mono<Boolean> deregisterInstance(URI uri) {
		//@formatter:off
		return this.webClient.delete().uri(uri)
					.exchange()
					.returnResult(Void.class).getResponseBody()
					.then(Mono.just(true));
		//@formatter:on
	}

	private Registration createRegistration() {
		return Registration.builder()
			.name("Test-Instance")
			.healthUrl("http://localhost:" + this.port + "/mgmt/health")
			.managementUrl("http://localhost:" + this.port + "/mgmt")
			.serviceUrl("http://localhost:" + this.port)
			.build();
	}

	protected WebTestClient createWebClient(int port) {
		JsonMapper mapper = JsonMapper.builder().addModule(new JsonOrgModule()).build();
		return WebTestClient.bindToServer()
			.baseUrl("http://localhost:" + port)
			.exchangeStrategies(ExchangeStrategies.builder().codecs((configurer) -> {
				configurer.defaultCodecs().jacksonJsonDecoder(new JacksonJsonDecoder(mapper));
				configurer.defaultCodecs().jacksonJsonEncoder(new JacksonJsonEncoder(mapper));
			}).build())
			.build();
	}

}
