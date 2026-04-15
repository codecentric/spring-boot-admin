/*
 * Copyright 2014-2026 the original author or authors.
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
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.netty.handler.timeout.ReadTimeoutException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.web.cache.ActuatorResponseCache;
import de.codecentric.boot.admin.server.web.cache.CacheEntry;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import de.codecentric.boot.admin.server.web.client.exception.ResolveEndpointException;

import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

/**
 * Forwards a request to a single instances endpoint and will respond with:
 * <ul>
 * <li>502 (Bad Gateway) when any error occurs during the request</li>
 * <li>503 (Service unavailable) when the instance is not found</li>
 * <li>504 (Gateway timeout) when the request exceeds the timeout</li>
 * </ul>
 *
 * <p>
 * When an optional {@link ActuatorResponseCache} is supplied (together with an
 * {@link HttpHeaderFilter}) the proxy transparently:
 * <ol>
 * <li>Returns a stored entry on a GET cache-hit without touching upstream.</li>
 * <li>Buffers and caches a 2xx GET response body when the response carries a known
 * {@code Content-Length} that is within the configured
 * {@link ActuatorResponseCache#getMaxPayloadSize() maxPayloadSize}. Responses with an
 * unknown {@code Content-Length} (e.g. chunked/streaming) are forwarded as-is and not
 * cached.</li>
 * <li>Invalidates the endpoint's cached entries after a successful mutating request
 * (POST/PUT/PATCH/DELETE).</li>
 * </ol>
 * Fan-out calls ({@link #forward(Flux, ForwardRequest)}) are never cached.
 *
 * @author Johannes Edmeier
 */
public class InstanceWebProxy {

	private static final Logger log = LoggerFactory.getLogger(InstanceWebProxy.class);

	private static final Instance NULL_INSTANCE = Instance.create(InstanceId.of("null"));

	private final InstanceWebClient instanceWebClient;

	private final ExchangeStrategies strategies = ExchangeStrategies.withDefaults();

	@Nullable private final ActuatorResponseCache cache;

	@Nullable private final HttpHeaderFilter headerFilter;

	private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();

	public InstanceWebProxy(InstanceWebClient instanceWebClient) {
		this(instanceWebClient, null, null);
	}

	public InstanceWebProxy(InstanceWebClient instanceWebClient, @Nullable ActuatorResponseCache cache,
			@Nullable HttpHeaderFilter headerFilter) {
		if (cache != null && headerFilter == null) {
			throw new IllegalArgumentException("headerFilter must be provided when cache is configured");
		}
		this.instanceWebClient = instanceWebClient;
		this.cache = cache;
		this.headerFilter = headerFilter;
	}

