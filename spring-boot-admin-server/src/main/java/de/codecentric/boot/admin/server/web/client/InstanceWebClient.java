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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

public class InstanceWebClient {
    private final WebClient webClient;

    protected InstanceWebClient(WebClient webClient) {
        this.webClient = webClient;
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

    public static InstanceWebClient.Builder builder() {
        return new InstanceWebClient.Builder();
    }

    public static class Builder {
        private Duration connectTimeout = Duration.ofSeconds(2);
        private Duration readTimeout = Duration.ofSeconds(5);
        private WebClientCustomizer webClientCustomizer = builder -> { };
        private int defaultRetries = 0;
        private Map<String, Integer> retriesPerEndpoint = Collections.emptyMap();
        private HttpHeadersProvider httpHeadersProvider = instance -> HttpHeaders.EMPTY;
        private final List<InstanceExchangeFilterFunction> filters = new ArrayList<>();
        @Nullable
        private WebClient webClient;

        public Builder webClientCustomizer(WebClientCustomizer webClientCustomizer) {
            this.webClientCustomizer = webClientCustomizer;
            return this;
        }

        public Builder webClient(WebClient webClient) {
            this.webClient = webClient;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder defaultRetries(int defaultRetry) {
            this.defaultRetries = defaultRetry;
            return this;
        }

        public Builder retries(Map<String, Integer> retryPerEndpoint) {
            this.retriesPerEndpoint = retryPerEndpoint;
            return this;
        }

        public Builder httpHeadersProvider(HttpHeadersProvider httpHeadersProvider) {
            this.httpHeadersProvider = httpHeadersProvider;
            return this;
        }

        public Builder filter(InstanceExchangeFilterFunction filter) {
            this.filters.add(filter);
            return this;
        }

        public Builder filters(Consumer<List<InstanceExchangeFilterFunction>> filtersConsumer) {
            filtersConsumer.accept(this.filters);
            return this;
        }

        public InstanceWebClient build() {
            WebClient.Builder webClientBuilder;
            if (this.webClient == null) {
                webClientBuilder = createDefaultWebClient(this.connectTimeout, this.readTimeout);
            } else {
                webClientBuilder = this.webClient.mutate();
            }

            webClientBuilder.filters(webClientFilters -> {
                webClientFilters.add(InstanceExchangeFilterFunctions.addHeaders(this.httpHeadersProvider));
                webClientFilters.add(InstanceExchangeFilterFunctions.rewriteEndpointUrl());
                webClientFilters.add(InstanceExchangeFilterFunctions.setDefaultAcceptHeader());
                webClientFilters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.health()));
                webClientFilters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.info()));
                webClientFilters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.env()));
                webClientFilters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.httptrace()));
                webClientFilters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.threaddump()));
                webClientFilters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.liquibase()));
                webClientFilters.add(InstanceExchangeFilterFunctions.convertLegacyEndpoint(LegacyEndpointConverters.flyway()));
                webClientFilters.add(InstanceExchangeFilterFunctions.retry(this.defaultRetries, this.retriesPerEndpoint));
                this.filters.forEach(filter -> webClientFilters.add(InstanceExchangeFilterFunctions.toExchangeFilterFunction(
                    filter)));
            });

            webClientCustomizer.customize(webClientBuilder);
            return new InstanceWebClient(webClientBuilder.build());
        }


        private static WebClient.Builder createDefaultWebClient(Duration connectTimeout, Duration readTimeout) {
            HttpClient httpClient = HttpClient.create()
                                              .compress(true)
                                              .tcpConfiguration(tcp -> tcp.bootstrap(bootstrap -> bootstrap.option(
                                                  ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                                  (int) connectTimeout.toMillis()
                                              )).observe((connection, newState) -> {
                                                  if (ConnectionObserver.State.CONNECTED.equals(newState)) {
                                                      connection.addHandlerLast(new ReadTimeoutHandler(readTimeout.toMillis(),
                                                          TimeUnit.MILLISECONDS
                                                      ));
                                                  }
                                              }));
            ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
            return WebClient.builder().clientConnector(connector);
        }
    }
}
