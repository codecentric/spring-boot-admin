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

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.model.StatusInfo;
import de.codecentric.boot.admin.server.registry.store.ApplicationStore;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemindingNotifierTest {
    private final Application application1 = Application.create(ApplicationId.of("id-1"),
            Registration.create("App", "http://health").build()).statusInfo(StatusInfo.ofDown()).build();
    private final ClientApplicationEvent appDown = new ClientApplicationStatusChangedEvent(application1.getId(),
            StatusInfo.ofDown());
    private final ClientApplicationEvent appUp = new ClientApplicationStatusChangedEvent(application1.getId(),
            StatusInfo.ofUp());
    private final ClientApplicationEvent otherAppUp = new ClientApplicationStatusChangedEvent(ApplicationId.of("id-2"),
            StatusInfo.ofUp());
    private ApplicationStore store;

    @Before
    public void setUp() throws Exception {
        store = mock(ApplicationStore.class);
        when(store.find(application1.getId())).thenReturn(application1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_ctor_assert() {
        new CompositeNotifier(null);
    }

    @Test
    public void test_remind() throws Exception {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier, store);
        reminder.setReminderPeriod(-1000L);

        reminder.notify(appDown);
        reminder.notify(otherAppUp);
        reminder.sendReminders();
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(appDown, otherAppUp, appDown, appDown);

    }

    @Test
    public void test_no_remind_after_up() throws Exception {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier, store);
        reminder.setReminderPeriod(0L);

        reminder.notify(appDown);
        reminder.notify(appUp);
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(appDown, appUp);
    }

    @Test
    public void test_no_remind_before_end() throws Exception {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier, store);
        reminder.setReminderPeriod(Long.MAX_VALUE);

        reminder.notify(appDown);
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(appDown);
    }

}
