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
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

public class InstanceWebClient {
    private final WebClient webClient;

    public InstanceWebClient(HttpHeadersProvider httpHeadersProvider) {
        this(httpHeadersProvider, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    public InstanceWebClient(HttpHeadersProvider httpHeadersProvider, Duration connectTimeout, Duration readTimeout) {
        this(createDefaultWebClient(connectTimeout, readTimeout), httpHeadersProvider);
    }

    public InstanceWebClient(WebClient webClient, HttpHeadersProvider httpHeadersProvider) {
        this.webClient = webClient.mutate().filters(filters -> {
            filters.add(InstanceFilterFunctions.addHeaders(httpHeadersProvider));
            filters.add(InstanceFilterFunctions.rewriteEndpointUrl());
            filters.add(InstanceFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.health()));
            filters.add(InstanceFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.env()));
            filters.add(InstanceFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.httptrace()));
            filters.add(InstanceFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.threaddump()));
            filters.add(InstanceFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.liquibase()));
            filters.add(InstanceFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.flyway()));
        }).build();
    }

    public WebClient instance(Mono<Instance> instance) {
        return webClient.mutate()//
                        .filters(filters -> filters.add(0, InstanceFilterFunctions.setInstance(instance)))//
                        .build();
    }

    public WebClient instance(Instance instance) {
        return webClient.mutate()//
                        .filters(filters -> filters.add(0, InstanceFilterFunctions.setInstance(instance)))//
                        .build();
    }

    private static WebClient createDefaultWebClient(Duration connectTimeout, Duration readTimeout) {
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(
            options -> options.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis())//
                              .compression(true)//
                              .afterNettyContextInit(ctx -> {
                                  ctx.addHandlerLast(
                                      new ReadTimeoutHandler(readTimeout.toMillis(), TimeUnit.MILLISECONDS));
                              }));
        return WebClient.builder()
                        .clientConnector(connector)
                        .defaultHeader(HttpHeaders.ACCEPT, ActuatorMediaType.V2_JSON, ActuatorMediaType.V1_JSON,
                            MediaType.APPLICATION_JSON_VALUE)
                        .build();
    }
}
