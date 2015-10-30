package de.codecentric.boot.admin.notify;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

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
	}

	@Test
	public void test_onApplicationEvent() {
		notifier.onClientApplicationStatusChanged(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				StatusInfo.ofDown(), StatusInfo.ofUp()));

		SimpleMailMessage expected = new SimpleMailMessage();
		expected.setTo(new String[] { "foo@bar.com" });
		expected.setCc(new String[] { "bar@foo.com" });
		expected.setFrom("SBA <no-reply@example.com>");
		expected.setText("App (-id-)\nstatus changed from DOWN to UP\n\nhttp://health");
		expected.setSubject("App (-id-) is UP");

		verify(sender).send(eq(expected));
	}

}
