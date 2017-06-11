package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.event.ClientApplicationEvent;
import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.StatusInfo;

import java.util.Arrays;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CompositeNotifierTest {

    private static final ClientApplicationEvent APP_DOWN = new ClientApplicationStatusChangedEvent(
            Application.create("App")
                       .withId("id-1")
                       .withHealthUrl("http://health")
                       .withStatusInfo(StatusInfo.ofDown())
                       .build(), StatusInfo.ofUp(), StatusInfo.ofDown());

    @Test(expected = IllegalArgumentException.class)
    public void test_ctor_assert() {
        new CompositeNotifier(null);
    }

    @Test
    public void test_all_notifiers_get_notified() throws Exception {
        TestNotifier notifier1 = new TestNotifier();
        TestNotifier notifier2 = new TestNotifier();
        CompositeNotifier compositeNotifier = new CompositeNotifier(Arrays.<Notifier>asList(notifier1, notifier2));

        compositeNotifier.notify(APP_DOWN);

        assertThat(notifier1.getEvents()).containsOnly(APP_DOWN);
        assertThat(notifier2.getEvents()).containsOnly(APP_DOWN);
    }
}
