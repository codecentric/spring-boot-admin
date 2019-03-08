/*
 * Copyright 2014-2019 the original author or authors.
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

package de.codecentric.boot.admin.server.services;

import de.codecentric.boot.admin.server.domain.values.InstanceId;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.retry.Retry;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calls the checkFn for all instances in the given time, but not before the given retention time has passed.
 * The instances which will be checked have to be registered via `markAsChecked`.
 *
 * @author Johannes Edmeier
 */
public class IntervalCheck {
    private static final Logger log = LoggerFactory.getLogger(IntervalCheck.class);
    private final String name;
    private final Map<InstanceId, Instant> lastChecked = new ConcurrentHashMap<>();
    private final Function<InstanceId, Mono<Void>> checkFn;
    private Duration interval;
    private Duration minRetention;
    @Nullable
    private Disposable subscription;

    public IntervalCheck(String name, Function<InstanceId, Mono<Void>> checkFn) {
        this(name, checkFn, Duration.ofSeconds(10), Duration.ofSeconds(10));
    }

    public IntervalCheck(String name,
                         Function<InstanceId, Mono<Void>> checkFn,
                         Duration interval,
                         Duration minRetention) {
        this.name = name;
        this.checkFn = checkFn;
        this.interval = interval;
        this.minRetention = minRetention;
    }

    public void start() {
        Scheduler scheduler = Schedulers.newSingle(this.name + "-check");
        this.subscription = Flux.interval(this.interval)
                                .doOnSubscribe(s -> log.debug("Scheduled {}-check every {}", this.name, this.interval))
                                .log(log.getName(), Level.FINEST)
                                .subscribeOn(scheduler)
                                .concatMap(i -> this.checkAllInstances())
                                .retryWhen(Retry.any()
                                                .retryMax(Long.MAX_VALUE)
                                                .doOnRetry(ctx -> log.warn("Unexpected error in {}-check",
                                                    this.name,
                                                    ctx.exception()
                                                )))
                                .doFinally(s -> scheduler.dispose())
                                .subscribe();
    }

    public void markAsChecked(InstanceId instanceId) {
        this.lastChecked.put(instanceId, Instant.now());
    }

    protected Mono<Void> checkAllInstances() {
        log.debug("check {} for all instances", this.name);
        Instant expiration = Instant.now().minus(this.minRetention);
        return Flux.fromIterable(this.lastChecked.entrySet())
                   .filter(e -> e.getValue().isBefore(expiration))
                   .map(Map.Entry::getKey)
                   .flatMap(this.checkFn)
                   .then();
    }

    public void stop() {
        if (this.subscription != null) {
            this.subscription.dispose();
        }
    }

    public void setInterval(Duration interval) {
        this.interval = interval;
    }

    public void setMinRetention(Duration minRetention) {
        this.minRetention = minRetention;
    }
}
