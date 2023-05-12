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
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeiShuNotifierTest {

	public static final String WEBHOOK_URL = "http://localhost/v2";

	private final Instance instance = Instance.create(InstanceId.of("-id-"))
		.register(Registration.create("App", "http://health").build());

	private FeiShuNotifier notifier;

	private RestTemplate restTemplate;

	private InstanceRepository instanceRepository;

	@BeforeEach
	public void setUp() {
		instanceRepository = mock(InstanceRepository.class);
		when(instanceRepository.find(instance.getId())).thenReturn(Mono.just(instance));

		restTemplate = mock(RestTemplate.class);
		notifier = new FeiShuNotifier(instanceRepository, restTemplate);
		notifier.setWebhookUrl(URI.create(WEBHOOK_URL));
	}

	@Test
	public void test_onApplicationEvent_resolve() {
		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<Map<String, Object>>> httpRequest = ArgumentCaptor
			.forClass((Class<HttpEntity<Map<String, Object>>>) (Class<?>) HttpEntity.class);

		when(restTemplate.postForEntity(any(), httpRequest.capture(), eq(String.class)))
			.thenReturn(ResponseEntity.ok().build());

		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofDown())))
			.verifyComplete();
		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();

		assertThat(httpRequest.getValue().getHeaders()).containsEntry("Content-Type",
				Collections.singletonList("application/json"));

		Map<String, Object> body = httpRequest.getValue().getBody();
		assertThat(body).containsEntry("card",
				"{\"elements\":[{\"tag\":\"div\",\"text\":{\"tag\":\"plain_text\",\"content\":\"ServiceName: App(-id-) \\nServiceUrl:  \\nStatus: changed status from [DOWN] to [UP]\"}},{\"tag\":\"div\",\"text\":{\"tag\":\"lark_md\",\"content\":\"<at id=all></at>\"}}],\"header\":{\"template\":\"red\",\"title\":{\"tag\":\"plain_text\",\"content\":\"Codecentric's Spring Boot Admin notice\"}}}");
		assertThat(body).containsEntry("msg_type", FeiShuNotifier.MessageType.interactive);
	}

	@Test
	public void test_onApplicationEvent_trigger() {
		StatusInfo infoDown = StatusInfo.ofDown();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<Map<String, Object>>> httpRequest = ArgumentCaptor
			.forClass((Class<HttpEntity<Map<String, Object>>>) (Class<?>) HttpEntity.class);

		when(restTemplate.postForEntity(any(), httpRequest.capture(), eq(String.class)))
			.thenReturn(ResponseEntity.ok().build());

		StepVerifier
			.create(notifier
				.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), StatusInfo.ofUp())))
			.verifyComplete();
		StepVerifier
			.create(notifier.notify(new InstanceStatusChangedEvent(instance.getId(), instance.getVersion(), infoDown)))
			.verifyComplete();

		assertThat(httpRequest.getValue().getHeaders()).containsEntry("Content-Type",
				Collections.singletonList("application/json"));
		Map<String, Object> body = httpRequest.getValue().getBody();
		assertThat(body).containsEntry("card",
				"{\"elements\":[{\"tag\":\"div\",\"text\":{\"tag\":\"plain_text\",\"content\":\"ServiceName: App(-id-) \\nServiceUrl:  \\nStatus: changed status from [UP] to [DOWN]\"}},{\"tag\":\"div\",\"text\":{\"tag\":\"lark_md\",\"content\":\"<at id=all></at>\"}}],\"header\":{\"template\":\"red\",\"title\":{\"tag\":\"plain_text\",\"content\":\"Codecentric's Spring Boot Admin notice\"}}}");
		assertThat(body).containsEntry("msg_type", FeiShuNotifier.MessageType.interactive);
	}

}
