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

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class TestNotifier implements Notifier {
    private List<InstanceEvent> events = new ArrayList<>();

    @Override
    public Mono<Void> notify(InstanceEvent event) {
        this.events.add(event);
        return Mono.empty();
    }

    public List<InstanceEvent> getEvents() {
        return events;
    }
}
