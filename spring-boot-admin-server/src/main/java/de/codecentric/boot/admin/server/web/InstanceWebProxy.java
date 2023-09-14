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

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.netty.handler.timeout.ReadTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import de.codecentric.boot.admin.server.web.client.exception.ResolveEndpointException;

import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

/**
 * Forwards a request to a single instances endpoint and will respond with: - 502 (Bad
 * Gateway) when any error occurs during the request - 503 (Service unavailable) when the
 * instance is not found - 504 (Gateway timeout) when the request exceeds the timeout
 *
 * @author Johannes Edmeier
 */
public class InstanceWebProxy {

	private static final Logger log = LoggerFactory.getLogger(InstanceWebProxy.class);

	private static final Instance NULL_INSTANCE = Instance.create(InstanceId.of("null"));

	private final InstanceWebClient instanceWebClient;

	private final ExchangeStrategies strategies = ExchangeStrategies.withDefaults();

	public InstanceWebProxy(InstanceWebClient instanceWebClient) {
		this.instanceWebClient = instanceWebClient;
	}

	public <V> Mono<V> forward(Mono<Instance> instanceMono, ForwardRequest forwardRequest,
			Function<ClientResponse, Mono<V>> responseHandler) {
		return instanceMono.defaultIfEmpty(NULL_INSTANCE).flatMap((instance) -> {
			if (!instance.equals(NULL_INSTANCE)) {
				return this.forward(instance, forwardRequest, responseHandler);
			}
			else {
				return Mono.defer(() -> responseHandler
					.apply(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE, this.strategies).build()));
			}
		});
	}

	public Flux<InstanceResponse> forward(Flux<Instance> instances, ForwardRequest forwardRequest) {
		return instances.flatMap((instance) -> this.forward(instance, forwardRequest, (clientResponse) -> {
			InstanceResponse.Builder response = InstanceResponse.builder()
				.instanceId(instance.getId())
				.status(clientResponse.statusCode().value())
				.contentType(String.join(", ", clientResponse.headers().header(HttpHeaders.CONTENT_TYPE)));
			return clientResponse.bodyToMono(String.class)
				.map(response::body)
				.defaultIfEmpty(response)
				.map(InstanceResponse.Builder::build);
		}));
	}

	private <V> Mono<V> forward(Instance instance, ForwardRequest forwardRequest,
			Function<ClientResponse, Mono<V>> responseHandler) {
		log.trace("Proxy-Request for instance {} with URL '{}'", instance.getId(), forwardRequest.getUri());
		WebClient.RequestBodySpec bodySpec = this.instanceWebClient.instance(instance)
			.method(forwardRequest.getMethod())
			.uri(forwardRequest.getUri())
			.headers((h) -> h.addAll(forwardRequest.getHeaders()));

		WebClient.RequestHeadersSpec<?> headersSpec = bodySpec;
		if (requiresBody(forwardRequest.getMethod())) {
			headersSpec = bodySpec.body(forwardRequest.getBody());
		}

		return headersSpec.exchangeToMono(responseHandler).onErrorResume(ResolveEndpointException.class, (ex) -> {
			log.trace("No Endpoint found for Proxy-Request for instance {} with URL '{}'", instance.getId(),
					forwardRequest.getUri());
			return responseHandler.apply(ClientResponse.create(HttpStatus.NOT_FOUND, this.strategies).build());
		}).onErrorResume((ex) -> {
			Throwable cause = ex;
			if (ex instanceof WebClientRequestException) {
				cause = ex.getCause();
			}
			if (cause instanceof ReadTimeoutException || cause instanceof TimeoutException) {
				log.trace("Timeout for Proxy-Request for instance {} with URL '{}'", instance.getId(),
						forwardRequest.getUri());
				return responseHandler
					.apply(ClientResponse.create(HttpStatus.GATEWAY_TIMEOUT, this.strategies).build());
			}
			if (cause instanceof IOException) {
				log.trace("Proxy-Request for instance {} with URL '{}' errored", instance.getId(),
						forwardRequest.getUri(), cause);
				return responseHandler.apply(ClientResponse.create(HttpStatus.BAD_GATEWAY, this.strategies).build());
			}
			return Mono.error(ex);
		});
	}

	private boolean requiresBody(HttpMethod method) {
		return List.of(PUT, POST, PATCH).contains(method);
	}

	@lombok.Data
	@lombok.Builder(builderClassName = "Builder")
	public static class InstanceResponse {

		private final InstanceId instanceId;

		private final int status;

		@Nullable
		@JsonInclude(JsonInclude.Include.NON_EMPTY)
		private final String body;

		@Nullable
		@JsonInclude(JsonInclude.Include.NON_EMPTY)
		private final String contentType;

	}

	@lombok.Data
	@lombok.Builder(builderClassName = "Builder")
	public static class ForwardRequest {

		private final URI uri;

		private final HttpMethod method;

		private final HttpHeaders headers;

		private final BodyInserter<?, ? super ClientHttpRequest> body;

	}

}
