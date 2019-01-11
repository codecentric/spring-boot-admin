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
import de.codecentric.boot.admin.server.web.client.exception.ResolveEndpointException;
import de.codecentric.boot.admin.server.web.client.exception.ResolveInstanceException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static de.codecentric.boot.admin.server.utils.MediaType.ACTUATOR_V1_MEDIATYPE;
import static de.codecentric.boot.admin.server.utils.MediaType.ACTUATOR_V2_MEDIATYPE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public final class InstanceExchangeFilterFunctions {
    private static final Logger log = LoggerFactory.getLogger(InstanceExchangeFilterFunctions.class);
    public static final String ATTRIBUTE_INSTANCE = "instance";
    public static final String ATTRIBUTE_ENDPOINT = "endpointId";
    private static final List<MediaType> DEFAULT_ACCEPT_MEDIATYPES = asList(MediaType.parseMediaType(ActuatorMediaType.V2_JSON),
        MediaType.parseMediaType(ActuatorMediaType.V1_JSON),
        MediaType.APPLICATION_JSON
    );
    private static final List<MediaType> DEFAULT_LOGFILE_ACCEPT_MEDIATYPES = asList(MediaType.TEXT_PLAIN,
        MediaType.ALL
    );

    private InstanceExchangeFilterFunctions() {
    }

    public static ExchangeFilterFunction setInstance(Instance instance) {
        return setInstance(Mono.just(instance));
    }

    public static ExchangeFilterFunction setInstance(Mono<Instance> instance) {
        return (request, next) -> instance.map(i -> ClientRequest.from(request)
                                                                 .attribute(ATTRIBUTE_INSTANCE, i)
                                                                 .build())
                                          .switchIfEmpty(request.url().isAbsolute() ? Mono.just(request) : Mono.error(
                                              new ResolveInstanceException("Could not resolve Instance")))
                                          .flatMap(next::exchange);
    }

    public static ExchangeFilterFunction addHeaders(HttpHeadersProvider httpHeadersProvider) {
        return toExchangeFilterFunction((instance, request, next) -> {
            ClientRequest newRequest = ClientRequest.from(request)
                                                    .headers(headers -> headers.addAll(httpHeadersProvider.getHeaders(
                                                        instance)))
                                                    .build();
            return next.exchange(newRequest);
        });
    }

    public static ExchangeFilterFunction toExchangeFilterFunction(InstanceExchangeFilterFunction delegate) {
        return (request, next) -> {
            Optional<?> instance = request.attribute(ATTRIBUTE_INSTANCE);
            if (instance.isPresent() && instance.get() instanceof Instance) {
                return delegate.exchange((Instance) instance.get(), request, next);
            }
            return next.exchange(request);
        };
    }

    public static ExchangeFilterFunction rewriteEndpointUrl() {
        return toExchangeFilterFunction((instance, request, next) -> {
            if (request.url().isAbsolute()) {
                log.trace("Absolute URL '{}' for instance {} not rewritten", request.url(), instance.getId());
                if (request.url().toString().equals(instance.getRegistration().getManagementUrl())) {
                    return next.exchange(ClientRequest.from(request)
                                                      .attribute(ATTRIBUTE_ENDPOINT, Endpoint.ACTUATOR_INDEX)
                                                      .build());
                }
                return next.exchange(request);
            }

            UriComponents oldUrl = UriComponentsBuilder.fromUri(request.url()).build();
            if (oldUrl.getPathSegments().isEmpty()) {
                return Mono.error(new ResolveEndpointException("No endpoint specified"));
            }

            String endpointId = oldUrl.getPathSegments().get(0);
            Optional<Endpoint> endpoint = instance.getEndpoints().get(endpointId);

            if (!endpoint.isPresent()) {
                return Mono.error(new ResolveEndpointException("Endpoint '" + endpointId + "' not found"));
            }

            URI newUrl = rewriteUrl(oldUrl, endpoint.get().getUrl());
            log.trace("URL '{}' for Endpoint {} of instance {} rewritten to {}",
                oldUrl,
                endpoint.get().getId(),
                instance.getId(),
                newUrl
            );
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
                                   .pathSegment(newPathSegments)
                                   .query(oldUrl.getQuery())
                                   .build(true)
                                   .toUri();
    }

    public static ExchangeFilterFunction convertLegacyEndpoint(LegacyEndpointConverter converter) {
        return (ClientRequest request, ExchangeFunction next) -> {
            Mono<ClientResponse> clientResponse = next.exchange(request);
            if (request.attribute(ATTRIBUTE_ENDPOINT).map(converter::canConvert).orElse(false)) {
                return clientResponse.flatMap(response -> {
                    if (response.headers()
                                .contentType()
                                .map(t -> ACTUATOR_V1_MEDIATYPE.isCompatibleWith(t) ||
                                          APPLICATION_JSON.isCompatibleWith(t))
                                .orElse(false)) {
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
        return response -> {
            ClientResponse convertedResponse = ClientResponse.from(response).headers(headers -> {
                headers.replace(HttpHeaders.CONTENT_TYPE, singletonList(contentType.toString()));
                headers.remove(HttpHeaders.CONTENT_LENGTH);
            }).body(response.bodyToFlux(DataBuffer.class).transform(bodConverter)).build();
            return Mono.just(convertedResponse);
        };
    }

    public static ExchangeFilterFunction setDefaultAcceptHeader() {
        return (request, next) -> {
            if (request.headers().getAccept().isEmpty()) {
                Boolean isRequestForLogfile = request.attribute(ATTRIBUTE_ENDPOINT)
                                                     .map(Endpoint.LOGFILE::equals)
                                                     .orElse(false);
                List<MediaType> acceptedHeaders = isRequestForLogfile ? DEFAULT_LOGFILE_ACCEPT_MEDIATYPES : DEFAULT_ACCEPT_MEDIATYPES;
                return next.exchange(ClientRequest.from(request)
                                                  .headers(headers -> headers.setAccept(acceptedHeaders))
                                                  .build());
            }
            return next.exchange(request);
        };
    }

    public static ExchangeFilterFunction retry(int defaultRetries, Map<String, Integer> retriesPerEndpoint) {
        return (request, next) -> {
            int retries = 0;
            if (!request.method().equals(HttpMethod.DELETE) &&
                !request.method().equals(HttpMethod.PATCH) &&
                !request.method().equals(HttpMethod.POST) &&
                !request.method().equals(HttpMethod.PUT)) {
                retries = request.attribute(ATTRIBUTE_ENDPOINT).map(retriesPerEndpoint::get).orElse(defaultRetries);
            }
            return next.exchange(request).retry(retries);
        };
    }
}
