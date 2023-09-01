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

package de.codecentric.boot.admin.server.services;

import java.time.Duration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.Options;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.EventsourcingInstanceRepository;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceInfoChangedEvent;
import de.codecentric.boot.admin.server.domain.values.Endpoint;
import de.codecentric.boot.admin.server.domain.values.Endpoints;
import de.codecentric.boot.admin.server.domain.values.Info;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.eventstore.InMemoryEventStore;
import de.codecentric.boot.admin.server.web.client.InstanceWebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.retry;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.rewriteEndpointUrl;
import static de.codecentric.boot.admin.server.web.client.InstanceExchangeFilterFunctions.timeout;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

public class InfoUpdaterTest {

	public WireMockServer wireMock = new WireMockServer(Options.DYNAMIC_PORT);

	private InfoUpdater updater;

	private InMemoryEventStore eventStore;

	private InstanceRepository repository;

	private final ApiMediaTypeHandler apiMediaTypeHandler = new ApiMediaTypeHandler();

	@BeforeEach
	public void setup() {
		this.eventStore = new InMemoryEventStore();
		this.repository = new EventsourcingInstanceRepository(this.eventStore);
		this.updater = new InfoUpdater(this.repository,
				InstanceWebClient.builder()
					.filter(rewriteEndpointUrl())
					.filter(retry(0, singletonMap(Endpoint.INFO, 1)))
					.filter(timeout(Duration.ofSeconds(2), emptyMap()))
					.build(),
				this.apiMediaTypeHandler);
		this.wireMock.start();
	}

	@AfterEach
	public void teardown() {
		this.wireMock.stop();
	}

	@BeforeAll
	public static void setUp() {
		StepVerifier.setDefaultTimeout(Duration.ofSeconds(5));
	}

	@AfterAll
	public static void tearDown() {
		StepVerifier.resetDefaultTimeout();
	}

	@Test
	void should_update_info_for_online_with_info_endpoint_only() {
		// given
		Registration registration = Registration.create("foo", this.wireMock.url("/health")).build();
		Instance instance = Instance.create(InstanceId.of("onl"))
			.register(registration)
			.withEndpoints(Endpoints.single("info", this.wireMock.url("/info")))
			.withStatusInfo(StatusInfo.ofUp());
		StepVerifier.create(this.repository.save(instance)).expectNextCount(1).verifyComplete();
		String body = "{ \"foo\": \"bar\" }";
		this.wireMock.stubFor(
				get("/info").willReturn(okJson(body).withHeader("Content-Length", Integer.toString(body.length()))));

		Instance noInfo = Instance.create(InstanceId.of("noinfo"))
			.register(registration)
			.withEndpoints(Endpoints.single("beans", this.wireMock.url("/beans")))
			.withStatusInfo(StatusInfo.ofUp());
		StepVerifier.create(this.repository.save(noInfo)).expectNextCount(1).verifyComplete();

		Instance offline = Instance.create(InstanceId.of("off"))
			.register(registration)
			.withStatusInfo(StatusInfo.ofOffline());
		StepVerifier.create(this.repository.save(offline)).expectNextCount(1).verifyComplete();

		Instance unknown = Instance.create(InstanceId.of("unk"))
			.register(registration)
			.withStatusInfo(StatusInfo.ofUnknown());
		StepVerifier.create(this.repository.save(unknown)).expectNextCount(1).verifyComplete();

		// when
		StepVerifier.create(this.eventStore)
			.expectSubscription()
			.then(() -> StepVerifier.create(this.updater.updateInfo(offline.getId())).verifyComplete())
			.then(() -> StepVerifier.create(this.updater.updateInfo(unknown.getId())).verifyComplete())
			.then(() -> StepVerifier.create(this.updater.updateInfo(noInfo.getId())).verifyComplete())
			.expectNoEvent(Duration.ofMillis(100L))
			.then(() -> StepVerifier.create(this.updater.updateInfo(instance.getId())).verifyComplete())
			// then
			.assertNext((event) -> assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class))
			.thenCancel()
			.verify();

