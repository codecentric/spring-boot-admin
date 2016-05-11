package de.codecentric.boot.admin.notify.filter;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.model.Application;

public class ApplicationIdNotificationFilterTest {

	@Test
	public void test_filterByName() {
		NotificationFilter filter = new ApplicationIdNotificationFilter("cafebabe", -1L);

		ClientApplicationRegisteredEvent fooEvent = new ClientApplicationRegisteredEvent(
				Application.create("foo").withId("cafebabe").withHealthUrl("http://health")
						.build());
		assertThat(filter.filter(fooEvent), is(true));

		ClientApplicationRegisteredEvent barEvent = new ClientApplicationRegisteredEvent(
				Application.create("foo").withId("1337").withHealthUrl("http://health").build());
		assertThat(filter.filter(barEvent), is(false));
	}
}