	/**
	 * Forwards a request to a single instance, applying cache semantics when a cache is
	 * configured.
	 * @param instanceMono reactive lookup of the target {@link Instance}
	 * @param forwardRequest the request to proxy
	 * @param responseHandler consumer of the (possibly cached) {@link ClientResponse}
	 * @param <V> response type produced by {@code responseHandler}
	 * @return result of {@code responseHandler}
	 */
	public <V> Mono<V> forward(Mono<Instance> instanceMono, ForwardRequest forwardRequest,
			Function<ClientResponse, Mono<V>> responseHandler) {
		return instanceMono.defaultIfEmpty(NULL_INSTANCE).flatMap((instance) -> {
			if (!instance.equals(NULL_INSTANCE)) {
				return (this.cache != null) ? forwardWithCache(instance, forwardRequest, responseHandler)
						: forwardUpstream(instance, forwardRequest, responseHandler);
			}
			return Mono.defer(() -> responseHandler
				.apply(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE, this.strategies).build()));
		});
	}

	/**
	 * Forwards a request to all instances of an application. Caching is never applied to
	 * fan-out calls.
	 * @param instances the instances to forward to
	 * @param forwardRequest the request to proxy
	 * @return a stream of per-instance responses
	 */
	public Flux<InstanceResponse> forward(Flux<Instance> instances, ForwardRequest forwardRequest) {
		return instances.flatMap((instance) -> forwardUpstream(instance, forwardRequest, (clientResponse) -> {
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

	// ---- cache-aware forwarding ----------------------------------------------

	private <V> Mono<V> forwardWithCache(Instance instance, ForwardRequest forwardRequest,
			Function<ClientResponse, Mono<V>> responseHandler) {
		InstanceId instanceId = instance.getId();
		String endpointPath = forwardRequest.getUri().getPath();
		String rawQuery = forwardRequest.getUri().getRawQuery();
		HttpMethod method = forwardRequest.getMethod();
		String endpointId = extractEndpointId(endpointPath);

		return Mono.fromCallable(() -> lookupCache(instanceId, endpointPath, rawQuery, method, endpointId))
			.subscribeOn(Schedulers.boundedElastic())
			.onErrorResume((ex) -> {
				log.warn("Cache lookup failed for instance '{}' endpoint '{}', falling back to upstream", instanceId,
						endpointId, ex);
				return Mono.just(Optional.empty());
			})
			.flatMap((hit) -> {
				if (hit.isPresent()) {
					return responseHandler.apply(buildClientResponse(hit.get()));
				}
				return forwardUpstream(instance, forwardRequest, (cr) -> interceptResponse(cr, instanceId, endpointPath,
						rawQuery, endpointId, method, responseHandler));
			});
	}

	private <V> Mono<V> interceptResponse(ClientResponse clientResponse, InstanceId instanceId, String endpointPath,
			@Nullable String rawQuery, String endpointId, HttpMethod method,
			Function<ClientResponse, Mono<V>> responseHandler) {
		HttpStatusCode statusCode = clientResponse.statusCode();

		if (isMutatingMethod(method) && statusCode.is2xxSuccessful()
				&& this.cache.shouldCache(HttpMethod.GET, endpointId)) {
			return Mono.fromRunnable(() -> this.cache.invalidateEndpointForInstance(instanceId, endpointId))
				.subscribeOn(Schedulers.boundedElastic())
				.onErrorResume((ex) -> {
					log.warn("Failed to invalidate cache for instance '{}' and endpoint '{}'", instanceId, endpointId,
							ex);
					return Mono.empty();
				})
				.then(Mono.defer(() -> responseHandler.apply(clientResponse)));
		}

		if (this.cache.shouldCache(method, endpointId) && statusCode.is2xxSuccessful()) {
			return bufferAndCache(clientResponse, instanceId, endpointPath, rawQuery, endpointId, statusCode,
					responseHandler);
		}

		return responseHandler.apply(clientResponse);
	}

	private <V> Mono<V> bufferAndCache(ClientResponse clientResponse, InstanceId instanceId, String endpointPath,
			@Nullable String rawQuery, String endpointId, HttpStatusCode statusCode,
			Function<ClientResponse, Mono<V>> responseHandler) {
		HttpHeaders originalHeaders = clientResponse.headers().asHttpHeaders();
		long contentLength = originalHeaders.getContentLength();
		if (contentLength < 0) {
			log.trace("Skipping cache for endpoint '{}': Content-Length is unknown", endpointId);
			return responseHandler.apply(clientResponse);
		}
		if (contentLength > this.cache.getMaxPayloadSize()) {
			log.trace("Skipping cache for endpoint '{}': Content-Length {} exceeds limit {}", endpointId, contentLength,
					this.cache.getMaxPayloadSize());
			return responseHandler.apply(clientResponse);
		}
		// Content-Length is known and within the cache limit, so buffering the body for
		// caching is bounded by maxPayloadSize.
		return DataBufferUtils.join(clientResponse.body(BodyExtractors.toDataBuffers()))
			.switchIfEmpty(Mono.fromSupplier(() -> this.bufferFactory.allocateBuffer(0)))
			.flatMap((joined) -> {
				byte[] bytes = new byte[joined.readableByteCount()];
				joined.read(bytes);
				DataBufferUtils.release(joined);
				// Content-Length was verified before joining, so bytes.length is bounded.
				HttpHeaders filteredHeaders = this.headerFilter.filterHeaders(originalHeaders);
				CacheEntry entry = new CacheEntry(statusCode.value(), filteredHeaders, bytes);
				ClientResponse rebuilt = rebuildClientResponse(statusCode, originalHeaders, bytes);
				return Mono.fromRunnable(() -> {
					this.cache.put(instanceId, endpointPath, rawQuery, entry);
					log.trace("Cached response for endpoint '{}' ({} bytes)", endpointId, bytes.length);
				}).subscribeOn(Schedulers.boundedElastic()).onErrorResume((ex) -> {
					log.warn("Failed to store cache entry for endpoint '{}'", endpointId, ex);
					return Mono.empty();
				}).then(Mono.defer(() -> responseHandler.apply(rebuilt)));
			});
	}

	// ---- cache helpers -------------------------------------------------------

	private Optional<CacheEntry> lookupCache(InstanceId instanceId, String endpointPath, @Nullable String rawQuery,
			HttpMethod method, String endpointId) {
		if (!this.cache.shouldCache(method, endpointId)) {
			return Optional.empty();
		}
		Optional<CacheEntry> entry = this.cache.get(instanceId, endpointPath, rawQuery);
		if (entry.isPresent()) {
			log.trace("Cache hit for instance {} endpoint '{}'", instanceId, endpointId);
		}
		else {
			log.trace("Cache miss for instance {} endpoint '{}'", instanceId, endpointId);
		}
		return entry;
	}

	/**
	 * Builds a {@link ClientResponse} from a stored {@link CacheEntry}. The wrapped
	 * {@link DataBuffer} is a read-only view of the cached byte array (no copy, no pool
	 * allocation); the response handler (e.g. {@code writeAndFlushWith}) is responsible
	 * for releasing it.
	 * @param entry the cached response entry
	 * @return a {@link ClientResponse} backed by the cached body bytes
	 */
	private ClientResponse buildClientResponse(CacheEntry entry) {
		DataBuffer body = this.bufferFactory.wrap(entry.getBodyRef());
		return ClientResponse.create(HttpStatusCode.valueOf(entry.getStatusCode()), this.strategies)
			.headers((h) -> h.addAll(entry.getHttpHeaders()))
			.body(Flux.just(body))
			.build();
	}

	/**
	 * Rebuilds a {@link ClientResponse} after body buffering so the response handler can
	 * consume the same bytes. The wrapped {@link DataBuffer} is backed directly by the
	 * buffered byte array (no pool allocation); the response handler is responsible for
	 * releasing it.
	 * @param statusCode the upstream response status
	 * @param originalHeaders the unfiltered upstream response headers
	 * @param bytes the buffered response body
	 * @return a {@link ClientResponse} backed by the buffered bytes
	 */
	private ClientResponse rebuildClientResponse(HttpStatusCode statusCode, HttpHeaders originalHeaders, byte[] bytes) {
		DataBuffer body = this.bufferFactory.wrap(bytes);
		return ClientResponse.create(statusCode, this.strategies)
			.headers((h) -> h.addAll(originalHeaders))
			.body(Flux.just(body))
			.build();
	}

	// ---- upstream forwarding -------------------------------------------------

	private <V> Mono<V> forwardUpstream(Instance instance, ForwardRequest forwardRequest,
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

	// ---- static helpers ------------------------------------------------------

	private static String extractEndpointId(String endpointPath) {
		int slash = endpointPath.indexOf('/');
		return (slash > 0) ? endpointPath.substring(0, slash) : endpointPath;
	}

	private static boolean isMutatingMethod(HttpMethod method) {
		return HttpMethod.POST.equals(method) || HttpMethod.PUT.equals(method) || HttpMethod.PATCH.equals(method)
				|| HttpMethod.DELETE.equals(method);
	}

	private boolean requiresBody(HttpMethod method) {
		return List.of(PUT, POST, PATCH).contains(method);
	}

	// ---- nested types --------------------------------------------------------

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
