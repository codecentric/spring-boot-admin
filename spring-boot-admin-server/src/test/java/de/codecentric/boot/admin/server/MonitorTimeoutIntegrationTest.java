/*
 * Copyright 2014-2024 the original author or authors.
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

package de.codecentric.boot.admin.server;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.services.StatusUpdater;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying that the monitor timeout configured via
 * {@code spring.boot.admin.monitor.default-timeout} is actually applied by
 * {@link StatusUpdater} when polling the health endpoint (see issue #5147).
 *
 * <p>
 * The test starts a minimal admin server on a random port. It registers an instance whose
 * health endpoint is backed by a {@link SlowHealthIndicator} and directly invokes
 * {@link StatusUpdater#updateStatus} to avoid waiting for the polling interval.
 *
 * <ul>
 * <li>No artificial delay → health responds within the effective timeout → instance stays
 * <strong>UP</strong></li>
 * <li>5 s delay &gt; 2 s effective timeout → WebClient times out → instance goes
 * <strong>OFFLINE</strong></li>
 * <li>After resetting the delay → instance recovers to <strong>UP</strong></li>
 * </ul>
 *
 * <p>
 * The configured {@code default-timeout} is 3 s. {@code StatusUpdater} subtracts a 1 s
 * margin before passing it to the WebClient, so the effective read timeout is 2 s.
 */
@SpringBootTest(classes = MonitorTimeoutIntegrationTest.TestAdminApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "spring.main.web-application-type=reactive", "spring.boot.admin.monitor.default-timeout=3s",
				"spring.boot.admin.monitor.status-interval=60s", "spring.boot.admin.monitor.status-lifetime=60s",
				"management.endpoints.web.exposure.include=health" })
class MonitorTimeoutIntegrationTest {

	@LocalServerPort
	private int port;

	@Autowired
	private SlowHealthIndicator slowHealthIndicator;

	@Autowired
	private StatusUpdater statusUpdater;

	@Autowired
	private InstanceRegistry instanceRegistry;

	@Autowired
	private InstanceRepository instanceRepository;

	private InstanceId instanceId;

	@BeforeEach
	void setUp() {
		this.slowHealthIndicator.setDelaySeconds(0);
		String healthUrl = "http://localhost:" + this.port + "/actuator/health";
		this.instanceId = this.instanceRegistry.register(Registration.create("timeout-test", healthUrl).build())
			.block();
	}

	@AfterEach
	void tearDown() {
		this.slowHealthIndicator.setDelaySeconds(0);
		if (this.instanceId != null) {
			this.instanceRegistry.deregister(this.instanceId).block();
		}
	}

	@Test
	void instanceIsUpWhenHealthRespondsWithinConfiguredTimeout() {
		triggerStatusUpdate();

		assertThat(findInstance().getStatusInfo().getStatus()).isEqualTo("UP");
	}

	@Test
	void instanceGoesOfflineWhenHealthExceedsConfiguredTimeout() {
		// 5 s delay exceeds the 2 s effective WebClient timeout (3 s configured − 1 s
		// margin).
		this.slowHealthIndicator.setDelaySeconds(5);
		triggerStatusUpdate();

		assertThat(findInstance().getStatusInfo().getStatus()).isEqualTo("OFFLINE");
	}

	@Test
	void instanceRecoversWhenHealthBecomesResponsiveAgain() {
		// Step 1: trigger a timeout → OFFLINE.
		this.slowHealthIndicator.setDelaySeconds(5);
		triggerStatusUpdate();
		assertThat(findInstance().getStatusInfo().getStatus()).isEqualTo("OFFLINE");

		// Step 2: remove the delay → health responds fast → UP again.
		this.slowHealthIndicator.setDelaySeconds(0);
		triggerStatusUpdate();
		assertThat(findInstance().getStatusInfo().getStatus()).isEqualTo("UP");
	}

	private void triggerStatusUpdate() {
		this.statusUpdater.updateStatus(this.instanceId).block(Duration.ofSeconds(15));
	}

	private Instance findInstance() {
		return this.instanceRepository.find(this.instanceId).block();
	}

	@SpringBootConfiguration
	@EnableAutoConfiguration
	@EnableAdminServer
	static class TestAdminApplication {

		@Bean
		SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
			return http.authorizeExchange((authorizeExchange) -> authorizeExchange.anyExchange().permitAll())
				.csrf(ServerHttpSecurity.CsrfSpec::disable)
				.build();
		}

		@Bean
		SlowHealthIndicator slowHealthIndicator() {
			return new SlowHealthIndicator();
		}

	}

	/**
	 * A reactive health indicator that introduces a configurable delay before responding.
	 * Used to simulate a slow downstream health endpoint for timeout verification.
	 */
	static class SlowHealthIndicator implements ReactiveHealthIndicator {

		private final AtomicLong delaySeconds = new AtomicLong(0);

		@Override
		public @NonNull Mono<Health> health() {
			long delay = this.delaySeconds.get();
			if (delay <= 0) {
				return Mono.just(Health.up().build());
			}
			return Mono.delay(Duration.ofSeconds(delay)).map((tick) -> Health.up().build());
		}

		void setDelaySeconds(long seconds) {
			this.delaySeconds.set(Math.max(0, seconds));
		}

	}

}
