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
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IntervalCheckTest {

	private static final InstanceId INSTANCE_ID = InstanceId.of("Test");

	@SuppressWarnings("unchecked")
	private final Function<InstanceId, Mono<Void>> checkFn = mock(Function.class, (i) -> Mono.empty());

	private final IntervalCheck intervalCheck = new IntervalCheck("test", this.checkFn, Duration.ofMillis(10),
			Duration.ofMillis(10), Duration.ofSeconds(1));

	@Test
	void should_check_after_being_started() {
		this.intervalCheck.markAsChecked(INSTANCE_ID);

		this.intervalCheck.start();
		await().atMost(Duration.ofMillis(100))
			.pollInterval(Duration.ofMillis(10))
			.untilAsserted(() -> verify(this.checkFn, atLeastOnce()).apply(INSTANCE_ID));
	}

	@Test
	void should_not_check_when_stopped() {
		this.intervalCheck.markAsChecked(INSTANCE_ID);

		this.intervalCheck.stop();
		await().pollDelay(Duration.ofMillis(100)).untilAsserted(() -> verify(this.checkFn, never()).apply(any()));
	}

	@Test
	void should_not_check_in_retention_period() {
		this.intervalCheck.setMinRetention(Duration.ofSeconds(100));
		this.intervalCheck.markAsChecked(INSTANCE_ID);

		this.intervalCheck.start();
		await().pollDelay(Duration.ofMillis(100)).untilAsserted(() -> verify(this.checkFn, never()).apply(any()));
	}

	@Test
	void should_recheck_after_retention_period() {
		this.intervalCheck.setMinRetention(Duration.ofMillis(10));
		this.intervalCheck.markAsChecked(INSTANCE_ID);

		this.intervalCheck.start();
		await().atMost(Duration.ofMillis(100))
			.pollInterval(Duration.ofMillis(10))
			.untilAsserted(() -> verify(this.checkFn, atLeast(2)).apply(INSTANCE_ID));
	}

	@Test
	void should_not_wait_longer_than_maxBackoff() {
		this.intervalCheck.setInterval(Duration.ofMillis(10));
		this.intervalCheck.setMinRetention(Duration.ofMillis(10));
		this.intervalCheck.setMaxBackoff(Duration.ofSeconds(2));
		this.intervalCheck.markAsChecked(INSTANCE_ID);

		when(this.checkFn.apply(any())).thenReturn(Mono.error(new RuntimeException("Test")));

		this.intervalCheck.start();
		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> verify(this.checkFn, atLeast(7)).apply(INSTANCE_ID));
	}

	@Test
	void should_check_after_error() {
		this.intervalCheck.markAsChecked(INSTANCE_ID);

		when(this.checkFn.apply(any())).thenReturn(Mono.error(new RuntimeException("Test"))).thenReturn(Mono.empty());

		this.intervalCheck.start();
		await().atMost(Duration.ofMillis(1500))
			.untilAsserted(() -> verify(this.checkFn, atLeast(2)).apply(InstanceId.of("Test")));
	}

	@AfterEach
	void tearDown() {
		this.intervalCheck.stop();
	}

}
