package de.codecentric.boot.admin.notify.filter;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import org.junit.Test;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.notify.TestNotifier;

public class FilteringNotifierTest {

	private static final ClientApplicationRegisteredEvent EVENT = new ClientApplicationRegisteredEvent(
			Application.create("foo").withHealthUrl("http://health").build());

	@Test(expected = IllegalArgumentException.class)
	public void test_ctor_assert() {
		new FilteringNotifier(null);
	}

	@Test
	public void test_expired_removal() {
		FilteringNotifier notifier = new FilteringNotifier(new TestNotifier());
		notifier.setCleanupInterval(0L);

		String id1 = notifier.addFilter(new ApplicationNameNotificationFilter("foo", 0L));
		String id2 = notifier.addFilter(new ApplicationNameNotificationFilter("bar", -1L));

		assertThat(notifier.getNotificationFilters(), hasKey(id1));
		assertThat(notifier.getNotificationFilters(), hasKey(id2));

		notifier.notify(EVENT);

		assertThat(notifier.getNotificationFilters(), not(hasKey(id1)));
		assertThat(notifier.getNotificationFilters(), hasKey(id2));

		notifier.removeFilter(id2);
		assertThat(notifier.getNotificationFilters(), not(hasKey(id2)));
	}

	@Test
	public void test_filter() {
		TestNotifier delegate = new TestNotifier();
		FilteringNotifier notifier = new FilteringNotifier(delegate);

		String idTrue = notifier.addFilter(new NotificationFilter() {
			@Override
			public boolean filter(ClientApplicationEvent event) {
				return true;
			}
		});

		notifier.notify(EVENT);

		assertThat(delegate.getEvents(), not(hasItem(EVENT)));

		notifier.removeFilter(idTrue);
		notifier.notify(EVENT);

		assertThat(delegate.getEvents(), hasItem(EVENT));
	}
}
