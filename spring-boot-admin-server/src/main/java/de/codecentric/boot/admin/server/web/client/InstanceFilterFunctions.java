/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.web.client;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Function;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.client.reactive.ClientHttpResponseDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static java.util.Collections.singletonList;

public final class InstanceFilterFunctions {
    private static final String ATTRIBUTE_INSTANCE = "instance";
    private static final String ATTRIBUTE_ENDPOINT = "endpointId";
    private static final MediaType ACTUATOR_V1_MEDIATYPE = MediaType.parseMediaType(ActuatorMediaType.V1_JSON);
    private static final MediaType ACTUATOR_V2_MEDIATYPE = MediaType.parseMediaType(ActuatorMediaType.V2_JSON);

    private InstanceFilterFunctions() {
    }

    public static ExchangeFilterFunction setInstance(Instance instance) {
        return setInstance(Mono.just(instance));
    }

    public static ExchangeFilterFunction setInstance(Mono<Instance> instance) {
        return (request, next) -> instance.map(
                i -> ClientRequest.from(request).attribute(ATTRIBUTE_INSTANCE, i).build())
                                          .switchIfEmpty(request.url().isAbsolute() ?
                                                  Mono.just(request) :
                                                  Mono.error(new InstanceWebClientException("Instance not found")))
                                          .flatMap(next::exchange);
    }

    public static ExchangeFilterFunction addHeaders(HttpHeadersProvider httpHeadersProvider) {
        return withInstance((instance, request, next) -> {
            ClientRequest newRequest = ClientRequest.from(request)
                                                    .headers(headers -> headers.addAll(
                                                            httpHeadersProvider.getHeaders(instance)))
                                                    .build();
            return next.exchange(newRequest);
        });
    }

    public static ExchangeFilterFunction withInstance(InstanceExchangeFilterFunction delegate) {
        return (request, next) -> {
            Optional<?> instance = request.attribute(ATTRIBUTE_INSTANCE);
            if (instance.isPresent() && instance.get() instanceof Instance) {
                return delegate.exchange((Instance) instance.get(), request, next);
            }
            return next.exchange(request);
        };
    }

    @FunctionalInterface
    public interface InstanceExchangeFilterFunction {
        Mono<ClientResponse> exchange(Instance instance, ClientRequest request, ExchangeFunction next);
    }

    public static ExchangeFilterFunction rewriteEndpointUrl() {
        return withInstance((instance, request, next) -> {
            if (request.url().isAbsolute()) {
                return next.exchange(request);
            }

            UriComponents oldUrl = UriComponentsBuilder.fromUri(request.url()).build();
            if (oldUrl.getPathSegments().isEmpty()) {
                return Mono.error(new InstanceWebClientException("No endpoint specified"));
            }

            String endpointId = oldUrl.getPathSegments().get(0);
            Optional<Endpoint> endpoint = instance.getEndpoints().get(endpointId);

            if (!endpoint.isPresent()) {
                return Mono.error(new InstanceWebClientException("Endpoint '" + endpointId + "' not found"));
            }

            URI newUrl = rewriteUrl(oldUrl, endpoint.get().getUrl());
            ClientRequest newRequest = ClientRequest.from(request)
                                                    .attribute(ATTRIBUTE_ENDPOINT, endpoint.get().getId())
                                                    .url(newUrl)
                                                    .build();
            return next.exchange(newRequest);
        });
    }

    private static URI rewriteUrl(UriComponents oldUrl, String targetUrl) {
        String[] newPathSegments = oldUrl.getPathSegments()
                                         .subList(1, oldUrl.getPathSegments().size())
                                         .toArray(new String[]{});
        return UriComponentsBuilder.fromUriString(targetUrl)
                                   .pathSegment(newPathSegments).query(oldUrl.getQuery()).build(true)
                                   .toUri();
    }

    public static ExchangeFilterFunction convertLegacyEndpoint(LegacyEndpointConverter converter) {
        return (ClientRequest request, ExchangeFunction next) -> {
            Mono<ClientResponse> clientResponse = next.exchange(request);
            if (request.attribute(ATTRIBUTE_ENDPOINT).map(converter::canConvert).orElse(false)) {
                return clientResponse.flatMap(response -> {
                    if (response.headers().contentType().map(ACTUATOR_V1_MEDIATYPE::isCompatibleWith).orElse(false)) {
                        return convertClientResponse(converter::convert, ACTUATOR_V2_MEDIATYPE).apply(response);
                    }
                    return Mono.just(response);
                });
            }
            return clientResponse;
        };
    }

    private static Function<ClientResponse, Mono<ClientResponse>> convertClientResponse(Function<Flux<DataBuffer>, Flux<DataBuffer>> bodConverter,
                                                                                        MediaType contentType) {
        return response -> Mono.just(new ConvertedBodyResponse(response, bodConverter, contentType));
    }

