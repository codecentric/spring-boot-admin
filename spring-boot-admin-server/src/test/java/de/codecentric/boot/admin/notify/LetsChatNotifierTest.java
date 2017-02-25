package de.codecentric.boot.admin.notify;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

public class LetsChatNotifierTest {
	private static final String room = "text_room";
	private static final String token = "text_token";
	private static final String user = "api_user";
	private static final String appName = "App";
	private static final String id = "-id-";
	private static final String host = "http://localhost";

	private LetsChatNotifier notifier;
	private RestTemplate restTemplate;

	@Before
	public void setUp() {
		restTemplate = mock(RestTemplate.class);
		notifier = new LetsChatNotifier();
		notifier.setUsername(user);
		notifier.setUrl(URI.create(host));
		notifier.setRoom(room);
		notifier.setToken(token);
		notifier.setRestTemplate(restTemplate);
	}

	@Test
	public void test_onApplicationEvent_resolve() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();

		notifier.notify(getEvent(infoDown, infoUp));

		HttpEntity<?> expected = expectedMessage(standardMessage(infoUp.getStatus(), appName, id));
		verify(restTemplate).exchange(eq(URI.create(String.format("%s/rooms/%s/messages", host, room))), eq(HttpMethod.POST), eq(expected), eq(Void.class));
	}

	@Test
	public void test_onApplicationEvent_resolve_with_custom_message() {
		StatusInfo infoDown = StatusInfo.ofDown();
		StatusInfo infoUp = StatusInfo.ofUp();

		notifier.setMessage("TEST");
		notifier.notify(getEvent(infoDown, infoUp));

		HttpEntity<?> expected = expectedMessage("TEST");
		verify(restTemplate).exchange(eq(URI.create(String.format("%s/rooms/%s/messages", host, room))), eq(HttpMethod.POST), eq(expected), eq(Void.class));
	}

	private ClientApplicationStatusChangedEvent getEvent(StatusInfo infoDown, StatusInfo infoUp) {
		return new ClientApplicationStatusChangedEvent(
				Application.create(appName).withId(id).withHealthUrl("http://health").build(),
				infoDown, infoUp);
	}

	private HttpEntity<?> expectedMessage(String message) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		String auth = Base64Utils.encodeToString(String.format("%s:%s", token, user).getBytes());
		httpHeaders.add(HttpHeaders.AUTHORIZATION, String.format("Basic %s", auth));
		Map<String, Object> messageJson = new HashMap<>();
		messageJson.put("text", message);
		return new HttpEntity<>(messageJson, httpHeaders);
	}

	private String standardMessage(String status, String appName, String id) {
		return "*" + appName + "* (" + id + ") is *" + status + "*";
	}

}
