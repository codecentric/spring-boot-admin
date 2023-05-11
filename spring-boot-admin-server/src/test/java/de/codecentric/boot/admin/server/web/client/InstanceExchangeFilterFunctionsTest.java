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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.actuate.endpoint.ApiVersion;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.cookies.PerInstanceCookieStore;
import de.codecentric.boot.admin.server.web.client.exception.ResolveEndpointException;

import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.ATTRIBUTE_ENDPOINT;
import static de.codecentric.boot.admin.server.web.client.InstanceWebClient.ATTRIBUTE_INSTANCE;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

class InstanceExchangeFilterFunctionsTest {

	private static final Instance INSTANCE = Instance.create(InstanceId.of("i"));

	@Nested
	class ConvertLegacyEndpoints {

		private final DefaultDataBufferFactory bufferFactory = new DefaultDataBufferFactory();

		private final DataBuffer original = this.bufferFactory.wrap("ORIGINAL".getBytes(StandardCharsets.UTF_8));

		private final DataBuffer converted = this.bufferFactory.wrap("CONVERTED".getBytes(StandardCharsets.UTF_8));

		private final InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.convertLegacyEndpoints(
				singletonList(new LegacyEndpointConverter("test", (from) -> Flux.just(this.converted)) {
				}));

		@Test
		void should_convert_v1_actuator() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.attribute(ATTRIBUTE_ENDPOINT, "test")
				.build();
			ClientResponse legacyResponse = ClientResponse.create(HttpStatus.OK)
				.header(CONTENT_TYPE, InstanceExchangeFilterFunctions.V1_ACTUATOR_JSON.toString())
				.header(CONTENT_LENGTH, Integer.toString(this.original.readableByteCount()))
				.body(Flux.just(this.original))
				.build();

			Mono<ClientResponse> response = this.filter.filter(INSTANCE, request, (r) -> Mono.just(legacyResponse));

			StepVerifier.create(response).assertNext((r) -> {
				assertThat(r.headers().contentType()).hasValue(new MediaType(ApiVersion.LATEST.getProducedMimeType()));
				assertThat(r.headers().contentLength()).isEmpty();
				StepVerifier.create(r.body(BodyExtractors.toDataBuffers())).expectNext(this.converted).verifyComplete();
			}).verifyComplete();
		}

		@Test
		void should_convert_json() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.attribute(ATTRIBUTE_ENDPOINT, "test")
				.build();
			ClientResponse legacyResponse = ClientResponse.create(HttpStatus.OK)
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(CONTENT_LENGTH, Integer.toString(this.original.readableByteCount()))
				.body(Flux.just(this.original))
				.build();

			Mono<ClientResponse> response = this.filter.filter(INSTANCE, request, (r) -> Mono.just(legacyResponse));

