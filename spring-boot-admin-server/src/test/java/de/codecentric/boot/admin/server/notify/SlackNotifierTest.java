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

class SlackNotifierTest {

	private static final String CHANNEL = "channel";

	private static final String ICON = "icon";

	private static final String USER = "user";

	private static final String APP_NAME = "App";

	private static final Instance INSTANCE = Instance.create(InstanceId.of("-id-"))
		.register(Registration.create(APP_NAME, "https://health").build());

	private static final String MESSAGE = "test";

	private SlackNotifier notifier;

	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		InstanceRepository repository = mock(InstanceRepository.class);
		when(repository.find(INSTANCE.getId())).thenReturn(Mono.just(INSTANCE));
		restTemplate = mock(RestTemplate.class);

		notifier = new SlackNotifier(repository, restTemplate);
		notifier.setUsername(USER);
		notifier.setWebhookUrl(URI.create("http://localhost/"));
	}

	@Test
	void test_onApplicationEvent_resolve() {
		notifier.setChannel(CHANNEL);
		notifier.setIcon(ICON);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		clearInvocations(restTemplate);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		Object expected = expectedMessage("good", USER, ICON, CHANNEL, standardMessage("UP"));

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	@Test
	void test_onApplicationEvent_resolve_without_channel_and_icon() {
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		clearInvocations(restTemplate);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		Object expected = expectedMessage("good", USER, null, null, standardMessage("UP"));

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	@Test
	void test_onApplicationEvent_resolve_with_given_user() {
		String anotherUser = "another user";
		notifier.setUsername(anotherUser);
		notifier.setChannel(CHANNEL);
		notifier.setIcon(ICON);

		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		clearInvocations(restTemplate);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		Object expected = expectedMessage("good", anotherUser, ICON, CHANNEL, standardMessage("UP"));

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	@Test
	void test_onApplicationEvent_resolve_with_given_message() {
		notifier.setMessage(MESSAGE);
		notifier.setChannel(CHANNEL);
		notifier.setIcon(ICON);

		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		clearInvocations(restTemplate);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		Object expected = expectedMessage("good", USER, ICON, CHANNEL, MESSAGE);

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	@Test
	void test_onApplicationEvent_trigger() {
		notifier.setChannel(CHANNEL);
		notifier.setIcon(ICON);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();
		clearInvocations(restTemplate);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();

		Object expected = expectedMessage("danger", USER, ICON, CHANNEL, standardMessage("DOWN"));

		verify(restTemplate).postForEntity(any(URI.class), eq(expected), eq(Void.class));
	}

	private HttpEntity<Map<String, Object>> expectedMessage(String color, String user, String icon, String channel,
			String message) {
		Map<String, Object> messageJson = new HashMap<>();
		messageJson.put("username", user);
		if (icon != null) {
			messageJson.put("icon_emoji", ":" + icon + ":");
		}
		if (channel != null) {
			messageJson.put("channel", channel);
		}

		Map<String, Object> attachments = new HashMap<>();
		attachments.put("text", message);
		attachments.put("color", color);
		attachments.put("mrkdwn_in", Collections.singletonList("text"));

		messageJson.put("attachments", Collections.singletonList(attachments));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<>(messageJson, headers);
	}

	private String standardMessage(String status) {
		return "*" + INSTANCE.getRegistration().getName() + "* (" + INSTANCE.getId() + ") is *" + status + "*";
	}

}
