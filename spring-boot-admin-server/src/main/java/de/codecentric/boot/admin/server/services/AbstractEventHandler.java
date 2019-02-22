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

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.retry.Retry;

import java.util.logging.Level;
import javax.annotation.Nullable;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractEventHandler<T extends InstanceEvent> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Publisher<InstanceEvent> publisher;
    private final Class<T> eventType;
    @Nullable
    private Disposable subscription;

    protected AbstractEventHandler(Publisher<InstanceEvent> publisher, Class<T> eventType) {
        this.publisher = publisher;
        this.eventType = eventType;
    }

    public void start() {
        subscription = Flux.from(publisher)
                           .log(log.getName(), Level.FINEST)
                           .doOnSubscribe(s -> log.debug("Subscribed to {} events", eventType))
                           .ofType(eventType)
                           .cast(eventType)
                           .transform(this::handle)
                           .retryWhen(Retry.any()
                                           .retryMax(Long.MAX_VALUE)
                                           .doOnRetry(ctx -> log.warn("Unexpected error", ctx.exception())))
                           .subscribe();
    }

    protected abstract Publisher<Void> handle(Flux<T> publisher);

    public void stop() {
        if (subscription != null) {
            subscription.dispose();
        }
    }
}
