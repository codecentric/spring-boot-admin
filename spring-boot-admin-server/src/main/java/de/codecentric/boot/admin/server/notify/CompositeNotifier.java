/*
 * Copyright 2014-2018 the original author or authors.
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.util.Assert;

/**
 * A notifier delegating notifications to all specified notifiers.
 *
 * @author Sebastian Meiser
 */
public class CompositeNotifier implements Notifier {
    private final Iterable<Notifier> delegates;

    public CompositeNotifier(Iterable<Notifier> delegates) {
        Assert.notNull(delegates, "'delegates' must not be null!");
        this.delegates = delegates;
    }

    @Override
    public Mono<Void> notify(InstanceEvent event) {
        return Flux.fromIterable(delegates).flatMap(d -> d.notify(event)).then();
    }
}
