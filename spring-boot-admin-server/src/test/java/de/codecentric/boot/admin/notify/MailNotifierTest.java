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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class MailNotifierTest {

	private MailSender sender;
	private MailNotifier notifier;

	@Before
	public void setup() {
		sender = mock(MailSender.class);

		notifier = new MailNotifier(sender);
		notifier.setTo(new String[] { "foo@bar.com" });
		notifier.setCc(new String[] { "bar@foo.com" });
		notifier.setFrom("SBA <no-reply@example.com>");
		notifier.setSubject("#{application.id} is #{to.status}");
	}

	@Test
	public void test_onApplicationEvent() {
		notifier.notify(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofDown(), StatusInfo.ofUp()));

		SimpleMailMessage expected = new SimpleMailMessage();
		expected.setTo(new String[] { "foo@bar.com" });
		expected.setCc(new String[] { "bar@foo.com" });
		expected.setFrom("SBA <no-reply@example.com>");
		expected.setText("App (-id-)\nstatus changed from DOWN to UP\n\nhttp://health");
		expected.setSubject("-id- is UP");

		verify(sender).send(eq(expected));
	}

	// The following tests are rather for AbstractNotifier

	@Test
	public void test_onApplicationEvent_disbaled() {
		notifier.setEnabled(false);
		notifier.notify(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofDown(), StatusInfo.ofUp()));

		verifyNoMoreInteractions(sender);
	}

	@Test
	public void test_onApplicationEvent_noSend() {
		notifier.notify(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofUnknown(), StatusInfo.ofUp()));

		verifyNoMoreInteractions(sender);
	}

	@Test
	public void test_onApplicationEvent_noSend_wildcard() {
		notifier.setIgnoreChanges(new String[] { "*:UP" });
		notifier.notify(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofOffline(), StatusInfo.ofUp()));

		verifyNoMoreInteractions(sender);
	}

	@Test
	public void test_onApplicationEvent_throw_doesnt_propagate() {
		Notifier notifier = new AbstractStatusChangeNotifier() {
			@Override
			protected void doNotify(ClientApplicationEvent event) throws Exception {
				throw new IllegalStateException("test");
			}
		};
		notifier.notify(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofOffline(), StatusInfo.ofUp()));
	}
}
