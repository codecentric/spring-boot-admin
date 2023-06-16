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

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;

/**
 * Represents a function that filters an{@linkplain ExchangeFunction exchange function}
 * issued on a registered instance.
 *
 * @author Johannes Edmeier
 */
@FunctionalInterface
public interface InstanceExchangeFilterFunction {

	Mono<ClientResponse> filter(Instance instance, ClientRequest request, ExchangeFunction next);

}
