package de.codecentric.boot.admin.notify.filter;

import static org.assertj.core.api.Assertions.assertThat;

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
		assertThat(filter.filter(fooEvent)).isTrue();

		ClientApplicationRegisteredEvent barEvent = new ClientApplicationRegisteredEvent(
				Application.create("bar").withHealthUrl("http://health").build());
		assertThat(filter.filter(barEvent)).isFalse();
	}

	@Test
	public void test_expiry() throws InterruptedException {
		ExpiringNotificationFilter filterForever = new ApplicationNameNotificationFilter("foo",
				-1L);
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
