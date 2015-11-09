/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codecentric.boot.admin.notify;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class AbstractNotifierTest {

	@Test
	public void test_onApplicationEvent() {
		TestableNotifier notifier = new TestableNotifier();
		notifier.onClientApplicationStatusChanged(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofDown(), StatusInfo.ofUp()));
		assertTrue(notifier.hasNotified);
	}

	@Test
	public void test_onApplicationEvent_disbaled() {
		TestableNotifier notifier = new TestableNotifier();
		notifier.setEnabled(false);
		notifier.onClientApplicationStatusChanged(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofDown(), StatusInfo.ofUp()));
		assertFalse(notifier.hasNotified);
	}

	@Test
	public void test_onApplicationEvent_noSend() {
		TestableNotifier notifier = new TestableNotifier();
		notifier.onClientApplicationStatusChanged(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofUnknown(), StatusInfo.ofUp()));

		assertFalse(notifier.hasNotified);
	}

	@Test
	public void test_onApplicationEvent_noSend_wildcard() {
		TestableNotifier notifier = new TestableNotifier();
		notifier.setIgnoreChanges(new String[] { "*:UP" });
		notifier.onClientApplicationStatusChanged(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofOffline(), StatusInfo.ofUp()));

		assertFalse(notifier.hasNotified);
	}

	@Test
	public void test_onApplicationEvent_throw_doesnt_propagate() {
		AbstractNotifier notifier = new AbstractNotifier() {
			@Override
			protected void notify(ClientApplicationStatusChangedEvent event) throws Exception {
				throw new RuntimeException();
			}
		};
		notifier.onClientApplicationStatusChanged(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofOffline(), StatusInfo.ofUp()));
	}

	private static class TestableNotifier extends AbstractNotifier {
		private boolean hasNotified;

		@Override
		protected void notify(ClientApplicationStatusChangedEvent event) throws Exception {
			hasNotified = true;
		}
	}
}
