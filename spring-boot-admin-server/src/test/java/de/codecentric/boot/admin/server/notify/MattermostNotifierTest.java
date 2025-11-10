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
import java.util.Collections;
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

class MattermostNotifierTest {

	private static final String CHANNEL_ID = "channel";

	private static final String BOT_ACCESS_TOKEN = "bot_access_token";

	private static final String APP_NAME = "App";

	private static final Instance INSTANCE = Instance.create(InstanceId.of("-id-"))
		.register(Registration.create(APP_NAME, "https://health").build());

	private static final String MESSAGE = "test";

	private MattermostNotifier notifier;

	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		InstanceRepository repository = mock(InstanceRepository.class);
		when(repository.find(INSTANCE.getId())).thenReturn(Mono.just(INSTANCE));
		restTemplate = mock(RestTemplate.class);

		notifier = new MattermostNotifier(repository, restTemplate);
		notifier.setBotAccessToken(BOT_ACCESS_TOKEN);
		notifier.setApiUrl(URI.create("http://localhost/"));
	}

	@Test
	void test_onApplicationEvent_resolve() {
		notifier.setChannelId(CHANNEL_ID);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		clearInvocations(restTemplate);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		Object expected = expectedMessage("#2eb885", CHANNEL_ID, BOT_ACCESS_TOKEN, standardMessage("UP"));

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	@Test
	void test_onApplicationEvent_resolve_with_given_message() {
		notifier.setMessage(MESSAGE);
		notifier.setChannelId(CHANNEL_ID);

		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		clearInvocations(restTemplate);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		Object expected = expectedMessage("#2eb885", CHANNEL_ID, BOT_ACCESS_TOKEN, MESSAGE);

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	@Test
	void test_onApplicationEvent_trigger() {
		notifier.setChannelId(CHANNEL_ID);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();
		clearInvocations(restTemplate);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();

		Object expected = expectedMessage("#a30100", CHANNEL_ID, BOT_ACCESS_TOKEN, standardMessage("DOWN"));

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	private HttpEntity<Map<String, Object>> expectedMessage(String color, String channelId, String botAccessToken,
			String message) {
		Map<String, Object> messageJson = new HashMap<>();
		messageJson.put("channel_id", channelId);

		Map<String, Object> props = new HashMap<>();
		Map<String, Object> attachments = new HashMap<>();
		attachments.put("text", message);
		attachments.put("fallback", message);
		attachments.put("color", color);

		props.put("attachments", attachments);
		messageJson.put("props", Collections.singletonList(props));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(botAccessToken);
		return new HttpEntity<>(messageJson, headers);
	}

	private String standardMessage(String status) {
		return "*" + INSTANCE.getRegistration().getName() + "* (" + INSTANCE.getId() + ") is *" + status + "*";
	}

}
