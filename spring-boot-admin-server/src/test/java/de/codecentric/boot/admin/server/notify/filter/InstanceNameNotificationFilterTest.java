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

package de.codecentric.boot.admin.server.notify.filter;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InstanceNameNotificationFilterTest {

    @Test
    public void test_filterByName() {
        NotificationFilter filter = new ApplicationNameNotificationFilter("foo", null);

        Instance filteredInstance = Instance.create(InstanceId.of("-"))
                                            .register(Registration.create("foo", "http://health").build());
        InstanceRegisteredEvent filteredEvent = new InstanceRegisteredEvent(filteredInstance.getId(),
            filteredInstance.getVersion(), filteredInstance.getRegistration());
        assertThat(filter.filter(filteredEvent, filteredInstance)).isTrue();

        Instance ignoredInstance = Instance.create(InstanceId.of("-"))
                                           .register(Registration.create("bar", "http://health").build());
        InstanceRegisteredEvent ignoredEvent = new InstanceRegisteredEvent(ignoredInstance.getId(),
            ignoredInstance.getVersion(), ignoredInstance.getRegistration());
        assertThat(filter.filter(ignoredEvent, ignoredInstance)).isFalse();
    }

    @Test
    public void test_expiry() throws InterruptedException {
        ExpiringNotificationFilter filterForever = new ApplicationNameNotificationFilter("foo", null);
        ExpiringNotificationFilter filterExpired = new ApplicationNameNotificationFilter("foo",
            Instant.now().minus(Duration.ofSeconds(1)));
        ExpiringNotificationFilter filterLong = new ApplicationNameNotificationFilter("foo",
            Instant.now().plus(Duration.ofMillis(100)));

        assertThat(filterForever.isExpired()).isFalse();
        assertThat(filterLong.isExpired()).isFalse();
        assertThat(filterExpired.isExpired()).isTrue();

        TimeUnit.MILLISECONDS.sleep(200);
        assertThat(filterLong.isExpired()).isTrue();
    }
}
