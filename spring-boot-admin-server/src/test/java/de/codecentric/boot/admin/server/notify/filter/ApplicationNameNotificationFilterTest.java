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

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Registration;

import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationNameNotificationFilterTest {

    @Test
    public void test_filterByName() {
        NotificationFilter filter = new ApplicationNameNotificationFilter("foo", -1L);

        Application filteredApplication = Application.create(ApplicationId.of("-"))
                                                     .register(Registration.create("foo", "http://health").build());
        ClientApplicationRegisteredEvent filteredEvent = new ClientApplicationRegisteredEvent(
                filteredApplication.getId(), filteredApplication.getVersion(), filteredApplication.getRegistration());
        assertThat(filter.filter(filteredEvent, filteredApplication)).isTrue();

        Application ignoredApplication = Application.create(ApplicationId.of("-"))
                                                    .register(Registration.create("bar", "http://health").build());
        ClientApplicationRegisteredEvent ignoredEvent = new ClientApplicationRegisteredEvent(ignoredApplication.getId(),
                ignoredApplication.getVersion(), ignoredApplication.getRegistration());
        assertThat(filter.filter(ignoredEvent, ignoredApplication)).isFalse();
    }

    @Test
    public void test_expiry() throws InterruptedException {
        ExpiringNotificationFilter filterForever = new ApplicationNameNotificationFilter("foo", -1L);
        ExpiringNotificationFilter filterExpired = new ApplicationNameNotificationFilter("foo", 0L);
        ExpiringNotificationFilter filterLong = new ApplicationNameNotificationFilter("foo",
                System.currentTimeMillis() + 500L);

        assertThat(filterForever.isExpired()).isFalse();
        assertThat(filterLong.isExpired()).isFalse();
        assertThat(filterExpired.isExpired()).isTrue();

        TimeUnit.MILLISECONDS.sleep(501);
        assertThat(filterLong.isExpired()).isTrue();
    }
}
