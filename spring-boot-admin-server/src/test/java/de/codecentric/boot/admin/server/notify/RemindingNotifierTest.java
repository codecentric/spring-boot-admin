package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.model.StatusInfo;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RemindingNotifierTest {

    private static final ClientApplicationEvent APP_DOWN = new ClientApplicationStatusChangedEvent(
            Application.create(ApplicationId.of("id-1"), Registration.create("App", "http://health").build())
                       .statusInfo(StatusInfo.ofDown())
                       .build(), StatusInfo.ofUp(), StatusInfo.ofDown());

    private static final ClientApplicationEvent APP_UP = new ClientApplicationStatusChangedEvent(
            Application.create(ApplicationId.of("id-1"), Registration.create("App", "http://health").build())
                       .statusInfo(StatusInfo.ofUp())
                       .build(), StatusInfo.ofDown(), StatusInfo.ofUp());

    private static final ClientApplicationEvent OTHER_APP_UP = new ClientApplicationStatusChangedEvent(
            Application.create(ApplicationId.of("id-2"), Registration.create("App2", "http://health").build())
                       .statusInfo(StatusInfo.ofUp())
                       .build(), StatusInfo.ofDown(), StatusInfo.ofUp());

    @Test(expected = IllegalArgumentException.class)
    public void test_ctor_assert() {
        new CompositeNotifier(null);
    }

    @Test
    public void test_remind() throws Exception {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier);
        reminder.setReminderPeriod(-1000L);

        reminder.notify(APP_DOWN);
        reminder.notify(OTHER_APP_UP);
        reminder.sendReminders();
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(APP_DOWN, OTHER_APP_UP, APP_DOWN, APP_DOWN);

    }

    @Test
    public void test_no_remind_after_up() throws Exception {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier);
        reminder.setReminderPeriod(0L);

        reminder.notify(APP_DOWN);
        reminder.notify(APP_UP);
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(APP_DOWN, APP_UP);
    }

    @Test
    public void test_no_remind_before_end() throws Exception {
        TestNotifier notifier = new TestNotifier();
        RemindingNotifier reminder = new RemindingNotifier(notifier);
        reminder.setReminderPeriod(Long.MAX_VALUE);

        reminder.notify(APP_DOWN);
        reminder.sendReminders();

        assertThat(notifier.getEvents()).containsOnly(APP_DOWN);
    }

}
