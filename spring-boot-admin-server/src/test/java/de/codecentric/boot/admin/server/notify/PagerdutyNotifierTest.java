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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PagerdutyNotifierTest {

    private PagerdutyNotifier notifier;
    private RestTemplate restTemplate;

    private InstanceRepository repository;
    private static final String appName = "App";
    private static final Instance INSTANCE = Instance.create(InstanceId.of("-id-"))
                                                     .register(Registration.create(appName, "http://health").build());

    @Before
    public void setUp() {
        repository = mock(InstanceRepository.class);
        when(repository.find(INSTANCE.getId())).thenReturn(Mono.just(INSTANCE));
        restTemplate = mock(RestTemplate.class);

        notifier = new PagerdutyNotifier(repository);
        notifier.setServiceKey("--service--");
        notifier.setClient("TestClient");
        notifier.setClientUrl(URI.create("http://localhost"));
        notifier.setRestTemplate(restTemplate);
    }


    @Test
    public void test_onApplicationEvent_resolve() {
        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion() + 1, StatusInfo.ofDown())))
                    .verifyComplete();
        reset(restTemplate);

        StatusInfo up = StatusInfo.ofUp();
        when(repository.find(INSTANCE.getId())).thenReturn(Mono.just(INSTANCE.withStatusInfo(up)));
        StepVerifier.create(
            notifier.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion() + 2, up)))
                    .verifyComplete();


        Map<String, Object> expected = new HashMap<>();
        expected.put("service_key", "--service--");
        expected.put("incident_key", "App/-id-");
        expected.put("event_type", "resolve");
        expected.put("description", "App/-id- is UP");
        Map<String, Object> details = new HashMap<>();
        details.put("from", "DOWN");
        details.put("to", up);
        expected.put("details", details);

        verify(restTemplate).postForEntity(eq(PagerdutyNotifier.DEFAULT_URI), eq(expected), eq(Void.class));
    }

    @Test
    public void test_onApplicationEvent_trigger() {
        StepVerifier.create(notifier.notify(
            new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion() + 1, StatusInfo.ofUp())))
                    .verifyComplete();
        reset(restTemplate);

        StatusInfo down = StatusInfo.ofDown();
        when(repository.find(INSTANCE.getId())).thenReturn(Mono.just(INSTANCE.withStatusInfo(down)));
        StepVerifier.create(
            notifier.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion() + 2, down)))
                    .verifyComplete();

        Map<String, Object> expected = new HashMap<>();
        expected.put("service_key", "--service--");
        expected.put("incident_key", "App/-id-");
        expected.put("event_type", "trigger");
        expected.put("description", "App/-id- is DOWN");
        expected.put("client", "TestClient");
        expected.put("client_url", URI.create("http://localhost"));
        Map<String, Object> details = new HashMap<>();
        details.put("from", "UP");
        details.put("to", down);
        expected.put("details", details);
        Map<String, Object> context = new HashMap<>();
        context.put("type", "link");
        context.put("href", "http://health");
        context.put("text", "Application health-endpoint");
        expected.put("contexts", Arrays.asList(context));

        verify(restTemplate).postForEntity(eq(PagerdutyNotifier.DEFAULT_URI), eq(expected), eq(Void.class));
    }

}
