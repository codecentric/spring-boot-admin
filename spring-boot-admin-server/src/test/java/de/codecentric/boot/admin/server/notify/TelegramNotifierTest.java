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

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TelegramNotifierTest {
    private final Instance instance = Instance.create(InstanceId.of("-id-"))
                                              .register(Registration.create("Telegram", "http://health").build());
    private InstanceRepository repository;
    private TelegramNotifier notifier;
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        repository = mock(InstanceRepository.class);
        when(repository.find(instance.getId())).thenReturn(Mono.just(instance));

        restTemplate = mock(RestTemplate.class);
        notifier = new TelegramNotifier(repository);
        notifier.setDisableNotify(false);
        notifier.setAuthToken("--token-");
        notifier.setChatId("-room-");
        notifier.setParseMode("HTML");
        notifier.setApiUrl("https://telegram.com");
        notifier.setRestTemplate(restTemplate);
    }

    @Test
    public void test_onApplicationEvent_resolve() {
        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofDown())))
                    .verifyComplete();
        clearInvocations(restTemplate);

        StepVerifier.create(
            notifier.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
                    .verifyComplete();

        verify(restTemplate).getForObject(
            eq("https://telegram.com/bot--token-/sendmessage?chat_id={chat_id}&text={text}" +
               "&parse_mode={parse_mode}&disable_notification={disable_notification}"), eq(Void.class),
            eq(getParameters("UP")));
    }

    @Test
    public void test_onApplicationEvent_trigger() {
        StatusInfo infoDown = StatusInfo.ofDown();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> httpRequest = ArgumentCaptor.forClass(
            (Class<HttpEntity<Map<String, Object>>>) (Class<?>) HttpEntity.class);

        when(restTemplate.postForEntity(isA(String.class), httpRequest.capture(), eq(Void.class))).thenReturn(
            ResponseEntity.ok().build());

        StepVerifier.create(
            notifier.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
                    .verifyComplete();
        StepVerifier.create(
            notifier.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), infoDown)))
                    .verifyComplete();

        verify(restTemplate).getForObject(
            eq("https://telegram.com/bot--token-/sendmessage?chat_id={chat_id}&text={text}" +
               "&parse_mode={parse_mode}&disable_notification={disable_notification}"), eq(Void.class),
            eq(getParameters("DOWN")));
    }

    private Map<String, Object> getParameters(String status) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("chat_id", "-room-");
        parameters.put("text", getMessage("Telegram", "-id-", status));
        parameters.put("parse_mode", "HTML");
        parameters.put("disable_notification", false);
        return parameters;
    }

    private String getMessage(String name, String id, String status) {
        return "<strong>" + name + "</strong>/" + id + " is <strong>" + status + "</strong>";
    }
}
