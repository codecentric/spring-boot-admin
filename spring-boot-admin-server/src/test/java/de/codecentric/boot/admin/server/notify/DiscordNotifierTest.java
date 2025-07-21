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

class DiscordNotifierTest {

	private static final String AVATAR_URL = "http://avatarUrl";

	private static final String USER_NAME = "user";

	private static final String APP_NAME = "App";

	private static final URI WEBHOOK_URI = URI.create("http://localhost/");

	private static final Instance INSTANCE = Instance.create(InstanceId.of("-id-"))
		.register(Registration.create(APP_NAME, "https://health").build());

	private DiscordNotifier notifier;

	private RestTemplate restTemplate;

	@BeforeEach
	void setUp() {
		InstanceRepository repository = mock(InstanceRepository.class);
		when(repository.find(INSTANCE.getId())).thenReturn(Mono.just(INSTANCE));
		restTemplate = mock(RestTemplate.class);
		notifier = new DiscordNotifier(repository, restTemplate);
		notifier.setWebhookUrl(WEBHOOK_URI);
	}

	@Test
	void test_onApplicationEvent_resolve() {
		notifier.setUsername(USER_NAME);
		notifier.setAvatarUrl(AVATAR_URL);
		notifier.setTts(true);

		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		clearInvocations(restTemplate);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		Object expected = expectedMessage(USER_NAME, true, AVATAR_URL, standardMessage("UP"));

		verify(restTemplate).postForEntity(WEBHOOK_URI, expected, Void.class);
	}

	@Test
	void test_onApplicationEvent_resolve_minimum_configuration() {
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		clearInvocations(restTemplate);
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(INSTANCE.getId(), INSTANCE.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		Object expected = expectedMessage(null, false, null, standardMessage("UP"));

		verify(restTemplate).postForEntity(WEBHOOK_URI, expected, Void.class);
	}

	private HttpEntity<Map<String, Object>> expectedMessage(String username, boolean tts, String avatarUrl,
			String message) {
		Map<String, Object> body = new HashMap<>();
		body.put("content", message);
		body.put("tts", tts);

		if (avatarUrl != null) {
			body.put("avatar_url", avatarUrl);
		}
		if (username != null) {
			body.put("username", username);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.USER_AGENT, "RestTemplate");

		return new HttpEntity<>(body, headers);
	}

	private String standardMessage(String status) {
		return "*" + INSTANCE.getRegistration().getName() + "* (" + INSTANCE.getId() + ") is *" + status + "*";
	}

}
