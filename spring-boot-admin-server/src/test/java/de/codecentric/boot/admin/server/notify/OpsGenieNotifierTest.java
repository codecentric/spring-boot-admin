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


import de.codecentric.boot.admin.server.domain.entities.Application;
import de.codecentric.boot.admin.server.domain.entities.ApplicationRepository;
import de.codecentric.boot.admin.server.domain.events.ClientApplicationStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.ApplicationId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpsGenieNotifierTest {
    private OpsGenieNotifier notifier;
    private RestTemplate restTemplate;
    private ApplicationRepository repository;
    private static final String appName = "App";
    private static final Application application = Application.create(ApplicationId.of("-id-"))
                                                              .register(Registration.create(appName, "http://health")
                                                                                    .build());

    @Before
    public void setUp() {
        repository = mock(ApplicationRepository.class);
        when(repository.find(application.getId())).thenReturn(Mono.just(application));
        restTemplate = mock(RestTemplate.class);

        notifier = new OpsGenieNotifier(repository);
        notifier.setApiKey("--service--");
        notifier.setRecipients("--recipients--");
        notifier.setRestTemplate(restTemplate);
    }

    @Test
    public void test_onApplicationEvent_resolve() {
        StepVerifier.create(notifier.notify(
                new ClientApplicationStatusChangedEvent(application.getId(), application.getVersion() + 1,
                        StatusInfo.ofDown()))).verifyComplete();
        reset(restTemplate);
        when(repository.find(application.getId())).thenReturn(Mono.just(application.withStatusInfo(StatusInfo.ofUp())));

        StepVerifier.create(notifier.notify(
                new ClientApplicationStatusChangedEvent(application.getId(), application.getVersion() + 2,
                        StatusInfo.ofUp()))).verifyComplete();

        verify(restTemplate).exchange(eq("https://api.opsgenie.com/v1/json/alert/close"), eq(HttpMethod.POST),
                eq(expectedRequest("DOWN", "UP")), eq(Void.class));
    }

    @Test
    public void test_onApplicationEvent_trigger() {
        StepVerifier.create(notifier.notify(
                new ClientApplicationStatusChangedEvent(application.getId(), application.getVersion() + 1,
                        StatusInfo.ofUp()))).verifyComplete();
        reset(restTemplate);
        when(repository.find(application.getId())).thenReturn(
                Mono.just(application.withStatusInfo(StatusInfo.ofDown())));
        
        StepVerifier.create(notifier.notify(
                new ClientApplicationStatusChangedEvent(application.getId(), application.getVersion() + 2,
                        StatusInfo.ofDown()))).verifyComplete();

        verify(restTemplate).exchange(eq("https://api.opsgenie.com/v1/json/alert"), eq(HttpMethod.POST),
                eq(expectedRequest("UP", "DOWN")), eq(Void.class));
    }


    private String getMessage(String expectedStatus) {
        return String.format("App/-id- is %s", expectedStatus);
    }

    private String getDescription(String expectedOldStatus, String expectedNewStatus) {
        return String.format("Application App (-id-) went from %s to %s", expectedOldStatus, expectedNewStatus);
    }

    private HttpEntity expectedRequest(String expectedOldStatus, String expectedNewStatus) {
        Map<String, Object> expected = new HashMap<>();
        expected.put("apiKey", "--service--");
        expected.put("message", getMessage(expectedNewStatus));
        expected.put("alias", "App/-id-");
        expected.put("description", getDescription(expectedOldStatus, expectedNewStatus));

        if (!"UP".equals(expectedNewStatus)) {

            expected.put("recipients", "--recipients--");
            Map<String, Object> details = new HashMap<>();
            details.put("type", "link");
            details.put("href", "http://health");
            details.put("text", "Application health-endpoint");
            expected.put("details", details);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(expected, headers);
    }
}
