/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.server.notify;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DingTalkNotifierTest {

	private final Instance instance = Instance.create(InstanceId.of("-id-"))
		.register(Registration.create("DingTalk", "http://health").build());

	private InstanceRepository repository;

	private DingTalkNotifier notifier;

	private RestTemplate restTemplate;

	@BeforeEach
	public void setUp() {
		repository = mock(InstanceRepository.class);
		when(repository.find(instance.getId())).thenReturn(Mono.just(instance));

		restTemplate = mock(RestTemplate.class);
		notifier = new DingTalkNotifier(repository, restTemplate);
		notifier.setWebhookUrl("https://dingtalk.com/");
		notifier.setSecret("-secret-");
	}

	@Test
	public void test_onApplicationEvent_resolve() {
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		clearInvocations(restTemplate);

		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		Object expected = expectedMessage(standardMessage("UP"));

		verify(restTemplate).postForEntity(any(String.class), eq(expected), eq(Void.class));
	}

	@Test
	public void test_onApplicationEvent_trigger() {
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();

		Object expected = expectedMessage(standardMessage("DOWN"));

		verify(restTemplate).postForEntity(any(String.class), eq(expected), eq(Void.class));
	}

	private HttpEntity<Map<String, Object>> expectedMessage(String message) {
		Map<String, Object> messageJson = new HashMap<>();
		messageJson.put("msgtype", "text");

		Map<String, Object> content = new HashMap<>();
		content.put("content", message);
		messageJson.put("text", content);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(messageJson, headers);
	}

	private String standardMessage(String status) {
		return instance.getRegistration().getName() + " " + instance.getId() + " is " + status;
	}

}
