package de.codecentric.boot.admin.notify;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.notify.MicrosoftTeamsNotifier.Message;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MicrosoftTeamsNotifierTest {
	private static final String appName = "Test App";
	private static final String appId = "TestAppId";
	private static final String healthUrl = "http://health";
	private static final String managementUrl = "http://management";
	private static final String serviceUrl = "http://service";

	private MicrosoftTeamsNotifier notifier;
	private RestTemplate mockRestTemplate;
	private Application application;

	@Before
	public void setUp() throws Exception {
		mockRestTemplate = mock(RestTemplate.class);

		notifier = new MicrosoftTeamsNotifier();
		notifier.setRestTemplate(mockRestTemplate);
		notifier.setWebhookUrl(URI.create("http://example.com"));

		application = Application.create(appName).withId(appId).withHealthUrl(healthUrl)
				.withManagementUrl(managementUrl).withServiceUrl(serviceUrl).build();
	}

	@Test
	public void test_onClientApplicationDeRegisteredEvent_resolve() throws Exception {
		ClientApplicationDeregisteredEvent event = new ClientApplicationDeregisteredEvent(
				application);

		notifier.doNotify(event);

		verify(mockRestTemplate).postForEntity(eq(URI.create("http://example.com")),
				any(Message.class), eq(Void.class));
	}

	@Test
	public void test_onApplicationRegisteredEvent_resolve() throws Exception {
		ClientApplicationRegisteredEvent event = new ClientApplicationRegisteredEvent(application);

		notifier.doNotify(event);

		verify(mockRestTemplate).postForEntity(eq(URI.create("http://example.com")),
				any(Message.class), eq(Void.class));
	}

	@Test
	public void test_onApplicationStatusChangedEvent_resolve() throws Exception {
		ClientApplicationStatusChangedEvent event = new ClientApplicationStatusChangedEvent(
				application, StatusInfo.ofDown(), StatusInfo.ofUp());

		notifier.doNotify(event);

		verify(mockRestTemplate).postForEntity(eq(URI.create("http://example.com")),
				any(Message.class), eq(Void.class));
	}

	@Test
	public void test_shouldNotifyWithStatusChangeEventReturns_true() {
		ClientApplicationStatusChangedEvent event = new ClientApplicationStatusChangedEvent(
				application, StatusInfo.ofDown(), StatusInfo.ofOffline());

		boolean shouldNotify = notifier.shouldNotify(event);

		assertTrue("Returned false when should notify for status change", shouldNotify);
	}

	@Test
	public void test_shouldNotifyWithRegisteredEventReturns_true() {
		ClientApplicationRegisteredEvent event = new ClientApplicationRegisteredEvent(application);

		boolean shouldNotify = notifier.shouldNotify(event);

		assertTrue("Returned false when should notify for register", shouldNotify);
	}

	@Test
	public void test_shouldNotifyWithDeRegisteredEventReturns_true() {
		ClientApplicationDeregisteredEvent event = new ClientApplicationDeregisteredEvent(
				application);

		boolean shouldNotify = notifier.shouldNotify(event);

		assertTrue("Returned false when should notify for de-register", shouldNotify);
	}

	@Test
	public void test_getDeregisteredMessageForAppReturns_correctContent() {
		ClientApplicationDeregisteredEvent event = new ClientApplicationDeregisteredEvent(
				application);
		
		notifier.getDeregisteredMessage(event);

		assertMessage(notifier.getMessage(), notifier.getDeRegisteredTitle(), notifier.getMessageSummary(),
				String.format(notifier.getDeregisterActivitySubtitlePattern(),
						application.getName(), application.getId()));
	}

	@Test
	public void test_getRegisteredMessageForAppReturns_correctContent() {
		ClientApplicationRegisteredEvent event = new ClientApplicationRegisteredEvent(
				application);
		
		notifier.getRegisteredMessage(event);

		assertMessage(notifier.getMessage(), notifier.getRegisteredTitle(), notifier.getMessageSummary(),
				String.format(notifier.getRegisterActivitySubtitlePattern(), application.getName(),
						application.getId()));
	}

	@Test
	public void test_getStatusChangedMessageForAppReturns_correctContent() {
		ClientApplicationStatusChangedEvent event = new ClientApplicationStatusChangedEvent(application,
				StatusInfo.ofUp(), StatusInfo.ofDown());
		
		notifier.getStatusChangedMessage(event);

		assertMessage(notifier.getMessage(), notifier.getStatusChangedTitle(), notifier.getMessageSummary(),
				String.format(notifier.getStatusActivitySubtitlePattern(), application.getName(),
						application.getId(), StatusInfo.ofUp().getStatus(),
						StatusInfo.ofDown().getStatus()));
	}

	@Test
	public void test_getStatusChangedMessageWithMissingFormatArgumentReturns_activitySubtitlePattern() {
		ClientApplicationStatusChangedEvent event = new ClientApplicationStatusChangedEvent(application,
				StatusInfo.ofUp(), StatusInfo.ofDown());
		String pattern = "STATUS_%s_ACTIVITY%s_PATTERN%s%s%s%s";
		notifier.setStatusActivitySubtitlePattern(pattern);
		
		notifier.getStatusChangedMessage(event);

		assertEquals("Activity Subtitle doesn't match", pattern,
				notifier.getMessage().getSections().get(0).getActivitySubtitle());
	}

	@Test
	public void test_getStatusChangedMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
		ClientApplicationStatusChangedEvent event = new ClientApplicationStatusChangedEvent(application,
				StatusInfo.ofUp(), StatusInfo.ofDown());
		notifier.setStatusActivitySubtitlePattern("STATUS_ACTIVITY_PATTERN_%s");
		notifier.getStatusChangedMessage(event);

		assertEquals("Activity Subtitle doesn't match", "STATUS_ACTIVITY_PATTERN_" + appName,
				notifier.getMessage().getSections().get(0).getActivitySubtitle());
	}

	@Test
	public void test_getRegisterMessageWithMissingFormatArgumentReturns_activitySubtitlePattern() {
		ClientApplicationRegisteredEvent event = new ClientApplicationRegisteredEvent(
				application);
		String pattern = "REGISTER_%s_ACTIVITY%s_PATTERN%s%s%s%s";
		notifier.setRegisterActivitySubtitlePattern(pattern);
		notifier.getRegisteredMessage(event);

		assertEquals("Activity Subtitle doesn't match", pattern,
				notifier.getMessage().getSections().get(0).getActivitySubtitle());
	}

	@Test
	public void test_getRegisterMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
		ClientApplicationRegisteredEvent event = new ClientApplicationRegisteredEvent(
				application);
		notifier.setRegisterActivitySubtitlePattern("REGISTER_ACTIVITY_PATTERN_%s");
		notifier.getRegisteredMessage(event);

		assertEquals("Activity Subtitle doesn't match", "REGISTER_ACTIVITY_PATTERN_" + appName,
				notifier.getMessage().getSections().get(0).getActivitySubtitle());
	}

	@Test
	public void test_getDeRegisterMessageWithMissingFormatArgumentReturns_activitySubtitlePattern() {
		ClientApplicationDeregisteredEvent event = new ClientApplicationDeregisteredEvent(
				application);
		String pattern = "DEREGISTER_%s_ACTIVITY%s_PATTERN%s%s%s%s";
		notifier.setDeregisterActivitySubtitlePattern(pattern);
		notifier.getDeregisteredMessage(event);

		assertEquals("Activity Subtitle doesn't match", pattern,
				notifier.getMessage().getSections().get(0).getActivitySubtitle());
	}

	@Test
	public void test_getDeRegisterMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
		ClientApplicationDeregisteredEvent event = new ClientApplicationDeregisteredEvent(
				application);
		notifier.setDeregisterActivitySubtitlePattern("DEREGISTER_ACTIVITY_PATTERN_%s");
		notifier.getDeregisteredMessage(event);

		assertEquals("Activity Subtitle doesn't match", "DEREGISTER_ACTIVITY_PATTERN_" + appName,
				notifier.getMessage().getSections().get(0).getActivitySubtitle());
	}

	private void assertMessage(Message message, String expectedTitle, String expectedSummary,
			String expectedSubTitle) {
		assertEquals("Title doesn't match", expectedTitle, message.getTitle());
		assertEquals("Summary doesn't match", expectedSummary, message.getSummary());

		assertEquals("Incorrect number of sections", 1, message.getSections().size());

		MicrosoftTeamsNotifier.Section section = message.getSections().get(0);
		assertEquals("Activity Title doesn't match", application.getName(),
				section.getActivityTitle());

		assertEquals("Activity Subtitle doesn't match", expectedSubTitle,
				section.getActivitySubtitle());

		assertEquals("Theme Color doesn't match", "6db33f", message.getThemeColor());

		List<MicrosoftTeamsNotifier.Fact> facts = section.getFacts();
		assertEquals("Wrong number of facts", 5, facts.size());
		assertEquals("Status", facts.get(0).getName());
		assertEquals("UNKNOWN", facts.get(0).getValue());
		assertEquals("Service URL", facts.get(1).getName());
		assertEquals(serviceUrl, facts.get(1).getValue());
		assertEquals("Health URL", facts.get(2).getName());
		assertEquals(healthUrl, facts.get(2).getValue());
		assertEquals("Management URL", facts.get(3).getName());
		assertEquals(managementUrl, facts.get(3).getValue());
		assertEquals("Source", facts.get(4).getName());
		assertEquals(null, facts.get(4).getValue());
	}
}
