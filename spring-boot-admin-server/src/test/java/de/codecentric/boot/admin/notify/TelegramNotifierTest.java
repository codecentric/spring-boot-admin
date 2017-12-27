package de.codecentric.boot.admin.notify;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class TelegramNotifierTest {
	private TelegramNotifier notifier;
	private RestTemplate restTemplate;

	@Before
	public void setUp() {
		restTemplate = mock(RestTemplate.class);
		notifier = new TelegramNotifier();
		notifier.setDisableNotify(false);
		notifier.setAuthToken("--token-");
		notifier.setChatId("-room-");
		notifier.setParse_mode("HTML");
		notifier.setApiUrl("https://telegram.com");
		notifier.setRestTemplate(restTemplate);
	}

	@Test
	public void test_onApplicationEvent_resolve() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();
		notifier.notify(getEvent(infoDown, infoUp));
		verify(restTemplate).getForObject(
				eq("https://telegram.com/bot--token-/sendmessage?chat_id={chat_id}&text={text}"
						+ "&parse_mode={parse_mode}&disable_notification={disable_notification}"),
				eq(Void.class), eq(getParameters("UP")));
	}

	@Test
	public void test_onApplicationEvent_trigger() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();
		notifier.notify(getEvent(infoUp, infoDown));
		verify(restTemplate).getForObject(
				eq("https://telegram.com/bot--token-/sendmessage?chat_id={chat_id}&text={text}"
						+ "&parse_mode={parse_mode}&disable_notification={disable_notification}"),
				eq(Void.class), eq(getParameters("DOWN")));
	}

	private ClientApplicationStatusChangedEvent getEvent(StatusInfo infoDown, StatusInfo infoUp) {
		return new ClientApplicationStatusChangedEvent(Application.create("Telegram").withId("-id-")
				.withHealthUrl("http://health").build(), infoDown, infoUp);
	}

	private Map<String, Object> getParameters(String status) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("chat_id", "-room-");
		parameters.put("text", getMessage("Telegram", "-id-", status));
		parameters.put("parse_mode", "HTML");
		parameters.put("disable_notification", false);
		return parameters;
	}

	private String getMessage(String name, String id, String status) {
		return "<strong>" + name + "</strong>/" + id + " is <strong>" + status + "</strong>";
	}
}