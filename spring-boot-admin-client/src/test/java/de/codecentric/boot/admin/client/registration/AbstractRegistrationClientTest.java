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

package de.codecentric.boot.admin.client.registration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class AbstractRegistrationClientTest {

	private final WireMockServer wireMock = new WireMockServer(
			options().dynamicPort().notifier(new ConsoleNotifier(true)));

	private final Application application = Application.create("AppName")
		.managementUrl("http://localhost:8080/mgmt")
		.healthUrl("http://localhost:8080/health")
		.serviceUrl("http://localhost:8080")
		.build();

	private RegistrationClient registrationClient;

	public void setUp(RegistrationClient registrationClient) {
		this.registrationClient = registrationClient;
	}

	@BeforeEach
	void setUpWiremock() {
		wireMock.start();
	}

	@AfterEach
	void tearDown() {
		wireMock.stop();
	}

	@Test
	public void register_should_return_id_when_successful() {
		ResponseDefinitionBuilder response = created().withHeader("Content-Type", "application/json")
			.withHeader("Location", this.wireMock.url("/instances/abcdef"))
			.withBody("{ \"id\" : \"-id-\" }");
		this.wireMock.stubFor(post(urlEqualTo("/instances")).willReturn(response));

		assertThat(this.registrationClient.register(this.wireMock.url("/instances"), this.application))
			.isEqualTo(Optional.of("-id-"));

		RequestPatternBuilder expectedRequest = postRequestedFor(urlEqualTo("/instances"))
			.withHeader("Accept", equalTo("application/json"))
			.withHeader("Content-Type", equalTo("application/json"));
		this.wireMock.verify(expectedRequest);
	}

	@Test
	public void register_should_throw() {
		this.wireMock.stubFor(post(urlEqualTo("/instances")).willReturn(serverError()));

		assertThatThrownBy(() -> this.registrationClient.register(this.wireMock.url("/instances"), this.application))
			.isInstanceOf(Exception.class);
	}

	@Test
	public void deregister() {
		this.wireMock.stubFor(delete(urlEqualTo("/instances/-id-")).willReturn(ok()));
		this.registrationClient.deregister(this.wireMock.url("/instances"), "-id-");
		this.wireMock.verify(deleteRequestedFor(urlEqualTo("/instances/-id-")));
	}

	@Test
	public void deregister_should_trow() {
		this.wireMock.stubFor(delete(urlEqualTo("/instances/-id-")).willReturn(serverError()));
		assertThatThrownBy(() -> this.registrationClient.deregister(this.wireMock.url("/instances"), "-id-"))
			.isInstanceOf(Exception.class);
	}

}
