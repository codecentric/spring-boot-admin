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

import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.ApplicationRepository;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationEvent;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemindingNotifierTest {
    private final Application application1 = Application.create(ApplicationId.of("id-1"))
                                                        .register(Registration.create("App", "http://health").build())
                                                        .withStatusInfo(StatusInfo.ofDown());
    private final Application application2 = Application.create(ApplicationId.of("id-2"))
                                                        .register(Registration.create("App", "http://health").build())
                                                        .withStatusInfo(StatusInfo.ofDown());
    private final ClientApplicationEvent appDown = new ClientApplicationStatusChangedEvent(application1.getId(),
            application1.getVersion(), StatusInfo.ofDown());
    private final ClientApplicationEvent appUp = new ClientApplicationStatusChangedEvent(application1.getId(),
            application1.getVersion(), StatusInfo.ofUp());
    private final ClientApplicationEvent otherAppUp = new ClientApplicationStatusChangedEvent(application2.getId(), 0L,
            StatusInfo.ofUp());
    private ApplicationRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = mock(ApplicationRepository.class);
        when(repository.find(application1.getId())).thenReturn(Mono.just(application1));
        when(repository.find(application2.getId())).thenReturn(Mono.just(application2));
    }

    @Test
    public void test_ctor_assert() {
        assertThatThrownBy(() -> new CompositeNotifier(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void test_remind() throws Exception {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier, repository);
        reminder.setReminderPeriod(-1000L);

        StepVerifier.create(reminder.notify(appDown)).verifyComplete();
        StepVerifier.create(reminder.notify(otherAppUp)).verifyComplete();
        reminder.sendReminders();
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(appDown, otherAppUp, appDown, appDown);

    }

    @Test
    public void test_no_remind_after_up() throws Exception {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier, repository);
        reminder.setReminderPeriod(0L);

        StepVerifier.create(reminder.notify(appDown)).verifyComplete();
        StepVerifier.create(reminder.notify(appUp)).verifyComplete();
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(appDown, appUp);
    }

    @Test
    public void test_no_remind_before_end() throws Exception {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier, repository);
        reminder.setReminderPeriod(Long.MAX_VALUE);

        StepVerifier.create(reminder.notify(appDown)).verifyComplete();
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(appDown);
    }

}
