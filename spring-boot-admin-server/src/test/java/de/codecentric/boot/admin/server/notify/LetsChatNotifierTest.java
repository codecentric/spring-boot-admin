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
import java.nio.charset.StandardCharsets;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LetsChatNotifierTest {
    private static final String room = "text_room";
    private static final String token = "text_token";
    private static final String user = "api_user";
    private static final String host = "http://localhost";
    private static final Instance instance = Instance.create(InstanceId.of("-id-"))
                                              .register(Registration.create("App", "http://health").build());
    private LetsChatNotifier notifier;
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        InstanceRepository repository = mock(InstanceRepository.class);
        when(repository.find(instance.getId())).thenReturn(Mono.just(instance));

        restTemplate = mock(RestTemplate.class);
        notifier = new LetsChatNotifier(repository);
        notifier.setUsername(user);
        notifier.setUrl(URI.create(host));
        notifier.setRoom(room);
        notifier.setToken(token);
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

        HttpEntity<?> expected = expectedMessage(standardMessage("UP"));
        verify(restTemplate).exchange(eq(URI.create(String.format("%s/rooms/%s/messages", host, room))),
            eq(HttpMethod.POST), eq(expected), eq(Void.class));
    }

    @Test
    public void test_onApplicationEvent_resolve_with_custom_message() {
        notifier.setMessage("TEST");
        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofDown())))
                    .verifyComplete();
        clearInvocations(restTemplate);

        StepVerifier.create(
            notifier.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
                    .verifyComplete();

        HttpEntity<?> expected = expectedMessage("TEST");
        verify(restTemplate).exchange(eq(URI.create(String.format("%s/rooms/%s/messages", host, room))),
            eq(HttpMethod.POST), eq(expected), eq(Void.class));
    }

    private HttpEntity<?> expectedMessage(String message) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String auth = Base64Utils.encodeToString(String.format("%s:%s", token, user).getBytes(StandardCharsets.UTF_8));
        httpHeaders.add(HttpHeaders.AUTHORIZATION, String.format("Basic %s", auth));
        Map<String, Object> messageJson = new HashMap<>();
        messageJson.put("text", message);
        return new HttpEntity<>(messageJson, httpHeaders);
    }

    private String standardMessage(String status) {
        return "*" + instance.getRegistration().getName() + "* (" + instance.getId() + ") is *" + status + "*";
    }

}
