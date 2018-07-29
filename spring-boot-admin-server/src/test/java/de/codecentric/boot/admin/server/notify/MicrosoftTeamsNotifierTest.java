/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.notify;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.notify.MicrosoftTeamsNotifier.Message;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MicrosoftTeamsNotifierTest {
    private static final String appName = "Test App";
    private static final String appId = "TestAppId";
    private static final String healthUrl = "http://health";
    private static final String managementUrl = "http://management";
    private static final String serviceUrl = "http://service";

    private MicrosoftTeamsNotifier notifier;
    private RestTemplate mockRestTemplate;
    private Instance instance;
    private InstanceRepository repository;

    @Before
    public void setUp() {
        instance = Instance.create(InstanceId.of(appId))
                           .register(Registration.create(appName, healthUrl)
                                                 .managementUrl(managementUrl)
                                                 .serviceUrl(serviceUrl)
                                                 .build());

        repository = mock(InstanceRepository.class);
        when(repository.find(instance.getId())).thenReturn(Mono.just(instance));

        mockRestTemplate = mock(RestTemplate.class);
        notifier = new MicrosoftTeamsNotifier(repository);
        notifier.setRestTemplate(mockRestTemplate);
        notifier.setWebhookUrl(URI.create("http://example.com"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_onClientApplicationDeRegisteredEvent_resolve() {
        InstanceDeregisteredEvent event = new InstanceDeregisteredEvent(instance.getId(), 1L);

        StepVerifier.create(notifier.doNotify(event, instance)).verifyComplete();

        ArgumentCaptor<HttpEntity<Message>> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(mockRestTemplate).postForEntity(eq(URI.create("http://example.com")),
            entity.capture(),
            eq(Void.class)
        );

        assertThat(entity.getValue().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertMessage(entity.getValue().getBody(),
            notifier.getDeRegisteredTitle(),
            notifier.getMessageSummary(),
            String.format(notifier.getDeregisterActivitySubtitlePattern(),
                instance.getRegistration().getName(),
                instance.getId()
            )
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_onApplicationRegisteredEvent_resolve() {
        InstanceRegisteredEvent event = new InstanceRegisteredEvent(instance.getId(), 1L, instance.getRegistration());

        StepVerifier.create(notifier.doNotify(event, instance)).verifyComplete();

        ArgumentCaptor<HttpEntity<Message>> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(mockRestTemplate).postForEntity(eq(URI.create("http://example.com")), entity.capture(), eq(Void.class));

        assertThat(entity.getValue().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertMessage(entity.getValue().getBody(),
            notifier.getRegisteredTitle(),
            notifier.getMessageSummary(),
            String.format(notifier.getRegisterActivitySubtitlePattern(),
                instance.getRegistration().getName(),
                instance.getId()
            )
        );
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_onApplicationStatusChangedEvent_resolve() {
        InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(instance.getId(), 1L, StatusInfo.ofUp());

        StepVerifier.create(notifier.doNotify(event, instance)).verifyComplete();

        ArgumentCaptor<HttpEntity<Message>> entity = ArgumentCaptor.forClass(HttpEntity.class);
        verify(mockRestTemplate).postForEntity(eq(URI.create("http://example.com")), entity.capture(), eq(Void.class));

        assertThat(entity.getValue().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertMessage(entity.getValue().getBody(),
            notifier.getStatusChangedTitle(),
            notifier.getMessageSummary(),
            String.format(notifier.getStatusActivitySubtitlePattern(),
                instance.getRegistration().getName(),
                instance.getId(),
                StatusInfo.ofUnknown().getStatus(),
                StatusInfo.ofUp().getStatus()
            )
        );
    }

    @Test
    public void test_shouldNotifyWithRegisteredEventReturns_true() {
        InstanceRegisteredEvent event = new InstanceRegisteredEvent(instance.getId(), 1L, instance.getRegistration());
        assertThat(notifier.shouldNotify(event, instance)).isTrue();
    }

    @Test
    public void test_shouldNotifyWithDeRegisteredEventReturns_true() {
        InstanceDeregisteredEvent event = new InstanceDeregisteredEvent(instance.getId(), 1L);
        assertThat(notifier.shouldNotify(event, instance)).isTrue();
    }

    @Test
    public void test_getDeregisteredMessageForAppReturns_correctContent() {
        Message message = notifier.getDeregisteredMessage(instance);

        assertMessage(message,
            notifier.getDeRegisteredTitle(),
            notifier.getMessageSummary(),
            String.format(notifier.getDeregisterActivitySubtitlePattern(),
                instance.getRegistration().getName(),
                instance.getId()
            )
        );
    }

    @Test
    public void test_getRegisteredMessageForAppReturns_correctContent() {
        Message message = notifier.getRegisteredMessage(instance);

        assertMessage(message,
            notifier.getRegisteredTitle(),
            notifier.getMessageSummary(),
            String.format(notifier.getRegisterActivitySubtitlePattern(),
                instance.getRegistration().getName(),
                instance.getId()
            )
        );
    }

    @Test
    public void test_getStatusChangedMessageForAppReturns_correctContent() {
        Message message = notifier.getStatusChangedMessage(instance, "UP", "DOWN");

        assertMessage(message,
            notifier.getStatusChangedTitle(),
            notifier.getMessageSummary(),
            String.format(notifier.getStatusActivitySubtitlePattern(),
                instance.getRegistration().getName(),
                instance.getId(),
                StatusInfo.ofUp().getStatus(),
                StatusInfo.ofDown().getStatus()
            )
        );
    }

    @Test
    public void test_getStatusChangedMessageWithMissingFormatArgumentReturns_activitySubtitlePattern() {
        String pattern = "STATUS_%s_ACTIVITY%s_PATTERN%s%s%s%s";
        notifier.setStatusActivitySubtitlePattern(pattern);
        Message message = notifier.getStatusChangedMessage(instance, "UP", "DOWN");

        assertThat(message.getSections().get(0).getActivitySubtitle()).isEqualTo(pattern);
    }

    @Test
    public void test_getStatusChangedMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
        notifier.setStatusActivitySubtitlePattern("STATUS_ACTIVITY_PATTERN_%s");
        Message message = notifier.getStatusChangedMessage(instance, "UP", "DOWN");

        assertThat(message.getSections().get(0).getActivitySubtitle()).isEqualTo("STATUS_ACTIVITY_PATTERN_" + appName);
    }

    @Test
    public void test_getRegisterMessageWithMissingFormatArgumentReturns_activitySubtitlePattern() {
        String pattern = "REGISTER_%s_ACTIVITY%s_PATTERN%s%s%s%s";
        notifier.setRegisterActivitySubtitlePattern(pattern);
        Message message = notifier.getRegisteredMessage(instance);

        assertThat(message.getSections().get(0).getActivitySubtitle()).isEqualTo(pattern);
    }

    @Test
    public void test_getRegisterMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
        notifier.setRegisterActivitySubtitlePattern("REGISTER_ACTIVITY_PATTERN_%s");
        Message message = notifier.getRegisteredMessage(instance);

        assertThat(message.getSections().get(0).getActivitySubtitle()).isEqualTo("REGISTER_ACTIVITY_PATTERN_" +
                                                                                 appName);
    }

    @Test
    public void test_getDeRegisterMessageWithMissingFormatArgumentReturns_activitySubtitlePattern() {
        String pattern = "DEREGISTER_%s_ACTIVITY%s_PATTERN%s%s%s%s";
        notifier.setDeregisterActivitySubtitlePattern(pattern);
        Message message = notifier.getDeregisteredMessage(instance);

        assertThat(message.getSections().get(0).getActivitySubtitle()).isEqualTo(pattern);
    }

    @Test
    public void test_getDeRegisterMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
        notifier.setDeregisterActivitySubtitlePattern("DEREGISTER_ACTIVITY_PATTERN_%s");
        Message message = notifier.getDeregisteredMessage(instance);

        assertThat(message.getSections().get(0).getActivitySubtitle()).isEqualTo("DEREGISTER_ACTIVITY_PATTERN_" +
                                                                                 appName);
    }

    private void assertMessage(Message message, String expectedTitle, String expectedSummary, String expectedSubTitle) {
        assertThat(message.getTitle()).isEqualTo(expectedTitle);
        assertThat(message.getSummary()).isEqualTo(expectedSummary);
        assertThat(message.getThemeColor()).isEqualTo("6db33f");

        assertThat(message.getSections()).hasSize(1).anySatisfy(section -> {
            assertThat(section.getActivityTitle()).isEqualTo(instance.getRegistration().getName());
            assertThat(section.getActivitySubtitle()).isEqualTo(expectedSubTitle);

            assertThat(section.getFacts()).hasSize(5).anySatisfy(fact -> {
                assertThat(fact.getName()).isEqualTo("Status");
                assertThat(fact.getValue()).isEqualTo("UNKNOWN");
            }).anySatisfy(fact -> {
                assertThat(fact.getName()).isEqualTo("Service URL");
                assertThat(fact.getValue()).isEqualTo(serviceUrl);
            }).anySatisfy(fact -> {
                assertThat(fact.getName()).isEqualTo("Health URL");
                assertThat(fact.getValue()).isEqualTo(healthUrl);
            }).anySatisfy(fact -> {
                assertThat(fact.getName()).isEqualTo("Management URL");
                assertThat(fact.getValue()).isEqualTo(managementUrl);
            }).anySatisfy(fact -> {
                assertThat(fact.getName()).isEqualTo("Source");
                assertThat(fact.getValue()).isEqualTo(null);
            });
        });
    }
}
