package de.codecentric.boot.admin.notify.filter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;

public class ApplicationNameNotificationFilterTest {

	@Test
	public void test_filterByName() {
		NotificationFilter filter = new ApplicationNameNotificationFilter("foo", -1L);

		ClientApplicationRegisteredEvent fooEvent = new ClientApplicationRegisteredEvent(
				Application.create("foo").withHealthUrl("http://health").build());
		assertThat(filter.filter(fooEvent), is(true));

		ClientApplicationRegisteredEvent barEvent = new ClientApplicationRegisteredEvent(
				Application.create("bar").withHealthUrl("http://health").build());
		assertThat(filter.filter(barEvent), is(false));
	}

	@Test
	public void test_expiry() throws InterruptedException {
		ExpiringNotificationFilter filterForever = new ApplicationNameNotificationFilter("foo",
				-1L);
		ExpiringNotificationFilter filterExpired = new ApplicationNameNotificationFilter("foo", 0L);
		ExpiringNotificationFilter filterLong = new ApplicationNameNotificationFilter("foo",
				System.currentTimeMillis() + 500L);

		assertThat(filterForever.isExpired(), is(false));
		assertThat(filterLong.isExpired(), is(false));
		assertThat(filterExpired.isExpired(), is(true));

		TimeUnit.MILLISECONDS.sleep(501);
		assertThat(filterLong.isExpired(), is(true));
	}
}
