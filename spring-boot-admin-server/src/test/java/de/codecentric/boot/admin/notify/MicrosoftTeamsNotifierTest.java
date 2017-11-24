package de.codecentric.boot.admin.notify;

import de.codecentric.boot.admin.event.ClientApplicationDeregisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationRegisteredEvent;
import de.codecentric.boot.admin.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.model.Application;
import de.codecentric.boot.admin.model.StatusInfo;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

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

    private MicrosoftTeamsNotifier testMTNotifier;
    private RestTemplate mockRestTemplate;
    private Application stubApp;

    @Before
    public void setUp() throws Exception {

        mockRestTemplate = mock(RestTemplate.class);

        testMTNotifier = new MicrosoftTeamsNotifier(mockRestTemplate);

        stubApp = Application.create(appName)
                .withId(appId)
                .withHealthUrl(healthUrl)
                .withManagementUrl(managementUrl)
                .withServiceUrl(serviceUrl).build();
    }

    @Test
    public void test_onClientApplicationDeRegisteredEvent_resolve() throws Exception{
        ClientApplicationDeregisteredEvent deregisteredEvent = new ClientApplicationDeregisteredEvent(stubApp);

        testMTNotifier.doNotify(deregisteredEvent);

        verify(mockRestTemplate).postForObject(any(URI.class), any(MicrosoftTeamsNotifier.Message.class),
                eq(Void.class));
    }

    @Test
    public void test_onApplicationRegisteredEvent_resolve() throws Exception {
        ClientApplicationRegisteredEvent registeredEvent = new ClientApplicationRegisteredEvent(stubApp);


        testMTNotifier.doNotify(registeredEvent);

        verify(mockRestTemplate).postForObject(any(URI.class), any(MicrosoftTeamsNotifier.Message.class),
                eq(Void.class));
    }

    @Test
    public void test_onApplicationStatusChangedEvent_resolve() throws Exception {
        ClientApplicationStatusChangedEvent statusChangedEvent = new ClientApplicationStatusChangedEvent(stubApp,
                StatusInfo.ofDown(), StatusInfo.ofUp());

        testMTNotifier.doNotify(statusChangedEvent);

        verify(mockRestTemplate).postForObject(any(URI.class), any(MicrosoftTeamsNotifier.Message.class),
                eq(Void.class));
    }

    @Test
    public void test_shouldNotifyWithStatusChangeEventReturns_true() {
        ClientApplicationStatusChangedEvent statusChangedEvent = new ClientApplicationStatusChangedEvent(stubApp,
                StatusInfo.ofDown(), StatusInfo.ofOffline());

        boolean shouldNotify = testMTNotifier.shouldNotify(statusChangedEvent);

        assertTrue("Returned false when should notify for status change", shouldNotify);
    }

    @Test
    public void test_shouldNotifyWithRegisteredEventReturns_true() {
        ClientApplicationRegisteredEvent registeredEvent = new ClientApplicationRegisteredEvent(stubApp);

        boolean shouldNotify = testMTNotifier.shouldNotify(registeredEvent);

        assertTrue("Returned false when should notify for register", shouldNotify);
    }

    @Test
    public void test_shouldNotifyWithDeRegisteredEventReturns_true() {
        ClientApplicationDeregisteredEvent deregisteredEvent = new ClientApplicationDeregisteredEvent(stubApp);

        boolean shouldNotify = testMTNotifier.shouldNotify(deregisteredEvent);

        assertTrue("Returned false when should notify for de-register", shouldNotify);
    }

    @Test
    public void test_getDeregisteredMessageForAppReturns_correctContent() {
        MicrosoftTeamsNotifier.Message testMessage = testMTNotifier.getDeregisteredMessage(stubApp);

        assertEquals("Title doesn't match", testMTNotifier.getDeRegisteredTitle(),
                testMessage.getTitle());
        assertEquals("Summary doesn't match", testMTNotifier.getMessageSummary(),
                testMessage.getSummary());

        assertEquals("Incorrect number of sections", 1, testMessage.getSections().size());

        assertEquals("Activity Title doesn't match",
                stubApp.getName(),
                testMessage.getSections().get(0).getActivityTitle());

        assertEquals("Activity Subtitle doesn't match",
                String.format(testMTNotifier.getDeregisterActivitySubtitlePattern(),
                    stubApp.getName(),
                    stubApp.getId()),
                testMessage.getSections().get(0).getActivitySubtitle());

        assertEquals("Wrong number of facts", 5,
                testMessage.getSections().get(0).getFacts().size());
    }

    @Test
    public void test_getRegisteredMessageForAppReturns_correctContent() {
        MicrosoftTeamsNotifier.Message testMessage = testMTNotifier.getRegisteredMessage(stubApp);

        assertEquals("Title doesn't match", testMTNotifier.getRegisteredTitle(),
                testMessage.getTitle());
        assertEquals("Summary doesn't match", testMTNotifier.getMessageSummary(),
                testMessage.getSummary());

        assertEquals("Incorrect number of sections", 1, testMessage.getSections().size());

        assertEquals("Activity Title doesn't match",
                stubApp.getName(),
                testMessage.getSections().get(0).getActivityTitle());


        assertEquals("Activity Subtitle doesn't match",
                String.format(testMTNotifier.getRegisterActivitySubtitlePattern(),
                        stubApp.getName(),
                        stubApp.getId()),
                testMessage.getSections().get(0).getActivitySubtitle());

        assertEquals("Wrong number of facts", 5,
                testMessage.getSections().get(0).getFacts().size());
    }

    @Test
    public void test_getStatusChangedMessageForAppReturns_correctContent() {
        MicrosoftTeamsNotifier.Message testMessage
                = testMTNotifier.getStatusChangedMessage(stubApp, StatusInfo.ofUp(), StatusInfo.ofDown());

        assertEquals("Title doesn't match", testMTNotifier.getStatusChangedTitle(),
                testMessage.getTitle());
        assertEquals("Summary doesn't match", testMTNotifier.getMessageSummary(),
                testMessage.getSummary());

        assertEquals("Incorrect number of sections", 1, testMessage.getSections().size());

        assertEquals("Activity Title doesn't match",
                stubApp.getName(),
                testMessage.getSections().get(0).getActivityTitle());

        assertEquals("Activity Subtitle doesn't match",
                String.format(testMTNotifier.getStatusActivitySubtitlePattern(),
                        stubApp.getName(),
                        stubApp.getId(),
                        StatusInfo.ofUp().getStatus(),
                        StatusInfo.ofDown().getStatus()),
                testMessage.getSections().get(0).getActivitySubtitle());

        assertEquals("Wrong number of facts", 5,
                testMessage.getSections().get(0).getFacts().size());
    }

    @Test
    public void test_getStatusChangedMessageWithMissingFormatArgumentReturns_activitySubtitlePattern() {
        String expectedPattern = "STATUS_%s_ACTIVITY%s_PATTERN%s%s%s%s";
        testMTNotifier.setStatusActivitySubtitlePattern(expectedPattern);
        MicrosoftTeamsNotifier.Message testMessage
                = testMTNotifier.getStatusChangedMessage(stubApp, StatusInfo.ofUp(), StatusInfo.ofDown());

        assertEquals("Activity Subtitle doesn't match", expectedPattern,
                testMessage.getSections().get(0).getActivitySubtitle());

    }

    @Test
    public void test_getStatusChangedMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
        testMTNotifier.setStatusActivitySubtitlePattern("STATUS_ACTIVITY_PATTERN_%s");
        MicrosoftTeamsNotifier.Message testMessage
                = testMTNotifier.getStatusChangedMessage(stubApp, StatusInfo.ofUp(), StatusInfo.ofDown());

        assertEquals("Activity Subtitle doesn't match", "STATUS_ACTIVITY_PATTERN_" + appName,
                testMessage.getSections().get(0).getActivitySubtitle());

    }

    @Test
    public void test_getRegisterMessageWithMissingFormatArgumentReturns_activitySubtitlePattern() {
        String expectedPattern = "REGISTER_%s_ACTIVITY%s_PATTERN%s%s%s%s";
        testMTNotifier.setRegisterActivitySubtitlePattern(expectedPattern);
        MicrosoftTeamsNotifier.Message testMessage
                = testMTNotifier.getRegisteredMessage(stubApp);

        assertEquals("Activity Subtitle doesn't match", expectedPattern,
                testMessage.getSections().get(0).getActivitySubtitle());

    }

    @Test
    public void test_getRegisterMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
        testMTNotifier.setRegisterActivitySubtitlePattern("REGISTER_ACTIVITY_PATTERN_%s");
        MicrosoftTeamsNotifier.Message testMessage
                = testMTNotifier.getRegisteredMessage(stubApp);

        assertEquals("Activity Subtitle doesn't match", "REGISTER_ACTIVITY_PATTERN_" + appName,
                testMessage.getSections().get(0).getActivitySubtitle());

    }

    @Test
    public void test_getDeRegisterMessageWithMissingFormatArgumentReturns_activitySubtitlePattern() {
        String expectedPattern = "DEREGISTER_%s_ACTIVITY%s_PATTERN%s%s%s%s";
        testMTNotifier.setDeregisterActivitySubtitlePattern(expectedPattern);
        MicrosoftTeamsNotifier.Message testMessage
                = testMTNotifier.getDeregisteredMessage(stubApp);

        assertEquals("Activity Subtitle doesn't match", expectedPattern,
                testMessage.getSections().get(0).getActivitySubtitle());

    }

    @Test
    public void test_getDeRegisterMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
        testMTNotifier.setDeregisterActivitySubtitlePattern("DEREGISTER_ACTIVITY_PATTERN_%s");
        MicrosoftTeamsNotifier.Message testMessage
                = testMTNotifier.getDeregisteredMessage(stubApp);

        assertEquals("Activity Subtitle doesn't match", "DEREGISTER_ACTIVITY_PATTERN_" + appName,
                testMessage.getSections().get(0).getActivitySubtitle());

    }
}
