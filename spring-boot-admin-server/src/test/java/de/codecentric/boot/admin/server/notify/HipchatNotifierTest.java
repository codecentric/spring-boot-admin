package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.StatusInfo;

import java.net.URI;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        @SuppressWarnings("unchecked") ArgumentCaptor<HttpEntity<Map<String, Object>>> httpRequest = ArgumentCaptor.forClass(
                (Class<HttpEntity<Map<String, Object>>>) (Class<?>) HttpEntity.class);

        when(restTemplate.postForEntity(isA(String.class), httpRequest.capture(), eq(Void.class))).thenReturn(
                ResponseEntity.ok((Void) null));

        notifier.notify(new ClientApplicationStatusChangedEvent(
                Application.create("App").withId("-id-").withHealthUrl("http://health").build(), infoDown, infoUp));

        assertThat(httpRequest.getValue().getHeaders()).containsEntry("Content-Type", asList("application/json"));

        Map<String, Object> body = httpRequest.getValue().getBody();
        assertThat(body).containsEntry("color", "green");
        assertThat(body).containsEntry("message", "<strong>App</strong>/-id- is <strong>UP</strong>");
        assertThat(body).containsEntry("notify", Boolean.TRUE);
        assertThat(body).containsEntry("message_format", "html");

    }

    @Test
    public void test_onApplicationEvent_trigger() {
        StatusInfo infoDown = StatusInfo.ofDown();
        StatusInfo infoUp = StatusInfo.ofUp();

        @SuppressWarnings("unchecked") ArgumentCaptor<HttpEntity<Map<String, Object>>> httpRequest = ArgumentCaptor.forClass(
                (Class<HttpEntity<Map<String, Object>>>) (Class<?>) HttpEntity.class);

        when(restTemplate.postForEntity(isA(String.class), httpRequest.capture(), eq(Void.class))).thenReturn(
                ResponseEntity.ok((Void) null));

        notifier.notify(new ClientApplicationStatusChangedEvent(
                Application.create("App").withId("-id-").withHealthUrl("http://health").build(), infoUp, infoDown));

        assertThat(httpRequest.getValue().getHeaders()).containsEntry("Content-Type", asList("application/json"));

        Map<String, Object> body = httpRequest.getValue().getBody();
        assertThat(body).containsEntry("color", "red");
        assertThat(body).containsEntry("message", "<strong>App</strong>/-id- is <strong>DOWN</strong>");
        assertThat(body).containsEntry("notify", Boolean.TRUE);
        assertThat(body).containsEntry("message_format", "html");
    }
}
