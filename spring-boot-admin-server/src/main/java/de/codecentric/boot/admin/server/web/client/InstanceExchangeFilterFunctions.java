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

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.ApiVersion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.web.client.cookies.PerInstanceCookieStore;
import de.codecentric.boot.admin.server.web.client.exception.ResolveEndpointException;
import de.codecentric.boot.admin.server.web.client.reactive.ReactiveHttpHeadersProvider;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public final class InstanceExchangeFilterFunctions {

	public static final String ATTRIBUTE_ENDPOINT = "endpointId";

	private static final Logger log = LoggerFactory.getLogger(InstanceExchangeFilterFunctions.class);

	private static final List<MediaType> DEFAULT_LOGFILE_ACCEPT_MEDIA_TYPES = singletonList(MediaType.TEXT_PLAIN);

	static MediaType V1_ACTUATOR_JSON = MediaType.valueOf("application/vnd.spring-boot.actuator.v1+json");

	private static final List<MediaType> DEFAULT_ACCEPT_MEDIA_TYPES = asList(
			new MediaType(ApiVersion.V3.getProducedMimeType()), new MediaType(ApiVersion.V2.getProducedMimeType()),
			V1_ACTUATOR_JSON, MediaType.APPLICATION_JSON);

	private InstanceExchangeFilterFunctions() {
	}

	public static InstanceExchangeFilterFunction addHeaders(HttpHeadersProvider httpHeadersProvider) {
		return (instance, request, next) -> {
			request = ClientRequest.from(request)
				.headers((headers) -> headers.addAll(httpHeadersProvider.getHeaders(instance)))
				.build();
			return next.exchange(request);
		};
	}

	public static InstanceExchangeFilterFunction addHeadersReactive(ReactiveHttpHeadersProvider httpHeadersProvider) {
		return (instance, request, next) -> httpHeadersProvider.getHeaders(instance).flatMap((httpHeaders) -> {
			ClientRequest requestWithAdditionalHeaders = ClientRequest.from(request)
				.headers((headers) -> headers.addAll(httpHeaders))
				.build();

			return next.exchange(requestWithAdditionalHeaders);
		}).switchIfEmpty(Mono.defer(() -> next.exchange(request)));
	}

	public static InstanceExchangeFilterFunction rewriteEndpointUrl() {
		return (instance, request, next) -> {
			if (request.url().isAbsolute()) {
				log.trace("Absolute URL '{}' for instance {} not rewritten", request.url(), instance.getId());
				if (request.url().toString().equals(instance.getRegistration().getManagementUrl())) {
					request = ClientRequest.from(request)
						.attribute(ATTRIBUTE_ENDPOINT, Endpoint.ACTUATOR_INDEX)
						.build();
				}
				return next.exchange(request);
			}

			UriComponents requestUrl = UriComponentsBuilder.fromUri(request.url()).build();
			if (requestUrl.getPathSegments().isEmpty()) {
				return Mono.error(new ResolveEndpointException("No endpoint specified"));
			}

			String endpointId = requestUrl.getPathSegments().get(0);
			Optional<Endpoint> endpoint = instance.getEndpoints().get(endpointId);

			if (!endpoint.isPresent()) {
				return Mono.error(new ResolveEndpointException("Endpoint '" + endpointId + "' not found"));
			}

			URI rewrittenUrl = rewriteUrl(requestUrl, endpoint.get().getUrl());
			log.trace("URL '{}' for Endpoint {} of instance {} rewritten to {}", requestUrl, endpoint.get().getId(),
					instance.getId(), rewrittenUrl);
			request = ClientRequest.from(request)
				.attribute(ATTRIBUTE_ENDPOINT, endpoint.get().getId())
				.url(rewrittenUrl)
				.build();
			return next.exchange(request);
		};
	}

	private static URI rewriteUrl(UriComponents oldUrl, String targetUrl) {
		String[] newPathSegments = oldUrl.getPathSegments()
			.subList(1, oldUrl.getPathSegments().size())
			.toArray(new String[] {});
		return UriComponentsBuilder.fromUriString(targetUrl)
			.pathSegment(newPathSegments)
			.query(oldUrl.getQuery())
			.build(true)
			.toUri();
	}

	public static InstanceExchangeFilterFunction convertLegacyEndpoints(List<LegacyEndpointConverter> converters) {
		return (instance, request, next) -> {
			Mono<ClientResponse> clientResponse = next.exchange(request);

			Optional<Object> endpoint = request.attribute(ATTRIBUTE_ENDPOINT);
			if (!endpoint.isPresent()) {
				return clientResponse;
			}

			for (LegacyEndpointConverter converter : converters) {
				if (converter.canConvert(endpoint.get())) {
					return clientResponse.map((response) -> {
						if (isLegacyResponse(response)) {
							return convertLegacyResponse(converter, response);
						}
						return response;
					});
				}
			}
			return clientResponse;
		};
	}

	private static Boolean isLegacyResponse(ClientResponse response) {
		return response.headers()
			.contentType()
			.filter((t) -> V1_ACTUATOR_JSON.isCompatibleWith(t) || MediaType.APPLICATION_JSON.isCompatibleWith(t))
			.isPresent();
	}

	private static ClientResponse convertLegacyResponse(LegacyEndpointConverter converter, ClientResponse response) {
		return response.mutate().headers((headers) -> {
			headers.replace(HttpHeaders.CONTENT_TYPE,
					singletonList(ApiVersion.LATEST.getProducedMimeType().toString()));
			headers.remove(HttpHeaders.CONTENT_LENGTH);
		}).body(converter::convert).build();
	}

	public static InstanceExchangeFilterFunction setDefaultAcceptHeader() {
		return (instance, request, next) -> {
			if (request.headers().getAccept().isEmpty()) {
				Boolean isRequestForLogfile = request.attribute(ATTRIBUTE_ENDPOINT)
					.map(Endpoint.LOGFILE::equals)
					.orElse(false);
				List<MediaType> acceptedHeaders = isRequestForLogfile ? DEFAULT_LOGFILE_ACCEPT_MEDIA_TYPES
						: DEFAULT_ACCEPT_MEDIA_TYPES;
				request = ClientRequest.from(request).headers((headers) -> headers.setAccept(acceptedHeaders)).build();
			}
			return next.exchange(request);
		};
	}

	public static InstanceExchangeFilterFunction retry(int defaultRetries, Map<String, Integer> retriesPerEndpoint) {
		return (instance, request, next) -> {
			int retries = 0;
			if (!request.method().equals(HttpMethod.DELETE) && !request.method().equals(HttpMethod.PATCH)
					&& !request.method().equals(HttpMethod.POST) && !request.method().equals(HttpMethod.PUT)) {
				retries = request.attribute(ATTRIBUTE_ENDPOINT).map(retriesPerEndpoint::get).orElse(defaultRetries);
			}
			return next.exchange(request).retry(retries);
		};
	}

	public static InstanceExchangeFilterFunction timeout(Duration defaultTimeout,
			Map<String, Duration> timeoutPerEndpoint) {
		return (instance, request, next) -> {
			Duration timeout = request.attribute(ATTRIBUTE_ENDPOINT)
				.map(timeoutPerEndpoint::get)
				.orElse(defaultTimeout);
			return next.exchange(request).timeout(timeout);
		};
	}

	// Accept header is broken on /logfile. We need to add "*/*" for old clients
	// see https://github.com/spring-projects/spring-boot/issues/16188
	public static InstanceExchangeFilterFunction logfileAcceptWorkaround() {
		return (instance, request, next) -> {
			if (request.attribute(ATTRIBUTE_ENDPOINT).map(Endpoint.LOGFILE::equals).orElse(false)) {
				List<MediaType> newAcceptHeaders = Stream
					.concat(request.headers().getAccept().stream(), Stream.of(MediaType.ALL))
					.collect(Collectors.toList());
				request = ClientRequest.from(request).headers((h) -> h.setAccept(newAcceptHeaders)).build();
			}
			return next.exchange(request);
		};
	}

	/**
	 * Creates the {@link InstanceExchangeFilterFunction} that could handle cookies during
	 * requests and responses to/from applications.
	 * @param store the cookie store to use
	 * @return the new filter function
	 */
	public static InstanceExchangeFilterFunction handleCookies(final PerInstanceCookieStore store) {
		return (instance, request, next) -> {
			// we need an absolute URL to be able to deal with cookies
			if (request.url().isAbsolute()) {
				return next.exchange(enrichRequestWithStoredCookies(instance.getId(), request, store))
					.map((response) -> storeCookiesFromResponse(instance.getId(), request, response, store));
			}

			return next.exchange(request);
		};
	}

	private static ClientRequest enrichRequestWithStoredCookies(final InstanceId instId, final ClientRequest request,
			final PerInstanceCookieStore store) {
		final MultiValueMap<String, String> storedCookies = store.get(instId, request.url(), request.headers());
		if (CollectionUtils.isEmpty(storedCookies)) {
			log.trace("No cookies found for request [url={}]", request.url());
			return request;
		}

		log.trace("Cookies found for request [url={}]", request.url());
		return ClientRequest.from(request).cookies((cm) -> cm.addAll(storedCookies)).build();
	}

	private static ClientResponse storeCookiesFromResponse(final InstanceId instId, final ClientRequest request,
			final ClientResponse response, final PerInstanceCookieStore store) {
		final HttpHeaders headers = response.headers().asHttpHeaders();
		log.trace("Searching for cookies in header values of response [url={},headerValues={}]", request.url(),
				headers);

		store.put(instId, request.url(), headers);

		return response;
	}

}
