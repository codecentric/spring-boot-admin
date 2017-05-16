package de.codecentric.boot.admin.notify;

import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by fernando on 5/16/17.
 */
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

        notifier.notify(new ClientApplicationStatusChangedEvent(
                Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
                infoDown, infoUp));

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("apiKey", "--service--");
        expected.put("alias", "App/-id-");
        expected.put("description","Application App (-id-) went from DOWN to UP");
        expected.put("message", "App/-id- is UP");

        String url = String.format("%s/close", OpsGenieNotifier.DEFAULT_URI);
        verify(restTemplate).postForEntity(eq(url), eq(expected),
                eq(Void.class));
    }

    @Test
    public void test_onApplicationEvent_trigger() {
        StatusInfo infoDown = StatusInfo.ofDown();
        StatusInfo infoUp = StatusInfo.ofUp();

        notifier.notify(new ClientApplicationStatusChangedEvent(
                Application.create("App").withId("-id-").withHealthUrl("http://health").build(),
                infoUp, infoDown));

        Map<String, Object> expected = new HashMap<String, Object>();
        expected.put("apiKey", "--service--");
        expected.put("recipients","--recipients--");
        expected.put("alias", "App/-id-");
        expected.put("description","Application App (-id-) went from UP to DOWN");
        expected.put("message", "App/-id- is DOWN");

        Map<String, Object> details = new HashMap<String, Object>();
        details.put("type","link");
        details.put("href", "http://health");
        details.put("text", "Application health-endpoint");
        expected.put("details", details);

        verify(restTemplate).postForEntity(eq(OpsGenieNotifier.DEFAULT_URI.toString()), eq(expected),
                eq(Void.class));
    }

}
