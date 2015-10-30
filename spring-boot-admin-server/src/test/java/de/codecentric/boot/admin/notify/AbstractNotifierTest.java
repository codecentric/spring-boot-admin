package de.codecentric.boot.admin.notify;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class AbstractNotifierTest {
	private TestableNotifier notifier = new TestableNotifier();

	@Test
	public void test_onApplicationEvent() {
		notifier.onClientApplicationStatusChanged(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofDown(), StatusInfo.ofUp()));
		assertTrue(notifier.hasNotified);
	}

	@Test
	public void test_onApplicationEvent_disbaled() {
		notifier.setEnabled(false);
		notifier.onClientApplicationStatusChanged(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofDown(), StatusInfo.ofUp()));
		assertFalse(notifier.hasNotified);
	}

	@Test
	public void test_onApplicationEvent_noSend() {
		notifier.onClientApplicationStatusChanged(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofUnknown(), StatusInfo.ofUp()));

		assertFalse(notifier.hasNotified);
	}

	@Test
	public void test_onApplicationEvent_noSend_wildcard() {
		notifier.setIgnoreChanges(new String[] { "*:UP" });
		notifier.onClientApplicationStatusChanged(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofOffline(), StatusInfo.ofUp()));

		assertFalse(notifier.hasNotified);
	}

	private static class TestableNotifier extends AbstractNotifier {
		private boolean hasNotified;

		@Override
		protected void notify(ClientApplicationStatusChangedEvent event) throws Exception {
			hasNotified = true;
		}
	}
}
