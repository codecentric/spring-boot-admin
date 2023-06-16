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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import de.codecentric.boot.admin.server.domain.entities.Instance;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.domain.events.InstanceDeregisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceRegisteredEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.domain.values.StatusInfo;
import de.codecentric.boot.admin.server.notify.MicrosoftTeamsNotifier.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MicrosoftTeamsNotifierTest {

	private static final String BLUE = "439fe0";

	private static final String RED = "b32d36";

	private static final String GREEN = "6db33f";

	private static final String appName = "Test App";

	private static final String appId = "TestAppId";

	private static final String healthUrl = "http://health";

	private static final String managementUrl = "http://management";

	private static final String serviceUrl = "http://service";

	private MicrosoftTeamsNotifier notifier;

	private RestTemplate mockRestTemplate;

	private Instance instance;

	private InstanceRepository repository;

	@BeforeEach
	void setUp() {
		instance = Instance.create(InstanceId.of(appId))
			.register(Registration.create(appName, healthUrl)
				.managementUrl(managementUrl)
				.serviceUrl(serviceUrl)
				.build());

		repository = mock(InstanceRepository.class);
		when(repository.find(instance.getId())).thenReturn(Mono.just(instance));

		mockRestTemplate = mock(RestTemplate.class);
		notifier = new MicrosoftTeamsNotifier(repository, mockRestTemplate);
		notifier.setWebhookUrl(URI.create("http://example.com"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void test_onClientApplicationDeRegisteredEvent_resolve() {
		InstanceDeregisteredEvent event = new InstanceDeregisteredEvent(instance.getId(), 1L);

		StepVerifier.create(notifier.doNotify(event, instance)).verifyComplete();

		ArgumentCaptor<HttpEntity<Message>> entity = ArgumentCaptor.forClass(HttpEntity.class);
		verify(mockRestTemplate).postForEntity(eq(URI.create("http://example.com")), entity.capture(), eq(Void.class));

		assertThat(entity.getValue().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
		assertMessage(entity.getValue().getBody(), notifier.getDeRegisteredTitle(), notifier.getMessageSummary(),
				"Test App with id TestAppId has de-registered from Spring Boot Admin", BLUE);
	}

	@Test
	@SuppressWarnings("unchecked")
	void test_onApplicationRegisteredEvent_resolve() {
		InstanceRegisteredEvent event = new InstanceRegisteredEvent(instance.getId(), 1L, instance.getRegistration());

		StepVerifier.create(notifier.doNotify(event, instance)).verifyComplete();

		ArgumentCaptor<HttpEntity<Message>> entity = ArgumentCaptor.forClass(HttpEntity.class);
		verify(mockRestTemplate).postForEntity(eq(URI.create("http://example.com")), entity.capture(), eq(Void.class));

		assertThat(entity.getValue().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
		assertMessage(entity.getValue().getBody(), notifier.getRegisteredTitle(), notifier.getMessageSummary(),
				"Test App with id TestAppId has registered with Spring Boot Admin", BLUE);
	}

	@Test
	@SuppressWarnings("unchecked")
	void test_onApplicationStatusChangedEvent_resolve() {
		InstanceStatusChangedEvent event = new InstanceStatusChangedEvent(instance.getId(), 1L, StatusInfo.ofUp());

		StepVerifier.create(notifier.doNotify(event, instance)).verifyComplete();

		ArgumentCaptor<HttpEntity<Message>> entity = ArgumentCaptor.forClass(HttpEntity.class);
		verify(mockRestTemplate).postForEntity(eq(URI.create("http://example.com")), entity.capture(), eq(Void.class));

		assertThat(entity.getValue().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
		assertMessage(entity.getValue().getBody(), notifier.getStatusChangedTitle(), notifier.getMessageSummary(),
				"Test App with id TestAppId changed status from UNKNOWN to UP", GREEN);
	}

	@Test
	void test_shouldNotifyWithRegisteredEventReturns_true() {
		InstanceRegisteredEvent event = new InstanceRegisteredEvent(instance.getId(), 1L, instance.getRegistration());
		assertThat(notifier.shouldNotify(event, instance)).isTrue();
	}

	@Test
	void test_shouldNotifyWithDeRegisteredEventReturns_true() {
		InstanceDeregisteredEvent event = new InstanceDeregisteredEvent(instance.getId(), 1L);
		assertThat(notifier.shouldNotify(event, instance)).isTrue();
	}

	@Test
	void test_getDeregisteredMessageForAppReturns_correctContent() {
		Message message = notifier.getDeregisteredMessage(instance,
				notifier.createEvaluationContext(new InstanceDeregisteredEvent(instance.getId(), 1L), instance));

		assertMessage(message, notifier.getDeRegisteredTitle(), notifier.getMessageSummary(),
				"Test App with id TestAppId has de-registered from Spring Boot Admin", BLUE);
	}

	@Test
	void test_getRegisteredMessageForAppReturns_correctContent() {
		Message message = notifier.getRegisteredMessage(instance,
				notifier.createEvaluationContext(new InstanceDeregisteredEvent(instance.getId(), 1L), instance));

		assertMessage(message, notifier.getRegisteredTitle(), notifier.getMessageSummary(),
				"Test App with id TestAppId has registered with Spring Boot Admin", BLUE);
	}

	@Test
	void test_getStatusChangedMessageForAppReturns_correctContent() {
		Message message = notifier.getStatusChangedMessage(instance, notifier.createEvaluationContext(
				new InstanceStatusChangedEvent(instance.getId(), 1L, StatusInfo.ofDown()), instance));

		assertMessage(message, notifier.getStatusChangedTitle(), notifier.getMessageSummary(),
				"Test App with id TestAppId changed status from UNKNOWN to DOWN", RED);
	}

	@Test
	void test_getStatusChangedMessageForAppReturns_UP_to_DOWN() {
		notifier.updateLastStatus(new InstanceStatusChangedEvent(instance.getId(), 1L, StatusInfo.ofUp()));

		Message message = notifier.getStatusChangedMessage(instance, notifier.createEvaluationContext(
				new InstanceStatusChangedEvent(instance.getId(), 1L, StatusInfo.ofDown()), instance));

		assertMessage(message, notifier.getStatusChangedTitle(), notifier.getMessageSummary(),
				"Test App with id TestAppId changed status from UP to DOWN", RED);
	}

	@Test
	void test_getStatusChangedMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
		notifier.setStatusActivitySubtitle("STATUS_ACTIVITY_PATTERN_#{instance.registration.name}");
		Message message = notifier.getStatusChangedMessage(instance,
				notifier.createEvaluationContext(new InstanceDeregisteredEvent(instance.getId(), 1L), instance));

		assertThat(message.getSections().get(0).getActivitySubtitle()).isEqualTo("STATUS_ACTIVITY_PATTERN_" + appName);
	}

	@Test
	void test_getRegisterMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
		notifier.setRegisterActivitySubtitle("REGISTER_ACTIVITY_PATTERN_#{instance.registration.name}");
		Message message = notifier.getRegisteredMessage(instance,
				notifier.createEvaluationContext(new InstanceDeregisteredEvent(instance.getId(), 1L), instance));

		assertThat(message.getSections().get(0).getActivitySubtitle())
			.isEqualTo("REGISTER_ACTIVITY_PATTERN_" + appName);
	}

	@Test
	void test_getDeRegisterMessageWithExtraFormatArgumentReturns_activitySubtitlePatternWithAppName() {
		notifier.setDeregisterActivitySubtitle("DEREGISTER_ACTIVITY_PATTERN_#{instance.registration.name}");
		Message message = notifier.getDeregisteredMessage(instance,
				notifier.createEvaluationContext(new InstanceDeregisteredEvent(instance.getId(), 1L), instance));

		assertThat(message.getSections().get(0).getActivitySubtitle())
			.isEqualTo("DEREGISTER_ACTIVITY_PATTERN_" + appName);
	}

	@Test
	void test_getStatusChangedMessage_parsesThemeColorFromSpelExpression() {
		notifier.setThemeColor(
				"#{event.type == 'STATUS_CHANGED' ? (event.statusInfo.status=='UP' ? 'green' : 'red') : 'blue'}");

		Message message = notifier.getStatusChangedMessage(instance, notifier.createEvaluationContext(
				new InstanceStatusChangedEvent(instance.getId(), 1L, StatusInfo.ofUp()), instance));

		assertThat(message.getThemeColor()).isEqualTo("green");
	}

	private void assertMessage(Message message, String expectedTitle, String expectedSummary, String expectedSubTitle,
			String expectedColor) {
		assertThat(message.getTitle()).isEqualTo(expectedTitle);
		assertThat(message.getSummary()).isEqualTo(expectedSummary);
		assertThat(message.getThemeColor()).isEqualTo(expectedColor);

		assertThat(message.getSections()).hasSize(1).anySatisfy((section) -> {
			assertThat(section.getActivityTitle()).isEqualTo(instance.getRegistration().getName());
			assertThat(section.getActivitySubtitle()).isEqualTo(expectedSubTitle);

			assertThat(section.getFacts()).hasSize(5).anySatisfy((fact) -> {
				assertThat(fact.getName()).isEqualTo("Status");
				assertThat(fact.getValue()).isEqualTo("UNKNOWN");
			}).anySatisfy((fact) -> {
				assertThat(fact.getName()).isEqualTo("Service URL");
				assertThat(fact.getValue()).isEqualTo(serviceUrl);
			}).anySatisfy((fact) -> {
				assertThat(fact.getName()).isEqualTo("Health URL");
				assertThat(fact.getValue()).isEqualTo(healthUrl);
			}).anySatisfy((fact) -> {
				assertThat(fact.getName()).isEqualTo("Management URL");
				assertThat(fact.getValue()).isEqualTo(managementUrl);
			}).anySatisfy((fact) -> {
				assertThat(fact.getName()).isEqualTo("Source");
				assertThat(fact.getValue()).isEqualTo(null);
			});
		});
	}

}
