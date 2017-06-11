package de.codecentric.boot.admin.server.notify.filter;

import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;

import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationNameNotificationFilterTest {

    @Test
    public void test_filterByName() {
        NotificationFilter filter = new ApplicationNameNotificationFilter("foo", -1L);

        Application filteredApplication = Application.create(ApplicationId.of("-"),
                Registration.create("foo", "http://health").build()).build();
        ClientApplicationRegisteredEvent filteredEvent = new ClientApplicationRegisteredEvent(filteredApplication);
        assertThat(filter.filter(filteredEvent)).isTrue();

        Application ignoredApplication = Application.create(ApplicationId.of("-"),
                Registration.create("bar", "http://health").build()).build();
        ClientApplicationRegisteredEvent ignoredEvent = new ClientApplicationRegisteredEvent(ignoredApplication);
        assertThat(filter.filter(ignoredEvent)).isFalse();
    }

    @Test
    public void test_expiry() throws InterruptedException {
        ExpiringNotificationFilter filterForever = new ApplicationNameNotificationFilter("foo", -1L);
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
