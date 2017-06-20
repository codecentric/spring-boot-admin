/*
 * Copyright 2014-2017 the original author or authors.
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

import de.codecentric.boot.admin.server.event.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.model.Application;
import de.codecentric.boot.admin.server.model.ApplicationId;
import de.codecentric.boot.admin.server.model.Registration;
import de.codecentric.boot.admin.server.model.StatusInfo;
import de.codecentric.boot.admin.server.registry.store.ApplicationStore;

import java.net.URI;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LetsChatNotifierTest {
    private final String room = "text_room";
    private final String token = "text_token";
    private final String user = "api_user";
    private final String host = "http://localhost";
    private final Application application = Application.create(ApplicationId.of("-id-"),
            Registration.create("App", "http://health").build()).build();
    private ApplicationStore store;
    private LetsChatNotifier notifier;
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        store = mock(ApplicationStore.class);
        when(store.find(application.getId())).thenReturn(application);

        restTemplate = mock(RestTemplate.class);
        notifier = new LetsChatNotifier(store);
        notifier.setUsername(user);
        notifier.setUrl(URI.create(host));
        notifier.setRoom(room);
        notifier.setToken(token);
        notifier.setRestTemplate(restTemplate);
    }

    @Test
    public void test_onApplicationEvent_resolve() {
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofDown()));
        reset(restTemplate);

        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofUp()));

        HttpEntity<?> expected = expectedMessage(standardMessage("UP"));
        verify(restTemplate).exchange(eq(URI.create(String.format("%s/rooms/%s/messages", host, room))),
                eq(HttpMethod.POST), eq(expected), eq(Void.class));
    }

    @Test
    public void test_onApplicationEvent_resolve_with_custom_message() {
        notifier.setMessage("TEST");
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofDown()));
        reset(restTemplate);

        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofUp()));

        HttpEntity<?> expected = expectedMessage("TEST");
        verify(restTemplate).exchange(eq(URI.create(String.format("%s/rooms/%s/messages", host, room))),
                eq(HttpMethod.POST), eq(expected), eq(Void.class));
    }

    private HttpEntity<?> expectedMessage(String message) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        String auth = Base64Utils.encodeToString(String.format("%s:%s", token, user).getBytes());
        httpHeaders.add(HttpHeaders.AUTHORIZATION, String.format("Basic %s", auth));
        Map<String, Object> messageJson = new HashMap<>();
        messageJson.put("text", message);
        return new HttpEntity<>(messageJson, httpHeaders);
    }

    private String standardMessage(String status) {
        return "*" + application.getRegistration().getName() + "* (" + application.getId() + ") is *" + status + "*";
    }

}
