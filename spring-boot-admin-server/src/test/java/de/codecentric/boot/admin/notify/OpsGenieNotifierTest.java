package de.codecentric.boot.admin.notify;

import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OpsGenieNotifierTest {
    private OpsGenieNotifier notifier;
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        notifier = new OpsGenieNotifier();
        notifier.setApiKey("--service--");
        notifier.setRecipients("--recipients--");
        notifier.setRestTemplate(restTemplate);
    }

    @Test
    public void test_onApplicationEvent_resolve() {
        StatusInfo infoDown = StatusInfo.ofDown();
        StatusInfo infoUp = StatusInfo.ofUp();
        ClientApplicationStatusChangedEvent event = getEvent(infoDown, infoUp);

        notifier.notify(event);

        verify(restTemplate).exchange(eq("https://api.opsgenie.com/v1/json/alert/close"), eq(HttpMethod.POST),
                eq(expectedRequest(event)), eq(Void.class));
    }

    @Test
    public void test_onApplicationEvent_trigger() {
        StatusInfo infoDown = StatusInfo.ofDown();
        StatusInfo infoUp = StatusInfo.ofUp();
        ClientApplicationStatusChangedEvent event = getEvent(infoUp, infoDown);

        notifier.notify(event);

        verify(restTemplate).exchange(eq("https://api.opsgenie.com/v1/json/alert"), eq(HttpMethod.POST),
                eq(expectedRequest(event)), eq(Void.class));
    }


    private ClientApplicationStatusChangedEvent getEvent(StatusInfo from, StatusInfo to) {
        return new ClientApplicationStatusChangedEvent(
                Application.create("App").withId("-id-").withHealthUrl("http://health").build(), from, to);
    }

    private String getMessage(ClientApplicationEvent event) {
        return String.format("%s/%s is %s", event.getApplication().getName(), event.getApplication().getId(),
                ((ClientApplicationStatusChangedEvent) event).getTo().getStatus());
    }

    private String getDescription(ClientApplicationEvent event) {
        return String.format("Application %s (%s) went from %s to %s", event.getApplication().getName(),
                event.getApplication().getId(), ((ClientApplicationStatusChangedEvent) event).getFrom().getStatus(),
                ((ClientApplicationStatusChangedEvent) event).getTo().getStatus());
    }

    private HttpEntity expectedRequest(ClientApplicationEvent event) {
        Map<String, Object> expected = new HashMap<>();
        expected.put("apiKey", "--service--");
        expected.put("message", getMessage(event));
        expected.put("alias", event.getApplication().getName() + "/" + event.getApplication().getId());
        expected.put("description", getDescription(event));

        if (event instanceof ClientApplicationStatusChangedEvent &&
            !"UP".equals(((ClientApplicationStatusChangedEvent) event).getTo().getStatus())) {

            expected.put("recipients", "--recipients--");
            Map<String, Object> details = new HashMap<>();
            details.put("type", "link");
            details.put("href", event.getApplication().getHealthUrl());
            details.put("text", "Application health-endpoint");
            expected.put("details", details);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(expected, headers);
    }
}
