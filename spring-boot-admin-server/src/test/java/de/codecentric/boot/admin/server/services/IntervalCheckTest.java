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
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import reactor.core.publisher.Mono;

import de.codecentric.boot.admin.server.domain.values.InstanceId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
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

	@Test
	void should_not_overflow_when_checks_timeout_randomly() {
		Duration CHECK_INTERVAL = Duration.ofMillis(500);

		@SuppressWarnings("unchecked")
		Function<InstanceId, Mono<Void>> timeoutCheckFn = mock(Function.class);

		doAnswer((invocation) -> {
			if (Math.random() < 0.5) {
				// Sometimes succeed quickly
				return Mono.empty();
			}
			else {
				// Sometimes timeout
				return Mono.just("slow response").delayElement(CHECK_INTERVAL.plus(Duration.ofSeconds(1))).then();
			}
		}).when(timeoutCheckFn).apply(any());

		IntervalCheck timeoutCheck = new IntervalCheck("overflow-test", timeoutCheckFn, CHECK_INTERVAL, CHECK_INTERVAL,
				Duration.ofSeconds(1));

		List<Throwable> retryErrors = new CopyOnWriteArrayList<>();

		timeoutCheck.setRetryConsumer(retryErrors::add);
		timeoutCheck.markAsChecked(INSTANCE_ID);
		timeoutCheck.start();
		try {
			await().pollDelay(Duration.ofSeconds(5))
				.until(() -> retryErrors.stream()
					.noneMatch((Throwable er) -> "OverflowException".equalsIgnoreCase(er.getClass().getSimpleName())));

			assertThat(retryErrors).noneMatch(
					(Throwable e) -> "OverflowException".equalsIgnoreCase(e.getCause().getClass().getSimpleName()));
		}
		finally {
			timeoutCheck.stop();
		}
	}

	@Test
	void should_not_lose_checks_under_backpressure() {
		Duration CHECK_INTERVAL = Duration.ofMillis(100);

		@SuppressWarnings("unchecked")
		Function<InstanceId, Mono<Void>> slowCheckFn = mock(Function.class);
		doAnswer((invocation) -> Mono.delay(CHECK_INTERVAL.plus(Duration.ofMillis(50))).then()).when(slowCheckFn)
			.apply(any());

		IntervalCheck slowCheck = new IntervalCheck("backpressure-test", slowCheckFn, CHECK_INTERVAL,
				Duration.ofMillis(50), Duration.ofSeconds(1));

		List<Long> checkTimes = new CopyOnWriteArrayList<>();
		doAnswer((invocation) -> {
			checkTimes.add(System.currentTimeMillis());
			return Mono.empty();
		}).when(slowCheckFn).apply(any());

		slowCheck.markAsChecked(INSTANCE_ID);
		slowCheck.start();

		try {
			await().atMost(Duration.ofSeconds(2)).until(() -> checkTimes.size() >= 5);
			// With onBackpressureLatest, we should have processed multiple checks without
			// drops
			assertThat(checkTimes).hasSizeGreaterThanOrEqualTo(5);
		}
		finally {
			slowCheck.stop();
		}
	}

	@Test
	void should_not_lose_checks_under_backpressure_latest() {
		Duration CHECK_INTERVAL = Duration.ofMillis(100);

		@SuppressWarnings("unchecked")
		Function<InstanceId, Mono<Void>> slowCheckFn = mock(Function.class);

		IntervalCheck slowCheck = new IntervalCheck("backpressure-test", slowCheckFn, CHECK_INTERVAL,
				Duration.ofMillis(50), Duration.ofSeconds(1));

		// Add multiple instances to increase load and cause drops
		Set<InstanceId> instanceIds = IntStream.range(0, 50)
			.mapToObj((i) -> InstanceId.of("Test" + i))
			.collect(Collectors.toSet());

		instanceIds.forEach(
				(InstanceId instanceId) -> slowCheck.getLastChecked().put(instanceId, Instant.now().minusMillis(1000)));

		List<Long> checkTimes = new CopyOnWriteArrayList<>();
		Map<String, List<Long>> checkTimesPerInstance = new ConcurrentHashMap<>();

		doAnswer((invocation) -> {
			long checkTime = System.currentTimeMillis();
			String instanceId = instanceIdString(invocation);
			List<Long> checkTimesInstance = checkTimesPerInstance.computeIfAbsent(instanceId,
					(String k) -> new CopyOnWriteArrayList<>());
			checkTimesInstance.add(checkTime);
			checkTimes.add(checkTime);
			if (Math.random() < 0.5) {
				// Sometimes succeed quickly
				return Mono.empty();
			}
			else {
				// Sometimes slow
				return Mono.delay(CHECK_INTERVAL.plus(Duration.ofMillis(100))).then();
			}
		}).when(slowCheckFn).apply(any());

		slowCheck.start();

		try {
			await().atMost(Duration.ofMillis(1500)).until(() -> checkTimes.size() >= 500);
			// With onBackpressureLatest, we should process more checks without drops
			instanceIds.forEach((InstanceId instanceId) -> assertThat(checkTimesPerInstance.get(instanceId.getValue()))
				.hasSizeGreaterThanOrEqualTo(10));
		}
		finally {
			slowCheck.stop();
		}
	}

	@AfterEach
	void tearDown() {
		this.intervalCheck.stop();
	}

	private static String instanceIdString(InvocationOnMock invocation) {
		return invocation.getArguments()[0].toString();
	}

}
