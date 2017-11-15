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
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SlackNotifierTest {
    private static final String channel = "channel";
    private static final String icon = "icon";
    private static final String user = "user";
    private static final String appName = "App";
    private static final Instance INSTANCE = Instance.create(InstanceId.of("-id-"))
                                                     .register(Registration.create(appName, "http://health").build());
    private static final String message = "test";

    private SlackNotifier notifier;
    private RestTemplate restTemplate;
    private InstanceRepository repository;

    @Before
    public void setUp() {
        repository = mock(InstanceRepository.class);
        when(repository.find(INSTANCE.getId())).thenReturn(Mono.just(INSTANCE));
        restTemplate = mock(RestTemplate.class);

        notifier = new SlackNotifier(repository);
        notifier.setUsername(user);
        notifier.setWebhookUrl(URI.create("http://localhost/"));
        notifier.setRestTemplate(restTemplate);
    }

    @Test
    public void test_onApplicationEvent_resolve() {
        notifier.setChannel(channel);
        notifier.setIcon(icon);
        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
                    .verifyComplete();
        clearInvocations(restTemplate);
        StepVerifier.create(
            notifier.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
                    .verifyComplete();

        Object expected = expectedMessage("good", user, icon, channel, standardMessage("UP"));

        verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
    }

    @Test
    public void test_onApplicationEvent_resolve_without_channel_and_icon() {
        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
                    .verifyComplete();
        clearInvocations(restTemplate);
        StepVerifier.create(
            notifier.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
                    .verifyComplete();

        Object expected = expectedMessage("good", user, null, null, standardMessage("UP"));

        verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
    }

    @Test
    public void test_onApplicationEvent_resolve_with_given_user() {
        String anotherUser = "another user";
        notifier.setUsername(anotherUser);
        notifier.setChannel(channel);
        notifier.setIcon(icon);

        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
                    .verifyComplete();
        clearInvocations(restTemplate);
        StepVerifier.create(
            notifier.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
                    .verifyComplete();

        Object expected = expectedMessage("good", anotherUser, icon, channel, standardMessage("UP"));

        verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
    }

    @Test
    public void test_onApplicationEvent_resolve_with_given_message() {
        notifier.setMessage(message);
        notifier.setChannel(channel);
        notifier.setIcon(icon);

        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
                    .verifyComplete();
        clearInvocations(restTemplate);
        StepVerifier.create(
            notifier.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
                    .verifyComplete();

        Object expected = expectedMessage("good", user, icon, channel, message);

        verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
    }

    @Test
    public void test_onApplicationEvent_trigger() {
        notifier.setChannel(channel);
        notifier.setIcon(icon);
        StepVerifier.create(
            notifier.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
                    .verifyComplete();
        clearInvocations(restTemplate);
        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
                    .verifyComplete();

        Object expected = expectedMessage("danger", user, icon, channel, standardMessage("DOWN"));

        verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
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

    private String standardMessage(String status) {
        return "*" + INSTANCE.getRegistration().getName() + "* (" + INSTANCE.getId() + ") is *" + status + "*";
    }
}
