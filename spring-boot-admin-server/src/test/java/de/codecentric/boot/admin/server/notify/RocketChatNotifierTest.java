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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RocketChatNotifierTest {

	private static final String ROOM_ID = "roomId";

	private static final String TOKEN = "tokenApi";

	private static final String USER_ID = "userId";

	private static final String HOST = "http://localhost";

	private static final String MESSAGE = "test";

	private static final Instance instance = Instance.create(InstanceId.of("-id-"))
		.register(Registration.create("App", "https://health").build());

	private RocketChatNotifier notifier;

	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		InstanceRepository repository = mock(InstanceRepository.class);
		when(repository.find(instance.getId())).thenReturn(Mono.just(instance));

		restTemplate = mock(RestTemplate.class);
		notifier = new RocketChatNotifier(repository, restTemplate);
		notifier.setUrl(HOST);
		notifier.setUserId(USER_ID);
		notifier.setToken(TOKEN);
		notifier.setRoomId(ROOM_ID);
	}

	@Test
	void test_onApplicationEvent_resolve() {
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		clearInvocations(restTemplate);

		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		HttpEntity<?> expected = expectedMessage(standardMessage(StatusInfo.ofUp().getStatus()));
		verify(restTemplate).exchange(URI.create(String.format("%s/api/v1/chat.sendMessage", HOST)), HttpMethod.POST,
				expected, Void.class);
	}

	@Test
	void test_onApplicationEvent_resolve_with_given_message() {
		notifier.setMessage(MESSAGE);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		clearInvocations(restTemplate);

		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		HttpEntity<?> expected = expectedMessage(MESSAGE);
		verify(restTemplate).exchange(URI.create(String.format("%s/api/v1/chat.sendMessage", HOST)), HttpMethod.POST,
				expected, Void.class);
	}

	private HttpEntity<?> expectedMessage(String message) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		httpHeaders.add("X-Auth-Token", TOKEN);
		httpHeaders.add("X-User-Id", USER_ID);
		Map<String, String> messageJsonData = new HashMap<>();
		messageJsonData.put("rid", ROOM_ID);
		messageJsonData.put("msg", message);
		Map<String, Object> messageJson = new HashMap<>();
		messageJson.put("message", messageJsonData);
		return new HttpEntity<>(messageJson, httpHeaders);
	}

	private String standardMessage(String status) {
		return "*" + instance.getRegistration().getName() + "* (" + instance.getId() + ") is *" + status + "*";
	}

}