		StepVerifier.create(this.repository.find(instance.getId()))
			.assertNext((app) -> assertThat(app.getInfo()).isEqualTo(Info.from(singletonMap("foo", "bar"))))
			.verifyComplete();
	}

	@Test
	void should_clear_info_on_http_error() {
		// given
		Instance instance = Instance.create(InstanceId.of("onl"))
			.register(Registration.create("foo", this.wireMock.url("/health")).build())
			.withEndpoints(Endpoints.single("info", this.wireMock.url("/info")))
			.withStatusInfo(StatusInfo.ofUp())
			.withInfo(Info.from(singletonMap("foo", "bar")));
		StepVerifier.create(this.repository.save(instance)).expectNextCount(1).verifyComplete();

		this.wireMock.stubFor(get("/info").willReturn(serverError()));

		// when
		StepVerifier.create(this.eventStore)
			.expectSubscription()
			.then(() -> StepVerifier.create(this.updater.updateInfo(instance.getId())).verifyComplete())
			// then
			.assertNext((event) -> assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class))
			.thenCancel()
			.verify();

		StepVerifier.create(this.repository.find(instance.getId()))
			.assertNext((app) -> assertThat(app.getInfo()).isEqualTo(Info.empty()))
			.verifyComplete();
	}

	@Test
	void should_clear_info_on_exception() {

		// given
		Instance instance = Instance.create(InstanceId.of("onl"))
			.register(Registration.create("foo", this.wireMock.url("/health")).build())
			.withEndpoints(Endpoints.single("info", this.wireMock.url("/info")))
			.withStatusInfo(StatusInfo.ofUp())
			.withInfo(Info.from(singletonMap("foo", "bar")));
		StepVerifier.create(this.repository.save(instance)).expectNextCount(1).verifyComplete();

		this.wireMock.stubFor(get("/info").willReturn(okJson("{ \"foo\": \"bar\" }").withFixedDelay(2100)));

		// when
		StepVerifier.create(this.eventStore)
			.expectSubscription()
			.then(() -> StepVerifier.create(this.updater.updateInfo(instance.getId())).verifyComplete())
			// then
			.assertNext((event) -> assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class))
			.thenCancel()
			.verify();

		StepVerifier.create(this.repository.find(instance.getId()))
			.assertNext((app) -> assertThat(app.getInfo()).isEqualTo(Info.empty()))
			.verifyComplete();
	}

	@Test
	void should_retry() {
		// given
		Registration registration = Registration.create("foo", this.wireMock.url("/health")).build();
		Instance instance = Instance.create(InstanceId.of("onl"))
			.register(registration)
			.withEndpoints(Endpoints.single("info", this.wireMock.url("/info")))
			.withStatusInfo(StatusInfo.ofUp());
		StepVerifier.create(this.repository.save(instance)).expectNextCount(1).verifyComplete();

		this.wireMock.stubFor(get("/info").inScenario("retry")
			.whenScenarioStateIs(STARTED)
			.willReturn(aResponse().withFixedDelay(5000))
			.willSetStateTo("recovered"));

		String body = "{ \"foo\": \"bar\" }";
		this.wireMock.stubFor(get("/info").inScenario("retry")
			.whenScenarioStateIs("recovered")
			.willReturn(okJson(body).withHeader("Content-Length", Integer.toString(body.length()))));

		// when
		StepVerifier.create(this.eventStore)
			.expectSubscription()
			.then(() -> StepVerifier.create(this.updater.updateInfo(instance.getId())).verifyComplete())
			// then
			.assertNext((event) -> assertThat(event).isInstanceOf(InstanceInfoChangedEvent.class))
			.thenCancel()
			.verify();

		StepVerifier.create(this.repository.find(instance.getId()))
			.assertNext((app) -> assertThat(app.getInfo()).isEqualTo(Info.from(singletonMap("foo", "bar"))))
			.verifyComplete();
	}

}