			StepVerifier.create(response).assertNext((r) -> {
				assertThat(r.headers().contentType()).hasValue(new MediaType(ApiVersion.LATEST.getProducedMimeType()));
				assertThat(r.headers().contentLength()).isEmpty();
				StepVerifier.create(r.body(BodyExtractors.toDataBuffers())).expectNext(this.converted).verifyComplete();
			}).verifyComplete();
		}

		@Test
		void should_not_convert_v2_actuator() {
			InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.convertLegacyEndpoints(
					singletonList(new LegacyEndpointConverter("test", (from) -> Flux.just(this.converted)) {
					}));

			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.attribute(ATTRIBUTE_ENDPOINT, "test")
				.build();
			ClientResponse response = ClientResponse.create(HttpStatus.OK)
				.header(CONTENT_TYPE, ApiVersion.LATEST.getProducedMimeType().toString())
				.header(CONTENT_LENGTH, Integer.toString(this.original.readableByteCount()))
				.body(Flux.just(this.original))
				.build();

			Mono<ClientResponse> convertedResponse = filter.filter(INSTANCE, request, (r) -> Mono.just(response));

			StepVerifier.create(convertedResponse).assertNext((r) -> {
				assertThat(r.headers().contentType()).hasValue(new MediaType(ApiVersion.LATEST.getProducedMimeType()));
				assertThat(r.headers().contentLength()).hasValue(this.original.readableByteCount());
				StepVerifier.create(r.body(BodyExtractors.toDataBuffers())).expectNext(this.original).verifyComplete();
			}).verifyComplete();
		}

		@Test
		void should_not_convert_unknown_endpoint() {
			InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.convertLegacyEndpoints(
					singletonList(new LegacyEndpointConverter("test", (from) -> Flux.just(this.converted)) {
					}));

			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test")).build();
			ClientResponse response = ClientResponse.create(HttpStatus.OK)
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(CONTENT_LENGTH, Integer.toString(this.original.readableByteCount()))
				.body(Flux.just(this.original))
				.build();

			Mono<ClientResponse> convertedResponse = filter.filter(INSTANCE, request, (r) -> Mono.just(response));

			StepVerifier.create(convertedResponse).assertNext((r) -> {
				assertThat(r.headers().contentType()).hasValue(MediaType.APPLICATION_JSON);
				assertThat(r.headers().contentLength()).hasValue(this.original.readableByteCount());
				StepVerifier.create(r.body(BodyExtractors.toDataBuffers())).expectNext(this.original).verifyComplete();
			}).verifyComplete();
		}

		@Test
		void should_not_convert_without_converter() {
			InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.convertLegacyEndpoints(
					singletonList(new LegacyEndpointConverter("test", (from) -> Flux.just(this.converted)) {
					}));

			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/unknown"))
				.attribute(ATTRIBUTE_ENDPOINT, "unknown")
				.build();
			ClientResponse response = ClientResponse.create(HttpStatus.OK)
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(CONTENT_LENGTH, Integer.toString(this.original.readableByteCount()))
				.body(Flux.just(this.original))
				.build();

			Mono<ClientResponse> convertedResponse = filter.filter(INSTANCE, request, (r) -> Mono.just(response));

			StepVerifier.create(convertedResponse).assertNext((r) -> {
				assertThat(r.headers().contentType()).hasValue(MediaType.APPLICATION_JSON);
				assertThat(r.headers().contentLength()).hasValue(this.original.readableByteCount());
				StepVerifier.create(r.body(BodyExtractors.toDataBuffers())).expectNext(this.original).verifyComplete();
			}).verifyComplete();
		}

	}

	@Nested
	class Retry {

		@Test
		void should_retry_using_default() {
			InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.retry(1, emptyMap());

			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test")).build();
			ClientResponse response = ClientResponse.create(HttpStatus.OK).build();

			AtomicLong invocationCount = new AtomicLong(0L);
			ExchangeFunction exchange = (r) -> Mono.fromSupplier(() -> {
				if (invocationCount.getAndIncrement() == 0) {
					throw new IllegalStateException("Test");
				}
				return response;
			});

			StepVerifier.create(filter.filter(INSTANCE, request, exchange)).expectNext(response).verifyComplete();
			assertThat(invocationCount.get()).isEqualTo(2);
		}

		@Test
		void should_retry_using_endpoint_value_default() {
			InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.retry(0, singletonMap("test", 1));

			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.attribute(ATTRIBUTE_ENDPOINT, "test")
				.build();
			ClientResponse response = ClientResponse.create(HttpStatus.OK).build();

			AtomicLong invocationCount = new AtomicLong(0L);
			ExchangeFunction exchange = (r) -> Mono.fromSupplier(() -> {
				if (invocationCount.getAndIncrement() == 0) {
					throw new IllegalStateException("Test");
				}
				return response;
			});

			StepVerifier.create(filter.filter(INSTANCE, request, exchange)).expectNext(response).verifyComplete();
			assertThat(invocationCount.get()).isEqualTo(2);
		}

		@Test
		void should_not_retry_for_put_post_patch_delete() {
			InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.retry(1, emptyMap());

			AtomicLong invocationCount = new AtomicLong(0L);
			ExchangeFunction exchange = (r) -> Mono.fromSupplier(() -> {
				invocationCount.incrementAndGet();
				throw new IllegalStateException("Test");
			});

			ClientRequest patchRequest = ClientRequest.create(HttpMethod.PATCH, URI.create("/test")).build();
			StepVerifier.create(filter.filter(INSTANCE, patchRequest, exchange))
				.verifyError(IllegalStateException.class);
			assertThat(invocationCount.get()).isEqualTo(1);

			invocationCount.set(0L);
			ClientRequest putRequest = ClientRequest.create(HttpMethod.PUT, URI.create("/test")).build();
			StepVerifier.create(filter.filter(INSTANCE, putRequest, exchange)).verifyError(IllegalStateException.class);
			assertThat(invocationCount.get()).isEqualTo(1);

			invocationCount.set(0L);
			ClientRequest postRequest = ClientRequest.create(HttpMethod.POST, URI.create("/test")).build();
			StepVerifier.create(filter.filter(INSTANCE, postRequest, exchange))
				.verifyError(IllegalStateException.class);
			assertThat(invocationCount.get()).isEqualTo(1);

			invocationCount.set(0L);
			ClientRequest deleteRequest = ClientRequest.create(HttpMethod.DELETE, URI.create("/test")).build();
			StepVerifier.create(filter.filter(INSTANCE, deleteRequest, exchange))
				.verifyError(IllegalStateException.class);
			assertThat(invocationCount.get()).isEqualTo(1);
		}

	}

	@Nested
	class AddHeaders {

		private InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.addHeaders((i) -> {
			HttpHeaders headers = new HttpHeaders();
			headers.add("X-INSTANCE-ID", i.getId().getValue());
			return headers;
		});

		@Test
		void should_add_headers_from_provider() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.attribute(ATTRIBUTE_INSTANCE, INSTANCE)
				.build();

			Mono<ClientResponse> response = this.filter.filter(INSTANCE, request, (req) -> {
				assertThat(req.headers().get("X-INSTANCE-ID")).containsExactly(INSTANCE.getId().getValue());
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

	}

	@Nested
	class AddHeadersReactive {

		@Test
		void should_add_headers_from_provider() {
			InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.addHeadersReactive((i) -> {
				HttpHeaders headers = new HttpHeaders();
				headers.add("X-INSTANCE-ID", i.getId().getValue());
				return Mono.just(headers);
			});

			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.attribute(ATTRIBUTE_INSTANCE, INSTANCE)
				.build();

			Mono<ClientResponse> response = filter.filter(INSTANCE, request, (req) -> {
				assertThat(req.headers().get("X-INSTANCE-ID")).containsExactly(INSTANCE.getId().getValue());
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

		@Test
		void should_pass_on_mono_empty() {
			InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions
				.addHeadersReactive((i) -> Mono.empty());

			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.attribute(ATTRIBUTE_INSTANCE, INSTANCE)
				.build();

			Mono<ClientResponse> response = filter.filter(INSTANCE, request, (req) -> {
				assertThat(req.headers().size()).isEqualTo(0);
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

	}

	@Nested
	class AddDefaultHeaders {

		private final InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.setDefaultAcceptHeader();

		@Test
		void should_add_default_accept_headers() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test")).build();

			Mono<ClientResponse> response = this.filter.filter(INSTANCE, request, (req) -> {
				assertThat(req.headers().getAccept()).containsExactly(
						new MediaType(ApiVersion.V3.getProducedMimeType()),
						new MediaType(ApiVersion.V2.getProducedMimeType()),
						InstanceExchangeFilterFunctions.V1_ACTUATOR_JSON, MediaType.APPLICATION_JSON);
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

		@Test
		void should_not_add_default_accept_headers() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
				.build();

			Mono<ClientResponse> response = this.filter.filter(INSTANCE, request, (req) -> {
				assertThat(req.headers().getAccept()).containsExactly(MediaType.APPLICATION_XML);
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

		@Test
		void should_add_default_logfile_accept_headers() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.attribute(ATTRIBUTE_ENDPOINT, Endpoint.LOGFILE)
				.build();

			Mono<ClientResponse> response = this.filter.filter(INSTANCE, request, (req) -> {
				assertThat(req.headers().getAccept()).containsExactly(MediaType.TEXT_PLAIN);
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

	}

	@Nested
	class RewriteEndpointUrl {

		private final InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.rewriteEndpointUrl();

		private final Registration registration = Registration.create("R", "http://test/actuator/health")
			.managementUrl("http://test/actuator")
			.build();

		private final Endpoints endpoints = Endpoints.single(Endpoint.ENV, "http://test/actuator/env");

		private final Instance instance = Instance.create(InstanceId.of("R"))
			.register(this.registration)
			.withEndpoints(this.endpoints);

		@Test
		void should_rewirte_url_and_add_endpoint_attribute() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("health/database"))
				.attribute(ATTRIBUTE_INSTANCE, this.instance)
				.build();

			Mono<ClientResponse> response = this.filter.filter(this.instance, request, (req) -> {
				assertThat(req.url()).isEqualTo(URI.create(this.registration.getHealthUrl() + "/database"));
				assertThat(req.attribute(ATTRIBUTE_ENDPOINT)).hasValue(Endpoint.HEALTH);
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

		@Test
		void should_not_rewrite_absolute_url() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://test/actuator/unknown"))
				.attribute(ATTRIBUTE_INSTANCE, this.instance)
				.build();

			Mono<ClientResponse> response = this.filter.filter(this.instance, request, (req) -> {
				assertThat(req.url()).isEqualTo(URI.create("http://test/actuator/unknown"));
				assertThat(req.attribute(ATTRIBUTE_ENDPOINT)).isEmpty();
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

		@Test
		void should_set_endpoint_attribute_for_management_url() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://test/actuator"))
				.attribute(ATTRIBUTE_INSTANCE, this.instance)
				.build();

			Mono<ClientResponse> response = this.filter.filter(this.instance, request, (req) -> {
				assertThat(req.attribute(ATTRIBUTE_ENDPOINT)).hasValue(Endpoint.ACTUATOR_INDEX);
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

		@Test
		void should_error_on_unspecified_endpoint() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create(""))
				.attribute(ATTRIBUTE_INSTANCE, this.instance)
				.build();

			Mono<ClientResponse> response = this.filter.filter(this.instance, request,
					(req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build()));

			StepVerifier.create(response)
				.verifyErrorSatisfies((e) -> assertThat(e).isInstanceOf(ResolveEndpointException.class)
					.hasMessage("No endpoint specified"));
		}

		@Test
		void should_error_on_unknown_endpoint() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("unknown"))
				.attribute(ATTRIBUTE_INSTANCE, this.instance)
				.build();

			Mono<ClientResponse> response = this.filter.filter(this.instance, request,
					(req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build()));

			StepVerifier.create(response)
				.verifyErrorSatisfies((e) -> assertThat(e).isInstanceOf(ResolveEndpointException.class)
					.hasMessage("Endpoint 'unknown' not found"));
		}

	}

	@Nested
	class Timeout {

		@Test
		void should_timeout_using_default() {
			InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.timeout(Duration.ofSeconds(1),
					emptyMap());

			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test")).build();

			Mono<ClientResponse> response = filter.filter(INSTANCE, request,
					(req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build())
						.delayElement(Duration.ofSeconds(10)));

			StepVerifier.create(response).expectError(TimeoutException.class).verify(Duration.ofSeconds(2));
		}

		@Test
		void should_timeout_using_endpoint_value_default() {
			InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.timeout(Duration.ofSeconds(10),
					singletonMap("test", Duration.ofSeconds(1)));

			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.attribute(ATTRIBUTE_ENDPOINT, "test")
				.build();

			Mono<ClientResponse> response = filter.filter(INSTANCE, request,
					(req) -> Mono.just(ClientResponse.create(HttpStatus.OK).build())
						.delayElement(Duration.ofSeconds(10)));

			StepVerifier.create(response).expectError(TimeoutException.class).verify(Duration.ofSeconds(2));
		}

	}

	@Nested
	class LogfileAcceptWorkaround {

		InstanceExchangeFilterFunction filter = InstanceExchangeFilterFunctions.logfileAcceptWorkaround();

		@Test
		void should_add_accept_all_to_headers_for_logfile() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.attribute(ATTRIBUTE_ENDPOINT, Endpoint.LOGFILE)
				.header(ACCEPT, TEXT_PLAIN_VALUE)
				.build();

			Mono<ClientResponse> response = this.filter.filter(INSTANCE, request, (req) -> {
				assertThat(req.headers().getAccept()).containsExactly(MediaType.TEXT_PLAIN, MediaType.ALL);
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

		@Test
		void should_not_add_accept_all_to_headers_for_non_logfile() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("/test"))
				.attribute(ATTRIBUTE_ENDPOINT, Endpoint.HTTPTRACE)
				.header(ACCEPT, APPLICATION_JSON_VALUE)
				.build();

			Mono<ClientResponse> response = this.filter.filter(INSTANCE, request, (req) -> {
				assertThat(req.headers().getAccept()).containsExactly(MediaType.APPLICATION_JSON);
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

	}

	@Nested
	public class CookieHandling {

		private PerInstanceCookieStore cookieStore;

		private InstanceExchangeFilterFunction filter;

		@BeforeEach
		public void setUp() {
			this.cookieStore = mock(PerInstanceCookieStore.class);
			this.filter = InstanceExchangeFilterFunctions.handleCookies(cookieStore);
		}

		@Test
		void should_store_retrieved_cookie() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://localhost/test")).build();

			Mono<ClientResponse> response = this.filter.filter(INSTANCE, request, (req) -> Mono
				.just(ClientResponse.create(HttpStatus.OK).header("Set-Cookie", "testCookie=testCookieValue").build()));

			StepVerifier.create(response).expectNextCount(1).verifyComplete();

			@SuppressWarnings("unchecked")
			ArgumentCaptor<MultiValueMap<String, String>> captor = ArgumentCaptor.forClass(MultiValueMap.class);
			verify(this.cookieStore).put(eq(INSTANCE.getId()), eq(request.url()), captor.capture());
			assertThat(captor.getValue()).containsEntry("Set-Cookie", singletonList("testCookie=testCookieValue"));
		}

		@Test
		void should_add_stored_cookie_to_request() {
			ClientRequest request = ClientRequest.create(HttpMethod.GET, URI.create("http://localhost/test")).build();

			MultiValueMap<String, String> cookieMap = new LinkedMultiValueMap<>();
			cookieMap.add("testCookie", "testCookieValue");
			when(this.cookieStore.get(eq(INSTANCE.getId()), eq(request.url()), any())).thenReturn(cookieMap);

			Mono<ClientResponse> response = this.filter.filter(INSTANCE, request, (req) -> {
				assertThat(req.cookies()).containsEntry("testCookie", singletonList("testCookieValue"));
				return Mono.just(ClientResponse.create(HttpStatus.OK).build());
			});

			StepVerifier.create(response).expectNextCount(1).verifyComplete();
		}

	}

}
