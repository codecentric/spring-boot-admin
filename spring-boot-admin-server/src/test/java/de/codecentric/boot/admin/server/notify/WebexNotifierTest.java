/*
 * Copyright 2014-2024 the original author or authors.
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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WebexNotifierTest {

	private final Instance instance = Instance.create(InstanceId.of("-id-"))
		.register(Registration.create("webex", "http://health").build());

	private InstanceRepository repository;

	private WebexNotifier notifier;

	private RestTemplate restTemplate;

	@BeforeEach
	public void setUp() {
		repository = mock(InstanceRepository.class);
		when(repository.find(instance.getId())).thenReturn(Mono.just(instance));

		restTemplate = mock(RestTemplate.class);
		notifier = new WebexNotifier(repository, restTemplate);
		notifier.setAuthToken("--token-");
		notifier.setRoomId("--room--");
		notifier.setUrl(URI.create("https://webexapis.com/v1/messages"));
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

		URI DEFAULT_URL = URI.create("https://webexapis.com/v1/messages");

		HttpEntity entity = new HttpEntity<>(createMessage("UP"), createHeaders());

		verify(restTemplate).postForEntity(eq(DEFAULT_URL), eq(entity), eq(Void.class));
	}

	@Test
	public void test_onApplicationEvent_trigger() {
		StatusInfo infoDown = StatusInfo.ofDown();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<Map<String, Object>>> httpRequest = ArgumentCaptor
			.forClass((Class<HttpEntity<Map<String, Object>>>) (Class<?>) HttpEntity.class);

		when(restTemplate.postForEntity(isA(String.class), httpRequest.capture(), eq(Void.class)))
			.thenReturn(ResponseEntity.ok().build());

		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();
		StepVerifier
			.create(notifier.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), infoDown)))
			.verifyComplete();

		URI DEFAULT_URL = URI.create("https://webexapis.com/v1/messages");

		HttpEntity entity = new HttpEntity<>(createMessage("DOWN"), createHeaders());

		verify(restTemplate).postForEntity(eq(DEFAULT_URL), eq(entity), eq(Void.class));
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth("--token-");
		return headers;
	}

	private Map<String, Object> createMessage(String status) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("roomId", "--room--");
		parameters.put("markdown", getMessage("webex", "-id-", status));
		return parameters;
	}

	private String getMessage(String name, String id, String status) {
		return "<strong>" + name + "</strong>/" + id + " is <strong>" + status + "</strong>";
	}

}