    private static class ConvertedBodyResponse implements ClientResponse {
        private final ClientResponse response;
        private final Function<Flux<DataBuffer>, Flux<DataBuffer>> converter;
        private final Headers headers;

        private ConvertedBodyResponse(ClientResponse response,
                                      Function<Flux<DataBuffer>, Flux<DataBuffer>> converter,
                                      MediaType contentType) {
            this.response = response;
            this.converter = converter;
            this.headers = new Headers() {
                @Override
                public OptionalLong contentLength() {
                    return response.headers().contentLength();
                }

                @Override
                public Optional<MediaType> contentType() {
                    return Optional.ofNullable(contentType);
                }

                @Override
                public List<String> header(String headerName) {
                    if (headerName.equals(HttpHeaders.CONTENT_TYPE)) {
                        return singletonList(contentType.toString());
                    }
                    return response.headers().header(headerName);
                }

                @Override
                public HttpHeaders asHttpHeaders() {
                    HttpHeaders newHeaders = new HttpHeaders();
                    newHeaders.putAll(response.headers().asHttpHeaders());
                    newHeaders.replace(HttpHeaders.CONTENT_TYPE, singletonList(contentType.toString()));
                    return HttpHeaders.readOnlyHttpHeaders(newHeaders);
                }
            };
        }

        @Override
        public HttpStatus statusCode() {
            return response.statusCode();
        }

        @Override
        public Headers headers() {
            return headers;
        }

        @Override
        public MultiValueMap<String, ResponseCookie> cookies() {
            return response.cookies();
        }

        @Override
        public <T> T body(BodyExtractor<T, ? super ClientHttpResponse> extractor) {
            return response.body((inputMessage, context) -> {
                ClientHttpResponse convertedMessage = new ClientHttpResponseDecorator(inputMessage) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return super.getBody().transform(ConvertedBodyResponse.this.converter);
                    }
                };
                return extractor.extract(convertedMessage, context);
            });
        }

        @Override
        public <T> Mono<T> bodyToMono(Class<? extends T> elementClass) {
            if (Void.class.isAssignableFrom(elementClass)) {
                return response.bodyToMono(elementClass);
            } else {
                return body(BodyExtractors.toMono(elementClass));
            }
        }

        @Override
        public <T> Mono<T> bodyToMono(ParameterizedTypeReference<T> typeReference) {
            if (Void.class.isAssignableFrom(typeReference.getType().getClass())) {
                return response.bodyToMono(typeReference);
            } else {
                return body(BodyExtractors.toMono(typeReference));
            }
        }

        @Override
        public <T> Flux<T> bodyToFlux(Class<? extends T> elementClass) {
            if (Void.class.isAssignableFrom(elementClass)) {
                return response.bodyToFlux(elementClass);
            } else {
                return body(BodyExtractors.toFlux(elementClass));
            }
        }

        @Override
        public <T> Flux<T> bodyToFlux(ParameterizedTypeReference<T> typeReference) {
            if (Void.class.isAssignableFrom(typeReference.getType().getClass())) {
                return response.bodyToFlux(typeReference);
            } else {
                return body(BodyExtractors.toFlux(typeReference));
            }
        }

        @Override
        public <T> Mono<ResponseEntity<T>> toEntity(Class<T> bodyType) {
            if (Void.class.isAssignableFrom(bodyType)) {
                return response.toEntity(bodyType);
            } else {
                return toEntityInternal(bodyToMono(bodyType));
            }
        }

        @Override
        public <T> Mono<ResponseEntity<T>> toEntity(ParameterizedTypeReference<T> typeReference) {
            if (Void.class.isAssignableFrom(typeReference.getType().getClass())) {
                return response.toEntity(typeReference);
            } else {
                return toEntityInternal(bodyToMono(typeReference));
            }
        }

        private <T> Mono<ResponseEntity<T>> toEntityInternal(Mono<T> bodyMono) {
            HttpHeaders headers = headers().asHttpHeaders();
            HttpStatus statusCode = statusCode();
            return bodyMono.map(body -> new ResponseEntity<>(body, headers, statusCode))
                           .switchIfEmpty(Mono.defer(() -> Mono.just(new ResponseEntity<>(headers, statusCode))));
        }

        @Override
        public <T> Mono<ResponseEntity<List<T>>> toEntityList(Class<T> responseType) {
            return toEntityListInternal(bodyToFlux(responseType));
        }

        @Override
        public <T> Mono<ResponseEntity<List<T>>> toEntityList(ParameterizedTypeReference<T> typeReference) {
            return toEntityListInternal(bodyToFlux(typeReference));
        }

        private <T> Mono<ResponseEntity<List<T>>> toEntityListInternal(Flux<T> bodyFlux) {
            HttpHeaders headers = headers().asHttpHeaders();
            HttpStatus statusCode = statusCode();
            return bodyFlux.collectList().map(body -> new ResponseEntity<>(body, headers, statusCode));
        }
    }
}
