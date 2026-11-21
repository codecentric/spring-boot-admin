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

package de.codecentric.boot.admin.server.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.client.autoconfigure.reactive.ReactiveHttpClientAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.HttpResources;
import reactor.netty.resources.ConnectionProvider;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider;
import de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunction;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;
import de.codecentric.boot.admin.server.web.client.LegacyEndpointConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class AdminServerInstanceWebClientConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(ReactiveHttpClientAutoConfiguration.class,
				WebClientAutoConfiguration.class, WebMvcAutoConfiguration.class, AdminServerAutoConfiguration.class,
				AdminServerInstanceWebClientConfiguration.class))
		.withUserConfiguration(AdminServerMarkerConfiguration.class);

	@Test
	void simpleConfig() {
		this.contextRunner.run((context) -> {
			assertThat(context).hasSingleBean(InstanceWebClient.Builder.class);
			assertThat(context).hasBean("filterInstanceWebClientCustomizer");
			assertThat(context).hasSingleBean(BasicAuthHttpHeaderProvider.class);
			assertThat(context).getBeanNames(InstanceExchangeFilterFunction.class)
				.containsExactly("addHeadersInstanceExchangeFilter", "rewriteEndpointUrlInstanceExchangeFilter",
						"setDefaultAcceptHeaderInstanceExchangeFilter", "legacyEndpointConverterInstanceExchangeFilter",
						"logfileAcceptWorkaround", "cookieHandlingInstanceExchangeFilter",
						"retryInstanceExchangeFilter", "timeoutInstanceExchangeFilter");
			assertThat(context).getBeanNames(LegacyEndpointConverter.class)
				.containsExactly("healthLegacyEndpointConverter", "infoLegacyEndpointConverter",
						"envLegacyEndpointConverter", "httptraceLegacyEndpointConverter",
						"threaddumpLegacyEndpointConverter", "liquibaseLegacyEndpointConverter",
						"flywayLegacyEndpointConverter", "beansLegacyEndpointConverter",
						"configpropsLegacyEndpointConverter", "mappingsLegacyEndpointConverter",
						"startupLegacyEndpointConverter");
		});
	}

	@Test
	void shouldApplyConfiguredConnectionPoolProperties() {
		this.contextRunner
			.withPropertyValues("spring.boot.admin.monitor.connection-pool.max-idle-time=5s",
					"spring.boot.admin.monitor.connection-pool.eviction-interval=10s",
					"spring.boot.admin.monitor.connection-pool.inactive-pool-timeout=2m")
			.run((context) -> assertThat(context.getBean(AdminServerInstanceWebClientConfiguration.class))
				.extracting("connectionProvider")
				.extracting("defaultPoolFactory.maxIdleTime", "defaultPoolFactory.evictionInterval",
						"inactivePoolDisposeInterval", "poolInactivity")
				.containsExactly(5_000L, Duration.ofSeconds(10), Duration.ofSeconds(10), Duration.ofMinutes(2)));
	}

	@Test
	void shouldKeepConnectionLimitOfGlobalConnectionPool() {
		this.contextRunner.run((context) -> assertThat(context.getBean(AdminServerInstanceWebClientConfiguration.class))
			.extracting("connectionProvider")
			.isInstanceOfSatisfying(ConnectionProvider.class,
					(connectionProvider) -> assertThat(connectionProvider.maxConnections())
						.isEqualTo(HttpResources.get().maxConnections())));
	}

	@Test
	void shouldKeepUserDefinedClientHttpConnector() throws IOException {
		AtomicInteger requests = new AtomicInteger();
		ClientHttpConnector delegate = new ReactorClientHttpConnector();
		ClientHttpConnector userConnector = (method, uri, requestCallback) -> {
			requests.incrementAndGet();
			return delegate.connect(method, uri, requestCallback);
		};
		HttpServer loopbackServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		loopbackServer.createContext("/", (exchange) -> {
			exchange.sendResponseHeaders(200, -1);
			exchange.close();
		});
		loopbackServer.start();
		try {
			String url = "http://localhost:" + loopbackServer.getAddress().getPort() + "/";
			Instance instance = Instance.create(InstanceId.of("id-1"))
				.register(Registration.create("test", "http://localhost/health").build());

			this.contextRunner.withBean(ClientHttpConnector.class, () -> userConnector)
				.run((context) -> context.getBean(InstanceWebClient.Builder.class)
					.build()
					.instance(instance)
					.get()
					.uri(url)
					.retrieve()
					.toBodilessEntity()
					.block(Duration.ofSeconds(10)));

			assertThat(requests).hasValue(1);
		}
		finally {
			loopbackServer.stop(0);
		}
	}

	@Test
	void shouldNotUseGlobalConnectionPoolForInstanceRequests() throws IOException {
		ConnectionProvider globalConnectionProvider = spy(ConnectionProvider.create("global"));
		HttpResources.set(globalConnectionProvider);
		HttpServer loopbackServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		loopbackServer.createContext("/", (exchange) -> {
			exchange.sendResponseHeaders(200, -1);
			exchange.close();
		});
		loopbackServer.start();
		try {
			String url = "http://localhost:" + loopbackServer.getAddress().getPort() + "/";
			Instance instance = Instance.create(InstanceId.of("id-1"))
				.register(Registration.create("test", "http://localhost/health").build());

			this.contextRunner.run((context) -> context.getBean(InstanceWebClient.Builder.class)
				.build()
				.instance(instance)
				.get()
				.uri(url)
				.retrieve()
				.toBodilessEntity()
				.block(Duration.ofSeconds(10)));

			verify(globalConnectionProvider, never()).acquire(any(), any(), any(), any());
		}
		finally {
			loopbackServer.stop(0);
			HttpResources.reset();
		}
	}

}
