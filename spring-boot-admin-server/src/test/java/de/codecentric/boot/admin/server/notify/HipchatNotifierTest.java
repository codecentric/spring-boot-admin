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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jamie Brown
 */
public class HipchatNotifierTest {

	private final Instance instance = Instance.create(InstanceId.of("-id-"))
		.register(Registration.create("App", "http://health").build());

	private HipchatNotifier notifier;

	private RestTemplate restTemplate;

	private InstanceRepository repository;

	@BeforeEach
	public void setUp() {
		repository = mock(InstanceRepository.class);
		when(repository.find(instance.getId())).thenReturn(Mono.just(instance));

		restTemplate = mock(RestTemplate.class);
		notifier = new HipchatNotifier(repository, restTemplate);
		notifier.setNotify(true);
		notifier.setAuthToken("--token-");
		notifier.setRoomId("-room-");
		notifier.setUrl(URI.create("http://localhost/v2"));
	}

	@Test
	public void test_onApplicationEvent_resolve() {
		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<Map<String, Object>>> httpRequest = ArgumentCaptor
			.forClass((Class<HttpEntity<Map<String, Object>>>) (Class<?>) HttpEntity.class);

		when(restTemplate.postForEntity(isA(String.class), httpRequest.capture(), eq(Void.class)))
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
		assertThat(body).containsEntry("color", "green");
		assertThat(body).containsEntry("message", "<strong>App</strong>/-id- is <strong>UP</strong>");
		assertThat(body).containsEntry("notify", Boolean.TRUE);
		assertThat(body).containsEntry("message_format", "html");

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

		assertThat(httpRequest.getValue().getHeaders()).containsEntry("Content-Type",
				Collections.singletonList("application/json"));
		Map<String, Object> body = httpRequest.getValue().getBody();
		assertThat(body).containsEntry("color", "red");
		assertThat(body).containsEntry("message", "<strong>App</strong>/-id- is <strong>DOWN</strong>");
		assertThat(body).containsEntry("notify", Boolean.TRUE);
		assertThat(body).containsEntry("message_format", "html");
	}

}
