package de.codecentric.boot.admin.notify;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

/**
 * @author Jamie Brown
 */
public class HipchatNotifierTest {
	private HipchatNotifier notifier;
	private RestTemplate restTemplate;

	@Before
	public void setUp() {
		restTemplate = mock(RestTemplate.class);
		notifier = new HipchatNotifier();
		notifier.setNotify(true);
		notifier.setAuthToken("--token-");
		notifier.setRoomId("-room-");
		notifier.setUrl(URI.create("http://localhost/v2"));
		notifier.setRestTemplate(restTemplate);
	}

	@Test
	public void test_onApplicationEvent_resolve() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();

		notifier.notify(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				infoDown, infoUp));

		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("color", "green");
		expected.put("message", "<strong>App</strong>/-id- is <strong>UP</strong>");
		expected.put("notify", Boolean.TRUE);
		expected.put("message_format", "html");

		verify(restTemplate).postForEntity(any(String.class), eq(expected), eq(Void.class));
	}

	@Test
	public void test_onApplicationEvent_trigger() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();

		notifier.notify(new ClientApplicationStatusChangedEvent(
				Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
				infoUp, infoDown));

		Map<String, Object> expected = new HashMap<String, Object>();
		expected.put("color", "red");
		expected.put("message", "<strong>App</strong>/-id- is <strong>DOWN</strong>");
		expected.put("notify", Boolean.TRUE);
		expected.put("message_format", "html");

		verify(restTemplate).postForEntity(any(String.class), eq(expected), eq(Void.class));
	}
}
