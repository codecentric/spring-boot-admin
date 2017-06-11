package de.codecentric.boot.admin.server.notify.filter;

import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.model.Application;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationIdNotificationFilterTest {

    @Test
    public void test_filterByName() {
        NotificationFilter filter = new ApplicationIdNotificationFilter("cafebabe", -1L);

        ClientApplicationRegisteredEvent fooEvent = new ClientApplicationRegisteredEvent(
                Application.create("foo").withId("cafebabe").withHealthUrl("http://health").build());
        assertThat(filter.filter(fooEvent)).isTrue();

        ClientApplicationRegisteredEvent barEvent = new ClientApplicationRegisteredEvent(
                Application.create("foo").withId("1337").withHealthUrl("http://health").build());
        assertThat(filter.filter(barEvent)).isFalse();
    }
}
