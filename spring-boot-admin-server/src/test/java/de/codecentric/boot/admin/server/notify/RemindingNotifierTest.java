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

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemindingNotifierTest {
    private final Instance instance1 = Instance.create(InstanceId.of("id-1"))
                                               .register(Registration.create("App", "http://health").build())
                                               .withStatusInfo(StatusInfo.ofDown());
    private final Instance instance2 = Instance.create(InstanceId.of("id-2"))
                                               .register(Registration.create("App", "http://health").build())
                                               .withStatusInfo(StatusInfo.ofDown());
    private final InstanceEvent appDown = new InstanceStatusChangedEvent(instance1.getId(), instance1.getVersion(),
            StatusInfo.ofDown());
    private final InstanceEvent appUp = new InstanceStatusChangedEvent(instance1.getId(), instance1.getVersion(),
            StatusInfo.ofUp());
    private final InstanceEvent otherAppUp = new InstanceStatusChangedEvent(instance2.getId(), 0L, StatusInfo.ofUp());
    private InstanceRepository repository;

    @Before
    public void setUp() {
        repository = mock(InstanceRepository.class);
        when(repository.find(instance1.getId())).thenReturn(Mono.just(instance1));
        when(repository.find(instance2.getId())).thenReturn(Mono.just(instance2));
    }

    @Test
    public void test_ctor_assert() {
        assertThatThrownBy(() -> new CompositeNotifier(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void test_remind() {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier, repository);
        reminder.setReminderPeriod(Duration.ZERO);

        StepVerifier.create(reminder.notify(appDown)).verifyComplete();
        StepVerifier.create(reminder.notify(otherAppUp)).verifyComplete();
        reminder.sendReminders();
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(appDown, otherAppUp, appDown, appDown);

    }

    @Test
    public void test_no_remind_after_up() {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier, repository);
        reminder.setReminderPeriod(Duration.ZERO);

        StepVerifier.create(reminder.notify(appDown)).verifyComplete();
        StepVerifier.create(reminder.notify(appUp)).verifyComplete();
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(appDown, appUp);
    }

    @Test
    public void test_no_remind_before_end() {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier, repository);
        reminder.setReminderPeriod(Duration.ofHours(24));

        StepVerifier.create(reminder.notify(appDown)).verifyComplete();
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(appDown);
    }

}
