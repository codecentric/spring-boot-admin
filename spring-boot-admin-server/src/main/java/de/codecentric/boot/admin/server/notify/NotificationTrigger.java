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

package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.services.AbstractEventHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.reactivestreams.Publisher;

public class NotificationTrigger extends AbstractEventHandler<InstanceEvent> {
    private final Notifier notifier;

    public NotificationTrigger(Notifier notifier, Publisher<InstanceEvent> publisher) {
        super(publisher, InstanceEvent.class);
        this.notifier = notifier;
    }

    @Override
    protected Publisher<Void> handle(Flux<InstanceEvent> publisher) {
        Scheduler scheduler = Schedulers.newSingle("notifications");
        return publisher.subscribeOn(scheduler).flatMap(this::sendNotifications).doFinally(s -> scheduler.dispose());
    }

    protected Mono<Void> sendNotifications(InstanceEvent event) {
        return notifier.notify(event);
    }
}
