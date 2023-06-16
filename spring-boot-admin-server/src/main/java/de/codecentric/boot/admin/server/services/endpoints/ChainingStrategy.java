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

import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Endpoints;

public class ChainingStrategy implements EndpointDetectionStrategy {

	private final EndpointDetectionStrategy[] delegates;

	public ChainingStrategy(EndpointDetectionStrategy... delegates) {
		Assert.notNull(delegates, "'delegates' must not be null.");
		Assert.noNullElements(delegates, "'delegates' must not contain null.");
		this.delegates = delegates;
	}

	@Override
	public Mono<Endpoints> detectEndpoints(Instance instance) {
		Mono<Endpoints> result = Mono.empty();
		for (EndpointDetectionStrategy delegate : delegates) {
			result = result.switchIfEmpty(delegate.detectEndpoints(instance));
		}
		return result.switchIfEmpty(Mono.just(Endpoints.empty()));
	}

}
