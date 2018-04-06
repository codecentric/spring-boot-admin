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
    private InstanceRepository repository;
    private static final Instance INSTANCE = Instance.create(InstanceId.of("-id-"))
                                                     .register(Registration.create("App", "http://health").build());

    @Before
    public void setUp() {
        repository = mock(InstanceRepository.class);
        when(repository.find(INSTANCE.getId())).thenReturn(Mono.just(INSTANCE));
        restTemplate = mock(RestTemplate.class);

        notifier = new OpsGenieNotifier(repository);
        notifier.setApiKey("--service--");
        notifier.setRestTemplate(restTemplate);
        notifier.setUser("--user--");
        notifier.setSource("--source--");
        notifier.setEntity("--entity--");
        notifier.setTags("--tag1--,--tag2--");
        notifier.setActions("--action1--,--action2--");
    }

    @Test
    public void test_onApplicationEvent_resolve() {
        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion() + 1, StatusInfo.ofDown())))
                    .verifyComplete();
        reset(restTemplate);
        when(repository.find(INSTANCE.getId())).thenReturn(Mono.just(INSTANCE.withStatusInfo(StatusInfo.ofUp())));

        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion() + 2, StatusInfo.ofUp())))
                    .verifyComplete();

        verify(restTemplate).exchange(eq("https://api.opsgenie.com/v2/alerts/App_-id-/close"), eq(HttpMethod.POST),
            eq(expectedRequest("DOWN", "UP")), eq(Void.class));
    }

    @Test
    public void test_onApplicationEvent_trigger() {
        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion() + 1, StatusInfo.ofUp())))
                    .verifyComplete();
        reset(restTemplate);
        when(repository.find(INSTANCE.getId())).thenReturn(Mono.just(INSTANCE.withStatusInfo(StatusInfo.ofDown())));

        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion() + 2, StatusInfo.ofDown())))
                    .verifyComplete();

        verify(restTemplate).exchange(eq("https://api.opsgenie.com/v2/alerts"), eq(HttpMethod.POST),
            eq(expectedRequest("UP", "DOWN")), eq(Void.class));
    }

    private String getMessage(String expectedStatus) {
        return String.format("App/-id- is %s", expectedStatus);
    }

    private String getDescription(String expectedOldStatus, String expectedNewStatus) {
        return String.format("Instance App (-id-) went from %s to %s", expectedOldStatus, expectedNewStatus);
    }

    private HttpEntity<Map<String, Object>> expectedRequest(String expectedOldStatus, String expectedNewStatus) {
        Map<String, Object> expected = new HashMap<>();

        expected.put("user", "--user--");
        expected.put("source", "--source--");

        if (!"UP".equals(expectedNewStatus)) {
            expected.put("message", getMessage(expectedNewStatus));
            expected.put("alias", "App_-id-");
            expected.put("description", getDescription(expectedOldStatus, expectedNewStatus));
            expected.put("entity", "--entity--");
            expected.put("tags", "--tag1--,--tag2--");
            expected.put("actions", "--action1--,--action2--");

            Map<String, Object> details = new HashMap<>();
            details.put("type", "link");
            details.put("href", "http://health");
            details.put("text", "Instance health-endpoint");
            expected.put("details", details);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "GenieKey --service--");
        return new HttpEntity<>(expected, headers);
    }
}
