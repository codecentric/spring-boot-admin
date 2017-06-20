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
import java.util.Collections;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jamie Brown
 */
public class HipchatNotifierTest {
    public final Application application = Application.create(ApplicationId.of("-id-"),
            Registration.create("App", "http://health").build()).build();
    private HipchatNotifier notifier;
    private RestTemplate restTemplate;
    private ApplicationStore store;

    @Before
    public void setUp() {
        store = mock(ApplicationStore.class);
        when(store.find(application.getId())).thenReturn(application);

        restTemplate = mock(RestTemplate.class);
        notifier = new HipchatNotifier(store);
        notifier.setNotify(true);
        notifier.setAuthToken("--token-");
        notifier.setRoomId("-room-");
        notifier.setUrl(URI.create("http://localhost/v2"));
        notifier.setRestTemplate(restTemplate);
    }

    @Test
    public void test_onApplicationEvent_resolve() {
        @SuppressWarnings("unchecked") ArgumentCaptor<HttpEntity<Map<String, Object>>> httpRequest = ArgumentCaptor.forClass(
                (Class<HttpEntity<Map<String, Object>>>) (Class<?>) HttpEntity.class);

        when(restTemplate.postForEntity(isA(String.class), httpRequest.capture(), eq(Void.class))).thenReturn(
                ResponseEntity.ok().build());

        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofDown()));
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofUp()));

        assertThat(httpRequest.getValue().getHeaders()).containsEntry("Content-Type",
                Collections.singletonList("application/json"));

        Map<String, Object> body = httpRequest.getValue().getBody();
        assertThat(body).containsEntry("color", "green");
        assertThat(body).containsEntry("message", "<strong>App</strong>/-id- is <strong>UP</strong>");
        assertThat(body).containsEntry("notify", Boolean.TRUE);
        assertThat(body).containsEntry("message_format", "html");

    }

    @Test
    public void test_onApplicationEvent_trigger() {
        StatusInfo infoDown = StatusInfo.ofDown();

        @SuppressWarnings("unchecked") ArgumentCaptor<HttpEntity<Map<String, Object>>> httpRequest = ArgumentCaptor.forClass(
                (Class<HttpEntity<Map<String, Object>>>) (Class<?>) HttpEntity.class);

        when(restTemplate.postForEntity(isA(String.class), httpRequest.capture(), eq(Void.class))).thenReturn(
                ResponseEntity.ok().build());

        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), StatusInfo.ofUp()));
        notifier.notify(new ClientApplicationStatusChangedEvent(application.getId(), infoDown));

        assertThat(httpRequest.getValue().
                getHeaders()).
                                     containsEntry("Content-Type", Collections.singletonList("application/json"));
        Map<String, Object> body = httpRequest.getValue().getBody();
        assertThat(body).containsEntry("color", "red");
        assertThat(body).containsEntry("message", "<strong>App</strong>/-id- is <strong>DOWN</strong>");
        assertThat(body).containsEntry("notify", Boolean.TRUE);
        assertThat(body).containsEntry("message_format", "html");
    }
}
