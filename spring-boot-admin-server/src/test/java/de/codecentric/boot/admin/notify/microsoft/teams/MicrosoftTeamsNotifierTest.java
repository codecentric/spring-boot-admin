package de.codecentric.boot.admin.notify.microsoft.teams;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import de.codecentric.boot.admin.notify.microsoft.teams.Message;
import de.codecentric.boot.admin.notify.microsoft.teams.MicrosoftTeamsNotifier;
import de.codecentric.boot.admin.notify.microsoft.teams.Section;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static de.codecentric.boot.admin.notify.microsoft.teams.MicrosoftTeamsNotifier.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MicrosoftTeamsNotifierTest {


    private static final URI webhookUrl = UriBuilder.fromPath("https://webhookurl").build();
    private static final String appName = "Test App";
    private static final String appId = "TestAppId";
    private static final String healthUrl = "http://health";
    private static final String managementUrl = "http://management";
    private static final String serviceUrl = "http://service";


    private MicrosoftTeamsNotifier testMTNotifier;

    private RestTemplate mockRestTemplate;
    private static Application stubApp;

    @Before
    public void setUp() throws Exception {
        testMTNotifier = new MicrosoftTeamsNotifier();

        mockRestTemplate = mock(RestTemplate.class);
        testMTNotifier.setRestTemplate(mockRestTemplate);
        testMTNotifier.setWebhookUrl(webhookUrl);

        stubApp = Application.create(appName)
                .withId(appId)
                .withHealthUrl(healthUrl)
                .withManagementUrl(managementUrl)
                .withServiceUrl(serviceUrl).build();
    }

    @Test
    public void test_onClientApplicationDeRegisteredEvent_resolve() throws Exception{
        ClientApplicationDeregisteredEvent deregisteredEvent = new ClientApplicationDeregisteredEvent(stubApp);

        Object expected = getDeregisteredMessage(stubApp);

        testMTNotifier.doNotify(deregisteredEvent);

        verify(mockRestTemplate).postForObject(any(URI.class), eq(expected), eq(Object.class));
    }



    @Test
    public void test_onApplicationRegisteredEvent_resolve() throws Exception {
        ClientApplicationRegisteredEvent registeredEvent = new ClientApplicationRegisteredEvent(stubApp);

        Object expected = getRegisteredMessage(stubApp);

        testMTNotifier.doNotify(registeredEvent);

        verify(mockRestTemplate).postForObject(any(URI.class), eq(expected), eq(Object.class));
    }

    @Test
    public void test_onApplicationStatusChangedEventFromDownToUp_resolve() throws Exception {
        ClientApplicationStatusChangedEvent statusChangedEvent = new ClientApplicationStatusChangedEvent(stubApp,
                StatusInfo.ofDown(), StatusInfo.ofUp());

        Object expected = getStatusChangedMessage(stubApp, StatusInfo.ofDown(), StatusInfo.ofUp());

        testMTNotifier.doNotify(statusChangedEvent);

        verify(mockRestTemplate).postForObject(any(URI.class), eq(expected), eq(Object.class));
    }

    @Test
    public void test_onApplicationStatusChangedEventFromUpToDown_resolve() throws Exception {
        ClientApplicationStatusChangedEvent statusChangedEvent = new ClientApplicationStatusChangedEvent(stubApp,
                StatusInfo.ofUp(), StatusInfo.ofDown());

        Object expected = getStatusChangedMessage(stubApp, StatusInfo.ofUp(), StatusInfo.ofDown());

        testMTNotifier.doNotify(statusChangedEvent);

        verify(mockRestTemplate).postForObject(any(URI.class), eq(expected), eq(Object.class));
    }

    @Test
    public void test_onApplicationStatusChangedEventFromUpToOffline_resolve() throws Exception {
        ClientApplicationStatusChangedEvent statusChangedEvent = new ClientApplicationStatusChangedEvent(stubApp,
                StatusInfo.ofUp(), StatusInfo.ofOffline());

        Object expected = getStatusChangedMessage(stubApp, StatusInfo.ofUp(), StatusInfo.ofOffline());

        testMTNotifier.doNotify(statusChangedEvent);

        verify(mockRestTemplate).postForObject(any(URI.class), eq(expected), eq(Object.class));
    }

    @Test
    public void test_onApplicationStatusChangedEventFromDownToOffline_resolve() throws Exception {
        ClientApplicationStatusChangedEvent statusChangedEvent = new ClientApplicationStatusChangedEvent(stubApp,
                StatusInfo.ofDown(), StatusInfo.ofOffline());

        Object expected = getStatusChangedMessage(stubApp, StatusInfo.ofDown(), StatusInfo.ofOffline());

        testMTNotifier.doNotify(statusChangedEvent);

        verify(mockRestTemplate).postForObject(any(URI.class), eq(expected), eq(Object.class));
    }

    @Test
    public void test_getDeregisteredMessageForAppReturns_correctContent() {
        Message testMessage = getDeregisteredMessage(stubApp);

        assertEquals("Title doesn't match", DE_REGISTERED_TITLE, testMessage.getTitle());
        assertEquals("Summary doesn't match", MESSAGE_SUMMARY, testMessage.getSummary());

        Section expectedSection = new Section();
        expectedSection.setActivityTitle(stubApp.getName());
        expectedSection.setActivitySubtitle(String.format(DEREGISTER_ACTIVITY_SUBTITLE_PATTERN,
                stubApp.getName(),
                stubApp.getId()));
        expectedSection.getFacts().put(STATUS_KEY, stubApp.getStatusInfo().getStatus());
        expectedSection.getFacts().put(SERVICE_URL_KEY, stubApp.getServiceUrl());
        expectedSection.getFacts().put(HEALTH_URL_KEY, stubApp.getHealthUrl());
        expectedSection.getFacts().put(MANAGEMENT_URL_KEY, stubApp.getManagementUrl());
        expectedSection.getFacts().put(SOURCE_KEY, stubApp.getSource());

        assertEquals("Incorrect number of sections", 1, testMessage.getSections().size());
        assertEquals("Sections don't match", expectedSection, testMessage.getSections().get(0));
    }

    @Test
    public void test_getRegisteredMessageForAppReturns_correctContent() {
        Message testMessage = getRegisteredMessage(stubApp);

        assertEquals("Title doesn't match", REGISTERED_TITLE, testMessage.getTitle());
        assertEquals("Summary doesn't match", MESSAGE_SUMMARY, testMessage.getSummary());

        Section expectedSection = new Section();
        expectedSection.setActivityTitle(stubApp.getName());
        expectedSection.setActivitySubtitle(String.format(REGISTER_ACTIVITY_SUBTITLE_PATTERN,
                stubApp.getName(),
                stubApp.getId()));
        expectedSection.getFacts().put(STATUS_KEY, stubApp.getStatusInfo().getStatus());
        expectedSection.getFacts().put(SERVICE_URL_KEY, stubApp.getServiceUrl());
        expectedSection.getFacts().put(HEALTH_URL_KEY, stubApp.getHealthUrl());
        expectedSection.getFacts().put(MANAGEMENT_URL_KEY, stubApp.getManagementUrl());
        expectedSection.getFacts().put(SOURCE_KEY, stubApp.getSource());

        assertEquals("Incorrect number of sections", 1, testMessage.getSections().size());
        assertEquals("Sections don't match", expectedSection, testMessage.getSections().get(0));
    }

    @Test
    public void test_getStatusChangedMessageForAppReturns_correctContent() {
        Message testMessage = getStatusChangedMessage(stubApp, StatusInfo.ofUp(), StatusInfo.ofDown());

        assertEquals("Title doesn't match", STATUS_CHANGED_TITLE, testMessage.getTitle());
        assertEquals("Summary doesn't match", MESSAGE_SUMMARY, testMessage.getSummary());

        Section expectedSection = new Section();
        expectedSection.setActivityTitle(stubApp.getName());
        expectedSection.setActivitySubtitle(String.format(STATUS_ACTIVITY_SUBTITLE_PATTERN,
                stubApp.getName(),
                stubApp.getId(),
                StatusInfo.ofUp(),
                StatusInfo.ofDown()));
        expectedSection.getFacts().put(STATUS_KEY, stubApp.getStatusInfo().getStatus());
        expectedSection.getFacts().put(SERVICE_URL_KEY, stubApp.getServiceUrl());
        expectedSection.getFacts().put(HEALTH_URL_KEY, stubApp.getHealthUrl());
        expectedSection.getFacts().put(MANAGEMENT_URL_KEY, stubApp.getManagementUrl());
        expectedSection.getFacts().put(SOURCE_KEY, stubApp.getSource());

        assertEquals("Incorrect number of sections", 1, testMessage.getSections().size());
        assertEquals("Sections don't match", expectedSection, testMessage.getSections().get(0));
    }

    @Test
    public void test_shouldNotifyWithStatusChangeEventReturns_true() {
        ClientApplicationStatusChangedEvent statusChangedEvent = new ClientApplicationStatusChangedEvent(stubApp,
                StatusInfo.ofDown(), StatusInfo.ofOffline());

        boolean shouldNotify = testMTNotifier.shouldNotify(statusChangedEvent);

        assertEquals("Returned false when should notify for status change", true, shouldNotify);
    }

    @Test
    public void test_shouldNotifyWithRegisteredEventReturns_true() {
        ClientApplicationRegisteredEvent registeredEvent = new ClientApplicationRegisteredEvent(stubApp);

        boolean shouldNotify = testMTNotifier.shouldNotify(registeredEvent);

        assertEquals("Returned false when should notify for register", true, shouldNotify);
    }

    @Test
    public void test_shouldNotifyWithDeRegisteredEventReturns_true() {
        ClientApplicationDeregisteredEvent deregisteredEvent = new ClientApplicationDeregisteredEvent(stubApp);

        boolean shouldNotify = testMTNotifier.shouldNotify(deregisteredEvent);

        assertEquals("Returned false when should notify for de-register", true, shouldNotify);
    }
}