/*
 * Copyright 2014-2019 the original author or authors.
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

import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.web.client.exception.ResolveEndpointException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static de.codecentric.boot.admin.server.utils.MediaType.ACTUATOR_V1_MEDIATYPE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public final class InstanceExchangeFilterFunctions {
    private static final Logger log = LoggerFactory.getLogger(InstanceExchangeFilterFunctions.class);
    public static final String ATTRIBUTE_ENDPOINT = "endpointId";
    private static final List<MediaType> DEFAULT_ACCEPT_MEDIATYPES = asList(MediaType.parseMediaType(ActuatorMediaType.V2_JSON),
        MediaType.parseMediaType(ActuatorMediaType.V1_JSON),
        MediaType.APPLICATION_JSON
    );
    private static final List<MediaType> DEFAULT_LOGFILE_ACCEPT_MEDIATYPES = singletonList(MediaType.TEXT_PLAIN);

    private InstanceExchangeFilterFunctions() {
    }

    public static InstanceExchangeFilterFunction addHeaders(HttpHeadersProvider httpHeadersProvider) {
        return (instance, request, next) -> {
            request = ClientRequest.from(request)
                                   .headers(headers -> headers.addAll(httpHeadersProvider.getHeaders(instance)))
                                   .build();
            return next.exchange(request);
        };
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
            log.trace(
                "URL '{}' for Endpoint {} of instance {} rewritten to {}",
                requestUrl,
                endpoint.get().getId(),
                instance.getId(),
                rewrittenUrl
            );
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
                                         .toArray(new String[]{});
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
                    return clientResponse.map(response -> {
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
                       .map(t -> ACTUATOR_V1_MEDIATYPE.isCompatibleWith(t) || APPLICATION_JSON.isCompatibleWith(t))
                       .orElse(false);
    }

    private static ClientResponse convertLegacyResponse(LegacyEndpointConverter converter, ClientResponse response) {
        return ClientResponse.from(response).headers(headers -> {
            headers.replace(HttpHeaders.CONTENT_TYPE, singletonList(ActuatorMediaType.V2_JSON));
            headers.remove(HttpHeaders.CONTENT_LENGTH);
        }).body(response.bodyToFlux(DataBuffer.class).transform(converter::convert)).build();
    }

    public static InstanceExchangeFilterFunction setDefaultAcceptHeader() {
        return (instance, request, next) -> {
            if (request.headers().getAccept().isEmpty()) {
                Boolean isRequestForLogfile = request.attribute(ATTRIBUTE_ENDPOINT)
                                                     .map(Endpoint.LOGFILE::equals)
                                                     .orElse(false);
                List<MediaType> acceptedHeaders = isRequestForLogfile ? DEFAULT_LOGFILE_ACCEPT_MEDIATYPES : DEFAULT_ACCEPT_MEDIATYPES;
                request = ClientRequest.from(request).headers(headers -> headers.setAccept(acceptedHeaders)).build();
            }
            return next.exchange(request);
        };
    }

    public static InstanceExchangeFilterFunction retry(int defaultRetries, Map<String, Integer> retriesPerEndpoint) {
        return (instance, request, next) -> {
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
                List<MediaType> newAcceptHeaders = Stream.concat(request.headers().getAccept().stream(),
                    Stream.of(MediaType.ALL)
                ).collect(Collectors.toList());
                request = ClientRequest.from(request).headers(h -> h.setAccept(newAcceptHeaders)).build();
            }
            return next.exchange(request);
        };
    }
}
