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

package de.codecentric.boot.admin.server.services;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider;
import de.codecentric.boot.admin.server.web.client.BasicAuthHttpHeaderProvider.InstanceCredentials;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.addHeaders;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.enforceCredentialsAllowedHosts;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.rewriteEndpointUrl;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that server-managed credentials selected by the (client-supplied) registration
 * name are only sent to the hosts they are bound to.
 */
class InstanceCredentialsAllowedHostsTest {

	private static final String VICTIM_AUTHORIZATION = "Basic dmljdGltLXVzZXI6dmljdGltLXNlY3JldA==";

	private final WireMockServer server = new WireMockServer(Options.DYNAMIC_PORT);

	@BeforeEach
	void startServer() {
		this.server.start();
		this.server.stubFor(get("/health").willReturn(aResponse().withStatus(200)
			.withHeader("Content-Type", "application/json")
			.withBody("{\"status\":\"UP\"}")));
	}

	@AfterEach
	void stopServer() {
		this.server.stop();
	}

	@Test
	void credentialsAreNotSentToHostOutsideOfAllowedHosts() {
		BasicAuthHttpHeaderProvider provider = new BasicAuthHttpHeaderProvider(null, null, Map.of("victim-service",
				new InstanceCredentials("victim-user", "victim-secret", List.of("victim.example.com"))));

		updateStatus(provider, "victim-service", this.server.url("/health"));

		assertThat(this.server.getAllServeEvents()).hasSize(1);
		assertThat(this.server.getAllServeEvents().get(0).getRequest().getHeader("Authorization")).isNull();
	}

	@Test
	void credentialsAreSentToAllowedHost() {
		BasicAuthHttpHeaderProvider provider = new BasicAuthHttpHeaderProvider(null, null, Map.of("victim-service",
				new InstanceCredentials("victim-user", "victim-secret", List.of("localhost"))));

		updateStatus(provider, "victim-service", this.server.url("/health"));

		this.server
			.verify(getRequestedFor(urlEqualTo("/health")).withHeader("Authorization", equalTo(VICTIM_AUTHORIZATION)));
	}

	@Test
	void defaultCredentialsAreNotSentToHostOutsideOfDefaultAllowedHosts() {
		BasicAuthHttpHeaderProvider provider = new BasicAuthHttpHeaderProvider("victim-user", "victim-secret", Map.of(),
				List.of("*.svc.cluster.local"));

		updateStatus(provider, "any-service", this.server.url("/health"));

		assertThat(this.server.getAllServeEvents()).hasSize(1);
		assertThat(this.server.getAllServeEvents().get(0).getRequest().getHeader("Authorization")).isNull();
	}

	@Test
	void unboundCredentialsAreStillSentForBackwardsCompatibility() {
		BasicAuthHttpHeaderProvider provider = new BasicAuthHttpHeaderProvider(null, null,
				Map.of("victim-service", new InstanceCredentials("victim-user", "victim-secret")));

		updateStatus(provider, "victim-service", this.server.url("/health"));

		this.server
			.verify(getRequestedFor(urlEqualTo("/health")).withHeader("Authorization", equalTo(VICTIM_AUTHORIZATION)));
	}

	@Test
	void metadataCredentialsAreNotRestricted() {
		BasicAuthHttpHeaderProvider provider = new BasicAuthHttpHeaderProvider(null, null, Map.of("victim-service",
				new InstanceCredentials("other-user", "other-secret", List.of("victim.example.com"))));

		InstanceId id = InstanceId.of("instance");
		Registration registration = Registration.create("victim-service", this.server.url("/health"))
			.metadata("user.name", "victim-user")
			.metadata("user.password", "victim-secret")
			.build();
		updateStatus(provider, id, registration);

		this.server
			.verify(getRequestedFor(urlEqualTo("/health")).withHeader("Authorization", equalTo(VICTIM_AUTHORIZATION)));
	}

	private void updateStatus(BasicAuthHttpHeaderProvider provider, String name, String healthUrl) {
		updateStatus(provider, InstanceId.of("instance"), Registration.create(name, healthUrl).build());
	}

	private void updateStatus(BasicAuthHttpHeaderProvider provider, InstanceId id, Registration registration) {
		InstanceWebClient webClient = InstanceWebClient.builder()
			.filter(addHeaders(provider))
			.filter(rewriteEndpointUrl())
			.filter(enforceCredentialsAllowedHosts(provider))
			.build();

		InstanceRepository repository = new EventsourcingInstanceRepository(new InMemoryEventStore());
		repository.save(Instance.create(id).register(registration)).block(Duration.ofSeconds(5));

		new StatusUpdater(repository, webClient, new ApiMediaTypeHandler(), new InMemoryHealthGroupsCache())
			.updateStatus(id)
			.block(Duration.ofSeconds(5));
	}

}
