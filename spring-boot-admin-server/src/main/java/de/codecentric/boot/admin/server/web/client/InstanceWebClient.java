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
import reactor.netty.ConnectionObserver;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

public class InstanceWebClient {
    private final WebClient webClient;

    public InstanceWebClient(HttpHeadersProvider httpHeadersProvider) {
        this(httpHeadersProvider, Duration.ofSeconds(2), Duration.ofSeconds(5));
    }

    public InstanceWebClient(HttpHeadersProvider httpHeadersProvider, Duration connectTimeout, Duration readTimeout) {
        this(httpHeadersProvider, connectTimeout, readTimeout, builder -> { });
    }

    public InstanceWebClient(HttpHeadersProvider httpHeadersProvider,
                             Duration connectTimeout,
                             Duration readTimeout,
                             WebClientCustomizer customizer) {
        this(createDefaultWebClient(connectTimeout, readTimeout, customizer), httpHeadersProvider);
    }

    public InstanceWebClient(WebClient webClient, HttpHeadersProvider httpHeadersProvider) {
        this.webClient = webClient.mutate().filters(filters -> {
            filters.add(InstanceExchangeFilterFunctions.addHeaders(httpHeadersProvider));
            filters.add(InstanceExchangeFilterFunctions.rewriteEndpointUrl());
            filters.add(InstanceExchangeFilterFunctions.setDefaultAcceptHeader());
            filters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.health()));
            filters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.info()));
            filters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.env()));
            filters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.httptrace()));
            filters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.threaddump()));
            filters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.liquibase()));
            filters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.flyway()));
        }).build();
    }

    public WebClient instance(Mono<Instance> instance) {
        return webClient.mutate()
                        .filters(filters -> filters.add(0, InstanceExchangeFilterFunctions.setInstance(instance)))
                        .build();
    }

    public WebClient instance(Instance instance) {
        return webClient.mutate()
                        .filters(filters -> filters.add(0, InstanceExchangeFilterFunctions.setInstance(instance)))
                        .build();
    }

    private static WebClient createDefaultWebClient(Duration connectTimeout,
                                                    Duration readTimeout,
                                                    WebClientCustomizer customizer) {
        //@formatter:off
        HttpClient httpClient = HttpClient.create().compress(true).tcpConfiguration(
            tcp -> tcp.bootstrap(
                bootstrap -> bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) connectTimeout.toMillis())
            ).observe(
                (connection, newState) -> {
                    if (ConnectionObserver.State.CONNECTED.equals(newState)) {
                        connection.addHandlerLast(new ReadTimeoutHandler(readTimeout.toMillis(), TimeUnit.MILLISECONDS));
                    }
                }
            )
        );
        //@formatter:on
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        WebClient.Builder builder = WebClient.builder().clientConnector(connector);
        customizer.customize(builder);
        return builder.build();
    }
}
