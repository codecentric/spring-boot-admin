package de.codecentric.boot.admin.notify;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class RemindingNotifierTest {

	private static final ClientApplicationEvent APP_DOWN = new ClientApplicationStatusChangedEvent(
			Application.create("App").withId("id-1").withHealthUrl("http://health")
					.withStatusInfo(StatusInfo.ofDown()).build(),
			StatusInfo.ofUp(), StatusInfo.ofDown());

	private static final ClientApplicationEvent APP_UP = new ClientApplicationStatusChangedEvent(
			Application.create("App").withId("id-1").withHealthUrl("http://health")
					.withStatusInfo(StatusInfo.ofUp()).build(),
			StatusInfo.ofDown(), StatusInfo.ofUp());

	private static final ClientApplicationEvent OTHER_APP_UP = new ClientApplicationStatusChangedEvent(
			Application.create("App").withId("id-2").withHealthUrl("http://health")
					.withStatusInfo(StatusInfo.ofUp()).build(),
			StatusInfo.ofDown(), StatusInfo.ofUp());

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

		assertThat(notifier.getEvents(),
				is(Arrays.asList(APP_DOWN, OTHER_APP_UP, APP_DOWN, APP_DOWN)));

	}

	@Test
	public void test_no_remind_after_up() throws Exception {
		TestNotifier notifier = new TestNotifier();
		RemindingNotifier reminder = new RemindingNotifier(notifier);
		reminder.setReminderPeriod(0L);

		reminder.notify(APP_DOWN);
		reminder.notify(APP_UP);
		reminder.sendReminders();

		assertThat(notifier.getEvents(), is(Arrays.asList(APP_DOWN, APP_UP)));
	}

	@Test
	public void test_no_remind_before_end() throws Exception {
		TestNotifier notifier = new TestNotifier();
		RemindingNotifier reminder = new RemindingNotifier(notifier);
		reminder.setReminderPeriod(Long.MAX_VALUE);

		reminder.notify(APP_DOWN);
		reminder.sendReminders();

		assertThat(notifier.getEvents(), is(Collections.singletonList(APP_DOWN)));
	}

}
