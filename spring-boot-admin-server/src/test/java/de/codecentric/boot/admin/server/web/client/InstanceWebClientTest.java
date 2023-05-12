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

package de.codecentric.boot.admin.server.web.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.web.client.exception.ResolveInstanceException;

import static de.codecentric.boot.admin.server.web.client.InstanceWebClient.ATTRIBUTE_INSTANCE;
import static org.assertj.core.api.Assertions.assertThat;

public class InstanceWebClientTest {

	@Test
	public void should_error_without_instance() {
		Mono<Void> response = InstanceWebClient.builder()
			.build()
			.instance(Mono.empty())
			.get()
			.uri("health")
			.exchangeToMono((r) -> Mono.empty());
		StepVerifier.create(response)
			.verifyErrorSatisfies((ex) -> assertThat(ex).isInstanceOf(ResolveInstanceException.class)
				.hasMessageContaining("Could not resolve Instance"));
	}

	@Test
	public void should_add_instance_attribute() {
		Instance instance = Instance.create(InstanceId.of("i"));

		Mono<ClientResponse> response = InstanceWebClient.builder().filter((inst, req, next) -> {
			assertThat(req.attribute(ATTRIBUTE_INSTANCE)).hasValue(instance);
			assertThat(inst).isEqualTo(instance);
			return Mono.just(ClientResponse.create(HttpStatus.OK).build());
		}).build().instance(Mono.just(instance)).get().uri("http://test/health").exchangeToMono(Mono::just);

		StepVerifier.create(response)
			.assertNext((r) -> assertThat(r.statusCode()).isEqualTo(HttpStatus.OK))
			.verifyComplete();
	}

}
