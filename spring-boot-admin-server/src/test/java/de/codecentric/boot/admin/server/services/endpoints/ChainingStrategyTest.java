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

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ChainingStrategyTest {

	@Test
	public void invariants() {
		assertThatThrownBy(() -> new ChainingStrategy((EndpointDetectionStrategy[]) null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'delegates' must not be null.");
		assertThatThrownBy(() -> new ChainingStrategy((EndpointDetectionStrategy) null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("'delegates' must not contain null.");
	}

	@Test
	public void should_chain_on_empty() {
		// given
		Instance instance = Instance.create(InstanceId.of("id"));
		ChainingStrategy strategy = new ChainingStrategy((a) -> Mono.empty(), (a) -> Mono.empty(),
				(a) -> Mono.just(Endpoints.single("id", "path")));
		// when/then
		StepVerifier.create(strategy.detectEndpoints(instance))
			.expectNext(Endpoints.single("id", "path"))
			.verifyComplete();
	}

	@Test
	public void should_return_empty_endpoints_when_all_empty() {
		// given
		Instance instance = Instance.create(InstanceId.of("id"));
		ChainingStrategy strategy = new ChainingStrategy((a) -> Mono.empty());
		// when/then
		StepVerifier.create(strategy.detectEndpoints(instance)).expectNext(Endpoints.empty()).verifyComplete();
	}

}
