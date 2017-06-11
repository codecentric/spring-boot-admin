package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.StatusInfo;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SlackNotifierTest {
    private static final String channel = "channel";
    private static final String icon = "icon";
    private static final String user = "user";
    private static final String appName = "App";
    private static final ApplicationId id = ApplicationId.of("-id-");
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

        Object expected = expectedMessage("good", user, null, null, standardMessage(infoUp.getStatus(), appName, id));

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
                Application.create(appName).withId(id).withHealthUrl("http://health").build(), infoDown, infoUp);
    }

    private HttpEntity<Map<String, Object>> expectedMessage(String color,
                                                            String user,
                                                            String icon,
                                                            String channel,
                                                            String message) {
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(messageJson, headers);
    }

    private String standardMessage(String status, String appName, ApplicationId id) {
        return "*" + appName + "* (" + id + ") is *" + status + "*";
    }
}
