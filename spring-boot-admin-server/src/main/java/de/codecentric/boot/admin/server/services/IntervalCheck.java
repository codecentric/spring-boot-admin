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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import de.codecentric.boot.admin.server.domain.values.InstanceId;

/**
 * Calls the checkFn for all instances in the given time, but not before the given
 * retention time has passed. The instances which will be checked have to be registered
 * via `markAsChecked`.
 *
 * @author Johannes Edmeier
 */
@Slf4j
public class IntervalCheck {

	private final String name;

	private final Map<InstanceId, Instant> lastChecked = new ConcurrentHashMap<>();

	private final Function<InstanceId, Mono<Void>> checkFn;

	@Setter
	private Duration maxBackoff;

	@Getter
	@Setter
	private Duration interval;

	@Setter
	private Duration minRetention;

	@Nullable
	private Disposable subscription;

	@Nullable
	private Scheduler scheduler;

	public IntervalCheck(String name, Function<InstanceId, Mono<Void>> checkFn, Duration interval,
			Duration minRetention, Duration maxBackoff) {
		this.name = name;
		this.checkFn = checkFn;
		this.interval = interval;
		this.minRetention = minRetention;
		this.maxBackoff = maxBackoff;
	}

	public void start() {
		this.scheduler = Schedulers.newSingle(this.name + "-check");
		this.subscription = Flux.interval(this.interval)
			.doOnSubscribe((s) -> log.debug("Scheduled {}-check every {}", this.name, this.interval))
			.log(log.getName(), Level.FINEST)
			.subscribeOn(this.scheduler)
			.concatMap((i) -> this.checkAllInstances())
			.retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
				.maxBackoff(maxBackoff)
				.doBeforeRetry((s) -> log.warn("Unexpected error in {}-check", this.name, s.failure())))
			.subscribe(null, (error) -> log.error("Unexpected error in {}-check", name, error));
	}

	public void markAsChecked(InstanceId instanceId) {
		this.lastChecked.put(instanceId, Instant.now());
	}

	protected Mono<Void> checkAllInstances() {
		log.debug("check {} for all instances", this.name);
		Instant expiration = Instant.now().minus(this.minRetention);
		return Flux.fromIterable(this.lastChecked.entrySet())
			.filter((entry) -> entry.getValue().isBefore(expiration))
			.map(Map.Entry::getKey)
			.flatMap(this.checkFn)
			.then();
	}

	public void stop() {
		if (this.subscription != null) {
			this.subscription.dispose();
			this.subscription = null;
		}
		if (this.scheduler != null) {
			this.scheduler.dispose();
			this.scheduler = null;
		}
	}

}
