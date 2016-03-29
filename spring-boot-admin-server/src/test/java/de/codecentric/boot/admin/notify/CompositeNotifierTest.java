package de.codecentric.boot.admin.notify;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class CompositeNotifierTest {

	private static final ClientApplicationEvent APP_DOWN = new ClientApplicationStatusChangedEvent(
			Application.create("App").withId("id-1").withHealthUrl("http://health")
					.withStatusInfo(StatusInfo.ofDown()).build(),
			StatusInfo.ofUp(), StatusInfo.ofDown());


	@Test
	public void test_all_notifiers_get_notified() throws Exception {
		TestNotifier notifier1 = new TestNotifier();
		TestNotifier notifier2 = new TestNotifier();
		CompositeNotifier compositeNotifier = new CompositeNotifier(Arrays.<Notifier>asList(notifier1,notifier2));

		compositeNotifier.notify(APP_DOWN);

		assertThat(notifier1.getEvents(), is(Arrays.asList(APP_DOWN)));
		assertThat(notifier2.getEvents(), is(Arrays.asList(APP_DOWN)));
	}

	private static class TestNotifier implements Notifier {
		private List<ClientApplicationEvent> events = new ArrayList<ClientApplicationEvent>();

		@Override
		public void notify(ClientApplicationEvent event) {
			this.events.add(event);
		}

		public List<ClientApplicationEvent> getEvents() {
			return events;
		}
	}
}
