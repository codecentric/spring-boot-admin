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

package de.codecentric.boot.admin.server.config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.http.client.FilteredHostException;
import org.springframework.boot.http.client.autoconfigure.HttpClientAutoConfiguration;
import org.springframework.boot.http.client.autoconfigure.reactive.ReactiveHttpClientAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webclient.autoconfigure.WebClientAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies that the outbound {@link InstanceWebClient} enforces the SSRF
 * {@code InetAddressFilter} at connection time. This is what makes hosts that are not
 * resolvable (or resolve to a different address) during registration safe even though the
 * registration-time check lets unresolvable hosts pass.
 */
class SsrfConnectionFilterTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
		.withConfiguration(AutoConfigurations.of(HttpClientAutoConfiguration.class,
				ReactiveHttpClientAutoConfiguration.class, WebClientAutoConfiguration.class,
				WebMvcAutoConfiguration.class, AdminServerAutoConfiguration.class))
		.withUserConfiguration(AdminServerMarkerConfiguration.class);

	private HttpServer loopbackServer;

	@BeforeEach
	void startLoopbackServer() throws IOException {
		this.loopbackServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		this.loopbackServer.createContext("/", (exchange) -> {
			byte[] body = "internal".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			exchange.getResponseBody().write(body);
			exchange.close();
		});
		this.loopbackServer.start();
	}

	@AfterEach
	void stopLoopbackServer() {
		this.loopbackServer.stop(0);
	}

	@Test
	void shouldBlockLoopbackConnectionWhenSsrfProtectionEnabled() {
		this.contextRunner.withPropertyValues("spring.boot.admin.ssrf-protection.enabled=true")
			.run(withInstanceWebClient((client) -> assertThatThrownBy(() -> get(client))
				.hasRootCauseInstanceOf(FilteredHostException.class)));
	}

	@Test
	void shouldAllowLoopbackConnectionWhenSsrfProtectionDisabled() {
		this.contextRunner.run(withInstanceWebClient((client) -> assertThat(get(client)).isEqualTo("internal")));
	}

	@ParameterizedTest
	@ValueSource(strings = { "169.254.169.254", "10.0.0.1", "172.16.0.1", "192.168.1.1", "100.64.0.1", "0.0.0.1",
			"[::1]", "[fe80::1]", "[fd00::1]" })
	void shouldBlockInternalAddressesWhenSsrfProtectionEnabled(String host) {
		this.contextRunner.withPropertyValues("spring.boot.admin.ssrf-protection.enabled=true")
			.run(withInstanceWebClient((client) -> assertThatThrownBy(() -> client.get()
				.uri("http://" + host + "/latest/meta-data/")
				.retrieve()
				.bodyToMono(String.class)
				.block()).hasRootCauseInstanceOf(FilteredHostException.class)));
	}

	private String get(WebClient client) {
		// "localhost" is resolved by the HTTP client at connection time, not by the
		// registration-time validator
		String url = "http://localhost:" + this.loopbackServer.getAddress().getPort() + "/";
		return client.get().uri(url).retrieve().bodyToMono(String.class).block();
	}

	private ContextConsumer<AssertableWebApplicationContext> withInstanceWebClient(Consumer<WebClient> assertions) {
		return (context) -> {
			Instance instance = Instance.create(InstanceId.of("id-1"))
				.register(Registration.create("test", "http://localhost/health").build());
			assertions.accept(context.getBean(InstanceWebClient.Builder.class).build().instance(instance));
		};
	}

}
