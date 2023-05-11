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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.web.client.exception.ResolveInstanceException;

public class InstanceWebClient {

	public static final String ATTRIBUTE_INSTANCE = "instance";

	private final WebClient webClient;

	protected InstanceWebClient(WebClient webClient) {
		this.webClient = webClient;
	}

	public WebClient instance(Mono<Instance> instance) {
		return this.webClient.mutate().filters((filters) -> filters.add(0, setInstance(instance))).build();
	}

	public WebClient instance(Instance instance) {
		return this.instance(Mono.justOrEmpty(instance));
	}

	public static InstanceWebClient.Builder builder() {
		return new InstanceWebClient.Builder();
	}

	public static InstanceWebClient.Builder builder(WebClient.Builder webClient) {
		return new InstanceWebClient.Builder(webClient);
	}

	private static ExchangeFilterFunction setInstance(Mono<Instance> instance) {
		return (request, next) -> instance
			.map((i) -> ClientRequest.from(request).attribute(ATTRIBUTE_INSTANCE, i).build())
			.switchIfEmpty(Mono.error(() -> new ResolveInstanceException("Could not resolve Instance")))
			.flatMap(next::exchange);
	}

	private static ExchangeFilterFunction toExchangeFilterFunction(InstanceExchangeFilterFunction filter) {
		return (request, next) -> request.attribute(ATTRIBUTE_INSTANCE)
			.filter(Instance.class::isInstance)
			.map(Instance.class::cast)
			.map((instance) -> filter.filter(instance, request, next))
			.orElseGet(() -> next.exchange(request));
	}

	public static class Builder {

		private List<InstanceExchangeFilterFunction> filters = new ArrayList<>();

		private WebClient.Builder webClientBuilder;

		public Builder() {
			this(WebClient.builder());
		}

		public Builder(WebClient.Builder webClientBuilder) {
			this.webClientBuilder = webClientBuilder;
		}

		protected Builder(Builder other) {
			this.filters = new ArrayList<>(other.filters);
			this.webClientBuilder = other.webClientBuilder.clone();
		}

		public Builder filter(InstanceExchangeFilterFunction filter) {
			this.filters.add(filter);
			return this;
		}

		public Builder filters(Consumer<List<InstanceExchangeFilterFunction>> filtersConsumer) {
			filtersConsumer.accept(this.filters);
			return this;
		}

		public Builder webClient(WebClient.Builder builder) {
			this.webClientBuilder = builder;
			return this;
		}

		public Builder clone() {
			return new Builder(this);
		}

		public InstanceWebClient build() {
			this.filters.stream()
				.map(InstanceWebClient::toExchangeFilterFunction)
				.forEach(this.webClientBuilder::filter);
			return new InstanceWebClient(this.webClientBuilder.build());
		}

	}

}
