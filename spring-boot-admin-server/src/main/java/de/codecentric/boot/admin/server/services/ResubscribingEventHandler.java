/*
 * Copyright 2014-2017 the original author or authors.
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

import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.retry.Retry;

import java.util.logging.Level;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ResubscribingEventHandler<T extends ClientApplicationEvent> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final Publisher<ClientApplicationEvent> publisher;
    private Disposable subscription;
    private Class<T> eventType;

    protected ResubscribingEventHandler(Publisher<ClientApplicationEvent> publisher, Class<T> eventType) {
        this.publisher = publisher;
        this.eventType = eventType;
    }

    public void start() {
        subscription = Flux.from(publisher)
                           .log(log.getName(), Level.FINEST)
                           .doOnSubscribe(subscription -> log.debug("Subscribed to {} events", eventType))
                           .ofType(eventType)
                           .cast(eventType)
                           .compose(this::handle)
                           .retryWhen(Retry.any()
                                           .retryMax(Integer.MAX_VALUE)
                                           .doOnRetry(ctx -> log.error("Resubscribing after uncaught error",
                                                   ctx.exception())))
                           .subscribe();
    }

    protected abstract Publisher<?> handle(Flux<T> publisher);

    public void stop() {
        if (subscription != null) {
            subscription.dispose();
        }
    }
}
