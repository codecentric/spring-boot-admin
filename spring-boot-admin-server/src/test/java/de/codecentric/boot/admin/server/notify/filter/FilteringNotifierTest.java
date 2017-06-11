package de.codecentric.boot.admin.server.notify.filter;

import de.codecentric.boot.admin.server.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.notify.TestNotifier;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FilteringNotifierTest {
    private static final ClientApplicationRegisteredEvent EVENT = new ClientApplicationRegisteredEvent(
            Application.create(ApplicationId.of("-"), Registration.create("foo", "http://health").build()).build());

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

        assertThat(notifier.getNotificationFilters()).containsKey(id1).containsKey(id2);

        notifier.notify(EVENT);

        assertThat(notifier.getNotificationFilters()).doesNotContainKey(id1).containsKey(id2);

        notifier.removeFilter(id2);
        assertThat(notifier.getNotificationFilters()).doesNotContainKey(id2);
    }

    @Test
    public void test_filter() {
        TestNotifier delegate = new TestNotifier();
        FilteringNotifier notifier = new FilteringNotifier(delegate);

        String idTrue = notifier.addFilter(event -> true);

        notifier.notify(EVENT);

        assertThat(delegate.getEvents()).doesNotContain(EVENT);

        notifier.removeFilter(idTrue);
        notifier.notify(EVENT);

        assertThat(delegate.getEvents()).contains(EVENT);
    }
}
