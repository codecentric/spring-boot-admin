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

package de.codecentric.boot.admin.server.notify.filter;

import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationIdNotificationFilterTest {
    @Test
    public void test_filterByName() {

        NotificationFilter filter = new ApplicationIdNotificationFilter(ApplicationId.of("cafebabe"), -1L);

        Application filteredApplication = Application.create(ApplicationId.of("cafebabe"),
                Registration.create("foo", "http://health").build()).build();
        ClientApplicationRegisteredEvent filteredEvent = new ClientApplicationRegisteredEvent(filteredApplication,
                filteredApplication.getRegistration());
        assertThat(filter.filter(filteredEvent)).isTrue();

        Application ignoredApplication = Application.create(ApplicationId.of("-"),
                Registration.create("foo", "http://health").build()).build();
        ClientApplicationRegisteredEvent ignoredEvent = new ClientApplicationRegisteredEvent(ignoredApplication,
                ignoredApplication.getRegistration());
        assertThat(filter.filter(ignoredEvent)).isFalse();
    }
}
