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

package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import de.codecentric.boot.admin.server.services.ResubscribingEventHandler;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import org.reactivestreams.Publisher;

public class NotificationTrigger extends ResubscribingEventHandler<ClientApplicationEvent> {
    private final Notifier notifier;

    public NotificationTrigger(Notifier notifier, Publisher<ClientApplicationEvent> publisher) {
        super(publisher, ClientApplicationEvent.class);
        this.notifier = notifier;
    }

    @Override
    protected Publisher<?> handle(Flux<ClientApplicationEvent> publisher) {
        return publisher.subscribeOn(Schedulers.newSingle("notifications")).doOnNext(this::sendNotifications);
    }

    protected void sendNotifications(ClientApplicationEvent event) {
        notifier.notify(event);
    }
}
