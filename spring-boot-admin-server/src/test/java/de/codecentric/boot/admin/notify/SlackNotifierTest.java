package de.codecentric.boot.admin.notify;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class SlackNotifierTest {
	private static final String channel = "channel";
	private static final String icon = "icon";
	private static final String user = "user";
	private static final String appName = "App";
	private static final String id = "-id-";
	private static final String message = "test";

	private SlackNotifier notifier;
	private RestTemplate restTemplate;

	@Before
	public void setUp() {
		restTemplate = mock(RestTemplate.class);
		notifier = new SlackNotifier();
		notifier.setUsername(user);
		notifier.setWebhookUrl(URI.create("http://localhost/"));
		notifier.setRestTemplate(restTemplate);
	}

	@Test
	public void test_onApplicationEvent_resolve() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();

		notifier.setChannel(channel);
		notifier.setIcon(icon);
		notifier.notify(getEvent(infoDown, infoUp));

		Object expected = expectedMessage("good", user, icon, channel,
				standardMessage(infoUp.getStatus(), appName, id));

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	@Test
	public void test_onApplicationEvent_resolve_without_channel_and_icon() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();

		notifier.notify(getEvent(infoDown, infoUp));

		Object expected = expectedMessage("good", user, null, null,
				standardMessage(infoUp.getStatus(), appName, id));

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	@Test
	public void test_onApplicationEvent_resolve_with_given_user() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();
		String anotherUser = "another user";

		notifier.setUsername(anotherUser);
		notifier.setChannel(channel);
		notifier.setIcon(icon);
		notifier.notify(getEvent(infoDown, infoUp));

		Object expected = expectedMessage("good", anotherUser, icon, channel,
				standardMessage(infoUp.getStatus(), appName, id));

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	@Test
	public void test_onApplicationEvent_resolve_with_given_message() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();

		notifier.setMessage(message);
		notifier.setChannel(channel);
		notifier.setIcon(icon);
		notifier.notify(getEvent(infoDown, infoUp));

		Object expected = expectedMessage("good", user, icon, channel, message);

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	@Test
	public void test_onApplicationEvent_trigger() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();

		notifier.setChannel(channel);
		notifier.setIcon(icon);
		notifier.notify(getEvent(infoUp, infoDown));

		Object expected = expectedMessage("danger", user, icon, channel,
				standardMessage(infoDown.getStatus(), appName, id));

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	private ClientApplicationStatusChangedEvent getEvent(StatusInfo infoDown, StatusInfo infoUp) {
		return new ClientApplicationStatusChangedEvent(
				Application.create(appName).withId(id).withHealthUrl("http://health").build(),
				infoDown, infoUp);
	}

	private Object expectedMessage(String color, String user, @Nullable String icon,
			@Nullable String channel, String message) {
		Map<String, Object> messageJson = new HashMap<>();
		messageJson.put("username", user);
		if (icon != null) {
			messageJson.put("icon_emoji", ":" + icon + ":");
		}
		if (channel != null) {
			messageJson.put("channel", channel);
		}

		Map<String, Object> attachments = new HashMap<>();
		attachments.put("text", message);
		attachments.put("color", color);
		attachments.put("mrkdwn_in", Collections.singletonList("text"));

		messageJson.put("attachments", Collections.singletonList(attachments));

		return messageJson;
	}

	private String standardMessage(String status, String appName, String id) {
		return "*" + appName + "* (" + id + ") is *" + status + "*";
	}
}
